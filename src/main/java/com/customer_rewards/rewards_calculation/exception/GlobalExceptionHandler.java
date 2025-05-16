package com.customer_rewards.rewards_calculation.exception;

import com.customer_rewards.rewards_calculation.exception.customException.*;
import com.customer_rewards.rewards_calculation.util.ErrorResponse;
import com.fasterxml.jackson.core.JsonParseException;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles RuntimeExceptions by returning a BAD_REQUEST error response.
     *
     * @param ex the runtime exception encountered
     * @return a ResponseEntity with an error response and a BAD_REQUEST status
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    /**
     * Handles all exceptions by returning an INTERNAL_SERVER_ERROR response.
     *
     * @param ex the exception encountered
     * @return a ResponseEntity with an ErrorResponse and INTERNAL_SERVER_ERROR status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error: " + ex.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles validation errors and returns a BAD_REQUEST response.
     *
     * @param ex the MethodArgumentNotValidException containing validation errors
     * @return a ResponseEntity with an error response and status BAD_REQUEST
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<String> errorMessages = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(objectError -> {
                    if (objectError instanceof FieldError) {
                        return ((FieldError) objectError).getField() + ": " + objectError.getDefaultMessage();
                    }
                    return objectError.getDefaultMessage();
                })
                .collect(Collectors.toList());
        String errorMessage = String.join("; ", errorMessages);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed: " + errorMessage,
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles ConstraintViolationException by returning a BAD_REQUEST error response.
     *
     * @param ex the ConstraintViolationException containing validation error details
     * @return a ResponseEntity with an ErrorResponse and BAD_REQUEST status
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation error: " + errorMessage,
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles malformed JSON requests by returning a BAD_REQUEST error response.
     *
     * @param ex the HttpMessageNotReadableException indicating the message was not readable
     * @param request the current WebRequest
     * @return a ResponseEntity containing the error response and a BAD_REQUEST status
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {

        String errorMessage = "Malformed JSON request";

        Throwable cause = ex.getCause();
        if (cause instanceof JsonParseException) {
            errorMessage = "JSON parse error: " + cause.getMessage();
        }

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage,System.currentTimeMillis());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles database access errors by returning an INTERNAL_SERVER_ERROR response.
     *
     * @param ex the DataAccessException that occurred
     * @param request the current WebRequest
     * @return a ResponseEntity containing an ErrorResponse and INTERNAL_SERVER_ERROR status
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex, WebRequest request) {
        String message = "A database error occurred.";

        if (ex.getMessage() != null && ex.getMessage().contains("Table \"CUSTOMER\" not found")) {
            message = "Required database table (CUSTOMER) was not found. Please ensure that the database has been properly initialized.";
        }

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                message,
                System.currentTimeMillis());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles MethodArgumentTypeMismatchException by returning a BAD_REQUEST response with details about the type mismatch.
     *
     * @param ex the exception indicating the type mismatch in request parameters
     * @param request the current WebRequest
     * @return a ResponseEntity containing an ErrorResponse and a BAD_REQUEST status
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            WebRequest request) {
        String paramName = ex.getName();
        String invalidValue = ex.getValue() != null ? ex.getValue().toString() : "null";
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown";
        String message = String.format("Invalid value '%s' for parameter '%s'. It must be of type '%s'.",
                invalidValue, paramName, requiredType);

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message, System.currentTimeMillis());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    /**
     * Handles cases where no handler is found for a given request by returning a NOT_FOUND response.
     *
     * @param ex the NoHandlerFoundException thrown when no handler is found
     * @param request the HttpServletRequest that resulted in the exception
     * @return a ResponseEntity containing error details and a NOT_FOUND status
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", new Date());
        errorDetails.put("status", HttpStatus.NOT_FOUND.value());
        errorDetails.put("error", "Not Found");
        errorDetails.put("message", "No mapping found for " + request.getMethod() + " " + request.getRequestURI());
        errorDetails.put("path", request.getRequestURI());

        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles CustomerNotFoundException by returning a NOT_FOUND response with error details.
     *
     * @param ex the CustomerNotFoundException thrown when a customer is not found
     * @return a ResponseEntity with a map containing the error status, message, and timestamp
     */
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCustomerNotFound(CustomerNotFoundException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("error", ex.getMessage());
        errorResponse.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles NullArgumentException by returning a BAD_REQUEST response with error details.
     *
     * @param ex the NullArgumentException that occurred
     * @return a ResponseEntity with a map containing error details and a BAD_REQUEST status
     */
    @ExceptionHandler(NullArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleNullArgumentException(NullArgumentException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("error", ex.getMessage());
        errorResponse.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

    }

    /**
     * Handles InvalidEmailException by returning a BAD_REQUEST response with error details.
     *
     * @param ex the InvalidEmailException that occurred
     * @return a ResponseEntity containing a map with error information and a BAD_REQUEST status
     */
    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidEmailException(InvalidEmailException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("error", ex.getMessage());
        errorResponse.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles InvalidPhoneException by returning a BAD_REQUEST response with error details.
     *
     * @param ex the InvalidPhoneException that occurred
     * @return a ResponseEntity containing a map with error details and a BAD_REQUEST status
     */
    @ExceptionHandler(InvalidPhoneException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPhoneException(InvalidPhoneException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("error", ex.getMessage());
        errorResponse.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles InvalidPurchaseAmountException by returning a BAD_REQUEST response with error details.
     *
     * @param ex the InvalidPurchaseAmountException that occurred
     * @return a ResponseEntity containing a map with error details and a BAD_REQUEST status
     */
    @ExceptionHandler(InvalidPurchaseAmountException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPurchaseAmount(InvalidPurchaseAmountException ex) {

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("error", ex.getMessage());
        errorResponse.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles InvalidPatternException by returning a BAD_REQUEST response with error details.
     *
     * @param ex the InvalidPatternException that occurred
     * @return a ResponseEntity containing a map of error details with a BAD_REQUEST status
     */
    @ExceptionHandler(InvalidPatternException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPatternException(InvalidPatternException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("error", ex.getMessage());
        errorResponse.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles InvalidDateFormatException by returning a BAD_REQUEST response with error details.
     *
     * @param ex the InvalidDateFormatException that occurred
     * @return a ResponseEntity containing a map with error details and a BAD_REQUEST status
     */
    @ExceptionHandler(InvalidDateFormatException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidDateFormat(InvalidDateFormatException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("error", ex.getMessage());
        errorResponse.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

}
