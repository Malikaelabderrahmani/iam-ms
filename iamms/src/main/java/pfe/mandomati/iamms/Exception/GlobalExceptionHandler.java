package pfe.mandomati.iamms.Exception;


import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import pfe.mandomati.iamms.Model.Request;
import pfe.mandomati.iamms.Service.RequestService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.BufferedReader;
import java.io.IOException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final RequestService requestService;

    public GlobalExceptionHandler(RequestService requestService) {
        this.requestService = requestService;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleSecurityException(Exception exception, HttpServletRequest request) {
        ProblemDetail errorDetail = null;

        // Enregistrement de la requête sauf si le code de statut est 200
        int statusCode = getStatusCode(exception);
        if (statusCode != 200) {
            Request logRequest = new Request();
            logRequest.setEndpoint(request.getRequestURI());
            logRequest.setIpAddress(request.getRemoteAddr());
            logRequest.setRequestBody(getRequestBody(request));
            logRequest.setStatusCode(statusCode);
            requestService.logRequest(logRequest);
        }

        // Gestion des exceptions spécifiques
        if (exception instanceof BadCredentialsException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(401), exception.getMessage());
            errorDetail.setProperty("description", "Le nom d'utilisateur ou le mot de passe est incorrect");
        } else if (exception instanceof AccountStatusException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
            errorDetail.setProperty("description", "Le compte est verrouillé");
        } else if (exception instanceof AccessDeniedException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
            errorDetail.setProperty("description", "Vous n'êtes pas autorisé à accéder à cette ressource");
        } else {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), exception.getMessage());
            errorDetail.setProperty("description", "Erreur interne du serveur inconnue.");
        }

        return errorDetail;
    }

    

    private int getStatusCode(Exception exception) {
        // Vérification des types d'exception pour déterminer le code de statut
        if (exception instanceof BadCredentialsException) {
            return HttpStatus.UNAUTHORIZED.value(); // 401
        } else if (exception instanceof AccountStatusException) {
            return HttpStatus.FORBIDDEN.value(); // 403
        } else if (exception instanceof AccessDeniedException) {
            return HttpStatus.FORBIDDEN.value(); // 403
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR.value(); // 500
        }
    }

    private String getRequestBody(HttpServletRequest request) {
        StringBuilder requestBody = new StringBuilder();
        try {
            // Lire le corps de la requête
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }   
        } catch (IOException e) {
            // En cas d'erreur lors de la lecture du corps de la requête
            log.error("Error reading request body: ", e);
        }
        return requestBody.toString();
    }
}
   