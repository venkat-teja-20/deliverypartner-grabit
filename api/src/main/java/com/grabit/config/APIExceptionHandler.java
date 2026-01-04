package com.grabit.config;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.grabit.enums.CommonErrors;
import com.grabit.exception.APIError;
import com.grabit.exception.CustomException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
@Log4j2
public class APIExceptionHandler {

    @ExceptionHandler(value = CustomException.class)
    public APIError handleCustomException(CustomException ex, HttpServletResponse response) {
        response.setStatus(ex.getErrorObject().getHttpCode());
        return ex.getErrorObject().getErrorMsg();
    }

    @ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
    public APIError handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex, HttpServletResponse response) {
        response.setStatus(406);
        return new APIError("INVALID_MEDIA_TYPE", ex.getMessage());
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public APIError handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletResponse response) {
        try {
            response.setStatus(409);
            if (ex.getMessage().contains("unique constraint")) {
                String regex = "Key \\((.*?)\\)=\\((.*?)\\)";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(ex.getMessage());
                if (matcher.find()) {
                    String name = matcher.group(1);
                    String value = matcher.group(2);
                    return new APIError("DUPLICATE_RECORD", name + "already exists");
                }
            }
            return new APIError("INVALID_REQUEST", ex.getMessage());
        } catch (Exception e) {
            response.setStatus(409);
            return new APIError("INTEGRITY_VIOLATION", ex.getMessage());
        }
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    public APIError handleNoResourceFoundException(NoResourceFoundException ex, HttpServletResponse response) {
        response.setStatus(ex.getBody().getStatus());
        return new APIError("INVALID_REQUEST_PATH", "The requested resource " + ex.getResourcePath() + " does not exist");
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public APIError handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HttpServletResponse response) {
        response.setStatus(ex.getBody().getStatus());
        return new APIError("METHOD_NOT_ALLOWED", ex.getBody().getDetail());
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public APIError handleConstraintViolationException(ConstraintViolationException ex, HttpServletResponse response) {
        response.setStatus(400);
        try {
            if (ex.getConstraintViolations().iterator().next().getMessageTemplate().contains("NotBlank")) {
                return new APIError("REQUIRED_FIELD_MISSING", ex.getConstraintViolations().iterator().next().getPropertyPath() + " must not be null or empty");
            }
            Integer max = (Integer) ex.getConstraintViolations().iterator().next().getConstraintDescriptor().getAttributes().get("max");
            Integer min = (Integer) ex.getConstraintViolations().iterator().next().getConstraintDescriptor().getAttributes().get("min");
            String message = "";
            String field = String.valueOf(ex.getConstraintViolations().iterator().next().getPropertyPath()).toUpperCase();
            if (min != null && min == 0)
                message = field + " can contain up to " +
                        max + " characters";
            else if (min != null && min.equals(max))
                message = field + " must be exactly " +
                        max + " characters";
            else
                message = field + " should have a minimum of " +
                        min + " and a maximum of " + max + " characters";
            return new APIError("CONSTRAINT_LIMIT", message);
        } catch (Exception e){
            return new APIError("CONSTRAINT_LIMITATION_ERROR", ex.getMessage());
        }
    }

    @ExceptionHandler(value = EntityNotFoundException.class)
    public APIError handleEntityNotFoundException(EntityNotFoundException ex, HttpServletResponse response) {
        response.setStatus(404);
        return new APIError("DATA_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(value = EntityExistsException.class)
    public APIError handleEntityExistsException(EntityExistsException ex, HttpServletResponse response) {
        response.setStatus(409);
        return new APIError("DUPLICATE_RECORD", ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public APIError handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletResponse response) {
        try {
            Throwable cause = ex.getMostSpecificCause();
            response.setStatus(406);
            List<String> typeList = new ArrayList<>(List.of("number", "double", "integer", "long"));
            if (cause instanceof InvalidFormatException invalidFormatEx)
                if (typeList.contains(invalidFormatEx.getTargetType().getSimpleName().toLowerCase())) {
                    return new APIError("INVALID_INPUT", "Expected value of type Number in " + invalidFormatEx.getPath().getFirst().getFieldName() + " but received : " + invalidFormatEx.getValue());
                } else if ("boolean".equalsIgnoreCase(invalidFormatEx.getTargetType().getSimpleName())) {
                    return new APIError("INVALID_INPUT", "Expected true or false in " + invalidFormatEx.getPath().getFirst().getFieldName() + " but received : " + invalidFormatEx.getValue());
                } else
                    return new APIError("INVALID_INPUT", "Expected value of type Text in " + invalidFormatEx.getPath().getFirst().getFieldName() + " but received : " + invalidFormatEx.getValue());
            else if (cause instanceof MismatchedInputException) {
                response.setStatus(400);
                return new APIError(CommonErrors.REQUEST_BODY_MISSING.toString(), CommonErrors.REQUEST_BODY_MISSING.getMessage());
            } else if (cause instanceof DateTimeParseException) {
                String dateString = cause.getMessage().split("'")[1];
                return new APIError("INVALID_DATE_FORMAT", dateString + " is not in acceptable Date Format");
            } else if (cause instanceof CustomException e) {
                return e.getErrorObject().getErrorMsg();
            } else
                return new APIError("INVALID_INPUT", cause.getMessage());
        } catch (Exception e) {
            return new APIError("INVALID_REQUEST",ex.getMessage());
        }
    }

    @ExceptionHandler(value = Exception.class)
    public APIError handleException(Exception ex, HttpServletResponse response) {
        if(ex instanceof AccessDeniedException e)
            throw e;
        log.info("Unknown Issue Occurred : " + ex.getMessage());
        response.setStatus(500);
        return new APIError(CommonErrors.unknown_error.toString(), CommonErrors.unknown_error.getMessage());
    }
}
