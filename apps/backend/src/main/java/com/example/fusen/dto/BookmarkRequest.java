package com.example.fusen.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class BookmarkRequest {
  @NotBlank(message = "URLは必須です。")
  @Pattern(regexp = "^(http|https)://.*", message = "URLはhttp://またはhttps://で始まる必要があります。")
  private String url;
  private String title;
  private String description;
  private List<String> tags;

  // Getters and Setters
  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }
}
