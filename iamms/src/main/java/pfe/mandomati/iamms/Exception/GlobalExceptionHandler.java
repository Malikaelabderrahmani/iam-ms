package pfe.mandomati.iamms.Exception;

import org.keycloak.common.VerificationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import pfe.mandomati.iamms.Model.Enums.RequestType;
import pfe.mandomati.iamms.Model.Request;
import pfe.mandomati.iamms.Service.RequestService;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private RequestService requestService;

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleSecurityException(Exception exception, HttpServletRequest httpServletRequest) {
        ProblemDetail errorDetail;

        exception.printStackTrace();

        if (exception instanceof BadCredentialsException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
            errorDetail.setProperty("description", "The username or password is incorrect");
        } else if (exception instanceof AccountStatusException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
            errorDetail.setProperty("description", "The account is locked");
        } else if (exception instanceof AccessDeniedException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
            errorDetail.setProperty("description", "You are not authorized to access this resource");
        } else if (exception instanceof VerificationException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
            errorDetail.setProperty("description", "The Keycloak token is invalid or expired");
        } else {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
            errorDetail.setProperty("description", "Unknown internal server error.");
        }

        Request request = new Request();
        request.setType(RequestType.API_CALL);
        request.setRequestDate(LocalDateTime.now());
        request.setIpAddress(httpServletRequest.getRemoteAddr());
        request.setUser(null);
        request.setEndpoint(httpServletRequest.getRequestURI());
        request.setRequestBody(httpServletRequest.getQueryString());
        request.setStatusCode(errorDetail.getStatus());

        requestService.logRequest(request);

        return errorDetail;
    }
}
