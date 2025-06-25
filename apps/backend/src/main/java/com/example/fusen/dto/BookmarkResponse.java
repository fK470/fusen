package com.example.fusen.dto;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.example.fusen.entity.Bookmark;

public class BookmarkResponse {
  private Long id;
  private String url;
  private String title;
  private String description;
  private List<String> tags;
  private String createdAt; // ISO 8601形式の文字列
  private String updatedAt; // ISO 8601形式の文字列

  private static final DateTimeFormatter ISO_8601_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

  public BookmarkResponse(Bookmark bookmark) {
    this.id = bookmark.getId();
    this.url = bookmark.getUrl();
    this.title = bookmark.getTitle();
    this.description = bookmark.getDescription();
    this.tags = bookmark.getTags().stream()
        .map(tag -> tag.getName())
        .collect(Collectors.toList());
    this.createdAt = formatLocalDateTimeToISO8601(bookmark.getCreatedAt());
    this.updatedAt = formatLocalDateTimeToISO8601(bookmark.getUpdatedAt());
  }

  private String formatLocalDateTimeToISO8601(LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    // LocalDateTimeをUTCとして扱い、ISO 8601形式にフォーマット
    return dateTime.atOffset(ZoneOffset.UTC).format(ISO_8601_FORMATTER);
  }

  // Getters
  public Long getId() {
    return id;
  }

  public String getUrl() {
    return url;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public List<String> getTags() {
    return tags;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }
}
