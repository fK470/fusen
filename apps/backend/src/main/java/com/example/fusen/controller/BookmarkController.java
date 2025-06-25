package com.example.fusen.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.fusen.dto.BookmarkRequest;
import com.example.fusen.dto.BookmarkResponse;
import com.example.fusen.entity.Bookmark;
import com.example.fusen.service.BookmarkService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/bookmarks")
public class BookmarkController {

  private final BookmarkService bookmarkService;

  @Autowired
  public BookmarkController(BookmarkService bookmarkService) {
    this.bookmarkService = bookmarkService;
  }

  @GetMapping
  public List<BookmarkResponse> getAllBookmarks(
      @RequestParam(defaultValue = "10") int limit,
      @RequestParam(defaultValue = "0") int offset) {
    return bookmarkService.findAll(limit, offset).stream()
        .map(BookmarkResponse::new)
        .toList();
  }

  @GetMapping("/{id}")
  public BookmarkResponse getBookmarkById(@PathVariable Long id) {
    return new BookmarkResponse(bookmarkService.findById(id));
  }

  @PostMapping
  public ResponseEntity<BookmarkResponse> createBookmark(@Valid @RequestBody BookmarkRequest request) {
    Bookmark bookmark = new Bookmark();
    bookmark.setUrl(request.getUrl());
    bookmark.setTitle(request.getTitle());
    bookmark.setDescription(request.getDescription());
    bookmark.setTags(bookmarkService.convertTags(request.getTags()));
    Bookmark createdBookmark = bookmarkService.create(bookmark);
    return new ResponseEntity<>(new BookmarkResponse(createdBookmark), HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public BookmarkResponse updateBookmark(@PathVariable Long id, @Valid @RequestBody BookmarkRequest request) {
    Bookmark existingBookmark = bookmarkService.findById(id); // Throws BookmarkNotFoundException if not found
    existingBookmark.updateFromRequest(request.getUrl(), request.getTitle(), request.getDescription(),
        bookmarkService.convertTags(request.getTags()));
    Bookmark updatedBookmark = bookmarkService.update(existingBookmark);
    return new BookmarkResponse(updatedBookmark);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteBookmark(@PathVariable Long id) {
    bookmarkService.delete(id); // Throws BookmarkNotFoundException if not found
  }
}
