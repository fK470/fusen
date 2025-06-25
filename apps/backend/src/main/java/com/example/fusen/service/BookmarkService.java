package com.example.fusen.service;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.seasar.doma.jdbc.SelectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fusen.dao.BookmarkDao;
import com.example.fusen.dao.BookmarkTagDao;
import com.example.fusen.dao.TagDao;
import com.example.fusen.entity.Bookmark;
import com.example.fusen.entity.Tag;
import com.example.fusen.exception.BookmarkNotFoundException;
import com.example.fusen.exception.DuplicateUrlException;
import com.example.fusen.exception.InvalidUrlException;

@Service
@Transactional
public class BookmarkService {

  private final BookmarkDao bookmarkRepository;
  private final TagDao tagRepository;
  private final BookmarkTagDao bookmarkTagRepository;

  @Autowired
  public BookmarkService(BookmarkDao bookmarkRepository, TagDao tagRepository, BookmarkTagDao bookmarkTagRepository) {
    this.bookmarkRepository = bookmarkRepository;
    this.tagRepository = tagRepository;
    this.bookmarkTagRepository = bookmarkTagRepository;
  }

  public List<Bookmark> findAll(int limit, int offset) {
    SelectOptions options = SelectOptions.get().limit(limit).offset(offset);
    List<Bookmark> bookmarks = bookmarkRepository.findAll(options);
    // Load tags for each bookmark
    for (Bookmark bookmark : bookmarks) {
      List<Tag> tags = bookmarkTagRepository.findTagsByBookmarkId(bookmark.getId());
      bookmark.setTags(new HashSet<>(tags));
    }
    return bookmarks;
  }

  public Bookmark findById(Long id) {
    Bookmark bookmark = bookmarkRepository.findById(id)
        .orElseThrow(() -> new BookmarkNotFoundException("Bookmark not found with id: " + id));
    // Load tags for the bookmark
    List<Tag> tags = bookmarkTagRepository.findTagsByBookmarkId(bookmark.getId());
    bookmark.setTags(new HashSet<>(tags));
    return bookmark;
  }

  public Bookmark create(Bookmark bookmark) {
    validateUrl(bookmark.getUrl());
    if (bookmarkRepository.existsByUrl(bookmark.getUrl())) {
      throw new DuplicateUrlException("Bookmark with URL already exists: " + bookmark.getUrl());
    }
    Set<Tag> managedTags = getOrCreateTags(bookmark.getTags());
    bookmark.setTags(managedTags);
    bookmarkRepository.insert(bookmark);
    
    // Save bookmark-tag relationships
    saveBookmarkTagRelationships(bookmark.getId(), managedTags);
    return bookmark;
  }

  public Bookmark update(Bookmark bookmark) {
    if (bookmarkRepository.findById(bookmark.getId()).isEmpty()) {
      throw new BookmarkNotFoundException("Bookmark not found with id: " + bookmark.getId());
    }
    validateUrl(bookmark.getUrl());
    // Check for duplicate URL only if the URL is changed and it's not the current
    // bookmark's URL
    Optional<Bookmark> existingBookmarkWithUrl = bookmarkRepository.findByUrl(bookmark.getUrl());
    if (existingBookmarkWithUrl.isPresent() && !existingBookmarkWithUrl.get().getId().equals(bookmark.getId())) {
      throw new DuplicateUrlException("Bookmark with URL already exists: " + bookmark.getUrl());
    }

    Set<Tag> managedTags = getOrCreateTags(bookmark.getTags());
    bookmark.setTags(managedTags);
    bookmarkRepository.update(bookmark);
    
    // Update bookmark-tag relationships
    bookmarkTagRepository.deleteByBookmarkId(bookmark.getId());
    saveBookmarkTagRelationships(bookmark.getId(), managedTags);
    return bookmark;
  }

  public void delete(Long id) {
    Bookmark bookmark = bookmarkRepository.findById(id)
        .orElseThrow(() -> new BookmarkNotFoundException("Bookmark not found with id: " + id));
    // Delete bookmark-tag relationships first
    bookmarkTagRepository.deleteByBookmarkId(id);
    bookmarkRepository.delete(bookmark);
  }

  public Set<Tag> convertTags(List<String> tagNames) {
    return tagNames.stream().map(Tag::new).collect(Collectors.toSet());
  }

  private void validateUrl(String url) {
    try {
      URI.create(url).toURL();
    } catch (IllegalArgumentException | MalformedURLException e) {
      throw new InvalidUrlException("Invalid URL format: " + url);
    }
  }

  private Set<Tag> getOrCreateTags(Set<Tag> tags) {
    Set<Tag> managedTags = new HashSet<>();
    if (tags != null) {
      for (Tag tag : tags) {
        Optional<Tag> existingTag = tagRepository.findByName(tag.getName());
        Tag managedTag = existingTag.orElseGet(() -> {
          tagRepository.insert(tag);
          return tag;
        });
        managedTags.add(managedTag);
      }
    }
    return managedTags;
  }

  private void saveBookmarkTagRelationships(Long bookmarkId, Set<Tag> tags) {
    for (Tag tag : tags) {
      if (tag.getId() != null) {
        bookmarkTagRepository.insertBookmarkTag(bookmarkId, tag.getId());
      }
    }
  }
}
