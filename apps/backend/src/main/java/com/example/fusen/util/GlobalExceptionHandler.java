package com.example.fusen.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.fusen.exception.BookmarkNotFoundException;
import com.example.fusen.exception.DuplicateUrlException;
import com.example.fusen.exception.InvalidUrlException;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BookmarkNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleBookmarkNotFoundException(BookmarkNotFoundException ex) {
    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("errorCode", "BOOKMARK_NOT_FOUND");
    errorResponse.put("message", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(DuplicateUrlException.class)
  public ResponseEntity<Map<String, String>> handleDuplicateUrlException(DuplicateUrlException ex) {
    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("errorCode", "DUPLICATE_URL");
    errorResponse.put("message", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(InvalidUrlException.class)
  public ResponseEntity<Map<String, String>> handleInvalidUrlException(InvalidUrlException ex) {
    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("errorCode", "INVALID_URL");
    errorResponse.put("message", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("errorCode", "VALIDATION_ERROR");
    errorResponse.put("message", "Validation failed: " + errors.toString());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("errorCode", "INTERNAL_SERVER_ERROR");
    errorResponse.put("message", "An unexpected error occurred: " + ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
