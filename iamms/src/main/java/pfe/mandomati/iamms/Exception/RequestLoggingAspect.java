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
        HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest();
        
        Request request = new Request();
        request.setEndpoint(httpRequest.getRequestURI());
        request.setIpAddress(getClientIp(httpRequest));
        request.setRequestBody(extractRequestBody(joinPoint));
        request.setType(determineRequestType(httpRequest));

        log.debug("Created request object: {}", request);


        if (SecurityContextHolder.getContext().getAuthentication() != null 
        && SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof KeycloakPrincipal) {
        
        KeycloakPrincipal principal = (KeycloakPrincipal) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
        String email = principal.getKeycloakSecurityContext().getToken().getEmail();
        userRepository.findByEmail(email).ifPresent(request::setUser);
        }
        
        try {
            Object result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            int statusCode = mapExceptionToStatusCode(e);
            request.setStatusCode(statusCode);
            requestService.logRequest(request);
            
            // Relancer l'exception appropriée
            throw mapToCustomException(e);
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
        String message = e.getMessage().toLowerCase();
        log.debug("Exception message: " + message);

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