package com.example.fusen.exception;

public class BookmarkNotFoundException extends RuntimeException {
  public BookmarkNotFoundException(String message) {
    super(message);
  }
}
