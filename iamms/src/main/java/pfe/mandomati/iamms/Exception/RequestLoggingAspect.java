package pfe.mandomati.iamms.Exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.keycloak.KeycloakPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.fasterxml.jackson.databind.ObjectMapper;
import pfe.mandomati.iamms.Model.Request;
import pfe.mandomati.iamms.Model.Enums.RequestType;
import pfe.mandomati.iamms.Repository.UserRepository;
import pfe.mandomati.iamms.Service.RequestService;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakPrincipal;


@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RequestLoggingAspect {

    private final RequestService requestService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Around("@within(org.springframework.web.bind.annotation.RestController) || @within(org.springframework.stereotype.Controller)")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {

        log.info("=== Starting request logging ===");

        HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest();

        log.info("URI: {}", httpRequest.getRequestURI());
        log.info("Method: {}", httpRequest.getMethod());

        Request request = new Request();
        request.setEndpoint(httpRequest.getRequestURI());
        request.setIpAddress(getClientIp(httpRequest));
        request.setRequestBody(extractRequestBody(joinPoint));
        request.setType(determineRequestType(httpRequest));

        log.debug("Created request object: {}", request);


        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.info("Authentication is present");
            if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof KeycloakPrincipal) {
                log.info("Principal is KeycloakPrincipal");
                try {
                    KeycloakPrincipal principal = (KeycloakPrincipal) SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal();
                    String email = principal.getKeycloakSecurityContext().getToken().getEmail();
                    log.info("User email: {}", email);
                    
                    userRepository.findByEmail(email).ifPresent(user -> {
                        request.setUser(user);
                        log.info("User set in request: {}", user.getEmail());
                    });
                } catch (Exception e) {
                    log.error("Error while setting user: ", e);
                }
            }
        }
        
        try {
            log.info("Proceeding with request execution");
            Object result = joinPoint.proceed();
            request.setStatusCode(200);
            log.info("Request executed successfully, setting status code 200");
            
            try {
                log.info("Attempting to save request");
                requestService.logRequest(request);
                log.info("Request saved successfully");
            } catch (Exception e) {
                log.error("Failed to save request: ", e);
            }
            return result;
        } catch (Exception e) {
            log.error("Exception occurred during request execution: ", e);
            int statusCode = mapExceptionToStatusCode(e);
            log.info("Mapped status code: {}", statusCode);
            request.setStatusCode(statusCode);
            
            try {
                log.info("Attempting to save failed request");
                requestService.logRequest(request);
                log.info("Failed request saved successfully");
            } catch (Exception saveException) {
                log.error("Failed to save failed request: ", saveException);
            }
            
            throw mapToCustomException(e);
        } finally {
        log.info("=== Request logging completed ===");
    }
    }

    private Exception mapToCustomException(Exception e) {
        if (e.getMessage().contains("not found")) {
            return new ResourceNotFoundException(e.getMessage());
        } else if (e.getMessage().toLowerCase().contains("access denied") 
                  || e.getMessage().toLowerCase().contains("forbidden")) {
            return new AccessDeniedException(e.getMessage());
        } else if (e.getMessage().toLowerCase().contains("unauthorized") 
                  || e.getMessage().toLowerCase().contains("not authenticated")) {
            return new UnauthorizedException(e.getMessage());
        } else if (e.getMessage().toLowerCase().contains("bad gateway") 
                  || e.getMessage().toLowerCase().contains("service unavailable")) {
            return new BadGatewayException(e.getMessage());
        }
        return e;
    }

    private int mapExceptionToStatusCode(Exception e) {
        String message = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        log.info("Mapping exception message to status code: {}", message);
        
        if (message.contains("not found")) {
            return 404;
        } else if (message.contains("access denied") || message.contains("forbidden")) {
            return 403;
        } else if (message.contains("unauthorized") || message.contains("not authenticated")) {
            return 401;
        } else if (message.contains("bad gateway") || message.contains("service unavailable")) {
            return 502;
        } else if (message.contains("bad request") || message.contains("invalid")) {
            return 400;
        }
        return 500;
    }
   
    private RequestType determineRequestType(HttpServletRequest request) {
        String path = request.getRequestURI().toLowerCase();
        if (path.contains("/login")) {
            return RequestType.LOGIN;
        } else if (path.contains("/logout")) {
            return RequestType.LOGOUT;
        } else if (path.contains("/refresh")) {
            return RequestType.TOKEN_REFRESH;
        }
        return RequestType.API_CALL;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    private String extractRequestBody(ProceedingJoinPoint joinPoint) {
        try {
            return objectMapper.writeValueAsString(joinPoint.getArgs());
        } catch (Exception e) {
            return "Could not serialize request body";
        }
    }  
}