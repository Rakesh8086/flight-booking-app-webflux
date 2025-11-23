package com.flight.app;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(WebExchangeBindException ex) {
        
        Map<String, String> errorMap = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName;
            if (error instanceof FieldError) {
                fieldName = ((FieldError) error).getField();
            } 
            else {
                fieldName = error.getObjectName(); 
            }
            String message = error.getDefaultMessage();
            errorMap.put(fieldName, message);
        });
        
        // 400 Bad request
        return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST); 
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleServiceValidationExceptions(IllegalArgumentException ex) {
        // 400 Bad Request
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex, WebClient request) {
        // Return a reason for the exception
        return new ResponseEntity<>(ex.getReason(), ex.getStatusCode());
    }
}
