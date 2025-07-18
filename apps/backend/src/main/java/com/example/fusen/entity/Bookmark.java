package com.example.fusen.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.seasar.doma.Column;
import org.seasar.doma.Entity;
import org.seasar.doma.GeneratedValue;
import org.seasar.doma.GenerationType;
import org.seasar.doma.Id;
import org.seasar.doma.Table;
import org.seasar.doma.Transient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bookmarks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bookmark {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "url")
  private String url;

  @Column(name = "title")
  private String title;

  @Column(name = "description")
  private String description;

  @Transient
  @Builder.Default
  private Set<Tag> tags = new HashSet<>();

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public void updateFromRequest(String url, String title, String description, Set<Tag> tags) {
    this.url = url;
    this.title = title;
    this.description = description;
    this.tags = tags;
    this.updatedAt = LocalDateTime.now();
  }
}