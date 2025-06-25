package com.example.fusen;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.example.fusen.entity.Bookmark;
import com.example.fusen.entity.Tag;

/**
 * Test data builder for creating test entities
 */
public class TestDataBuilder {

    public static class BookmarkBuilder {
        private Long id;
        private String url = "https://example.com";
        private String title = "Example Title";
        private String description = "Example Description";
        private Set<Tag> tags = new HashSet<>();
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public BookmarkBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BookmarkBuilder url(String url) {
            this.url = url;
            return this;
        }

        public BookmarkBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BookmarkBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BookmarkBuilder tags(Set<Tag> tags) {
            this.tags = tags;
            return this;
        }

        public BookmarkBuilder addTag(Tag tag) {
            this.tags.add(tag);
            return this;
        }

        public BookmarkBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public BookmarkBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Bookmark build() {
            return Bookmark.builder()
                .id(id)
                .url(url)
                .title(title)
                .description(description)
                .tags(tags)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
        }
    }

    public static class TagBuilder {
        private Long id;
        private String name = "test-tag";
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();

        public TagBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public TagBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TagBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TagBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Tag build() {
            return Tag.builder()
                .id(id)
                .name(name)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
        }
    }

    public static BookmarkBuilder bookmark() {
        return new BookmarkBuilder();
    }

    public static TagBuilder tag() {
        return new TagBuilder();
    }

    // Predefined test data
    public static Bookmark validBookmark() {
        return bookmark()
            .url("https://example.com")
            .title("Example Title")
            .description("Example Description")
            .build();
    }

    public static Bookmark bookmarkWithTags() {
        Set<Tag> tags = new HashSet<>();
        tags.add(tag().name("java").build());
        tags.add(tag().name("spring").build());
        
        return bookmark()
            .url("https://spring.io")
            .title("Spring Framework")
            .description("Spring Framework Documentation")
            .tags(tags)
            .build();
    }

    public static Tag validTag() {
        return tag().name("test-tag").build();
    }
}