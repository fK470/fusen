package com.example.fusen.exception;

public class DuplicateUrlException extends RuntimeException {
  public DuplicateUrlException(String message) {
    super(message);
  }
}
