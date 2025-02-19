package pfe.mandomati.iamms.Exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import pfe.mandomati.iamms.Model.Request;
import pfe.mandomati.iamms.Model.User;
import pfe.mandomati.iamms.Model.Enums.RequestType;
import pfe.mandomati.iamms.Service.RequestService;
import pfe.mandomati.iamms.Exception.UserNotFoundException;
import pfe.mandomati.iamms.Exception.KeycloakException;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private RequestService requestService;
    
    @Autowired
    private HttpServletRequest servletRequest;

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleSecurityException(Exception exception) {
        ProblemDetail errorDetail = null;
        int statusCode = 500;

        // Détermination du code d'état HTTP et des détails de l'erreur
        if (exception instanceof BadCredentialsException) {
            statusCode = 401;
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(statusCode), exception.getMessage());
            errorDetail.setProperty("description", "The username or password is incorrect");
        } else if (exception instanceof AccountStatusException) {
            statusCode = 403;
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(statusCode), exception.getMessage());
            errorDetail.setProperty("description", "The account is locked");
        } else if (exception instanceof AccessDeniedException) {
            statusCode = 403;
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(statusCode), exception.getMessage());
            errorDetail.setProperty("description", "You are not authorized to access this resource");
        } else if (exception instanceof UserNotFoundException) {
            statusCode = 404;
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(statusCode), exception.getMessage());
            errorDetail.setProperty("description", "User not found");
        } else if (exception instanceof KeycloakException) {
            statusCode = 500;
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(statusCode), exception.getMessage());
            errorDetail.setProperty("description", "Keycloak error");
        } else {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(statusCode), exception.getMessage());
            errorDetail.setProperty("description", "Unknown internal server error.");
        }

        // Enregistrement de la requête
        logRequest(determineRequestType(exception), statusCode);

        return errorDetail;
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ProblemDetail handleNoHandlerFoundException(NoHandlerFoundException exception) {
        int statusCode = 404;
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(statusCode), "No handler found for " + exception.getHttpMethod() + " " + exception.getRequestURL());
        errorDetail.setProperty("description", "The requested URL was not found on this server.");

        // Enregistrement de la requête
        logRequest(RequestType.API_CALL, statusCode);

        return errorDetail;
    }

    /**
     * Détermine le type de requête en fonction de l'exception
     */
    private RequestType determineRequestType(Exception exception) {
        if (exception instanceof BadCredentialsException) {
            return RequestType.LOGIN;
        } else {
            return RequestType.API_CALL;
        }
    }

    /**
     * Enregistre la requête dans la base de données
     */
    private void logRequest(RequestType requestType, int statusCode) {
        try {
            // Récupération du corps de la requête
            String requestBody = getRequestBody();
            
            // Création d'une nouvelle entrée de requête
            Request request = new Request();
            request.setType(requestType);
            request.setRequestDate(LocalDateTime.now());
            request.setIpAddress(getClientIpAddress());
            request.setUser(getCurrentUser());
            request.setEndpoint(servletRequest.getRequestURI());
            request.setRequestBody(requestBody);
            request.setStatusCode(statusCode);
            
            // Enregistrement dans la base de données via RequestService
            requestService.logRequest(request);
        } catch (Exception e) {
            // Log l'erreur mais ne la propage pas pour ne pas bloquer la réponse d'erreur principale
            System.err.println("Failed to log request: " + e.getMessage());
        }
    }

    /**
     * Récupère l'utilisateur actuellement authentifié
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Récupère l'adresse IP du client
     */
    private String getClientIpAddress() {
        String xForwardedForHeader = servletRequest.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            return xForwardedForHeader.split(",")[0].trim();
        }
        return servletRequest.getRemoteAddr();
    }

    /**
     * Récupère le corps de la requête
     */
    private String getRequestBody() {
        try {
            BufferedReader reader = servletRequest.getReader();
            if (reader != null) {
                return reader.lines().collect(Collectors.joining());
            }
        } catch (IOException e) {
            System.err.println("Failed to read request body: " + e.getMessage());
        }
        return "";
    }
}