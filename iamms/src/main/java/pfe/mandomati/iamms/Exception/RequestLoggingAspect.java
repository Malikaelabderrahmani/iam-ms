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
import pfe.mandomati.iamms.Repository.RequestRepository;
import pfe.mandomati.iamms.Repository.UserRepository;
import org.keycloak.KeycloakPrincipal;


@Aspect
@Component
@RequiredArgsConstructor
public class RequestLoggingAspect {
    
    private final RequestRepository requestRepository;
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

        // Set user if authenticated and exists in our database
        if (SecurityContextHolder.getContext().getAuthentication() != null 
            && SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof KeycloakPrincipal) {
            
            KeycloakPrincipal principal = (KeycloakPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
            String email = principal.getKeycloakSecurityContext().getToken().getEmail();
            
            // On cherche juste l'utilisateur sans en créer un nouveau
            userRepository.findByEmail(email).ifPresent(request::setUser);
        }
        
        try {
            Object result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            request.setStatusCode(determineStatusCode(e));
            // On enregistre la requête échouée
            requestRepository.save(request);
            throw e;
        }
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

    private int determineStatusCode(Exception e) {
        if (e instanceof SecurityException || e instanceof AccessDeniedException) {
            return 403;
        } else if (e instanceof IllegalArgumentException) {
            return 400;
        } else if (e instanceof ResourceNotFoundException) {
            return 404;
        } else if (e instanceof UnauthorizedException) {
            return 401;
        } else if (e instanceof BadGatewayException) {
            return 502;
        } else {
            return 500;
        }
    }
}