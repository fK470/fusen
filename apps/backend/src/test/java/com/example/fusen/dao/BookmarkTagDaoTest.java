package com.example.fusen.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.fusen.BaseTest;
import com.example.fusen.TestDataBuilder;
import com.example.fusen.entity.Bookmark;
import com.example.fusen.entity.Tag;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("BookmarkTagDao Integration Tests")
class BookmarkTagDaoTest extends BaseTest {

    @Autowired
    private BookmarkDao bookmarkDao;

    @Autowired
    private TagDao tagDao;

    @Autowired
    private BookmarkTagDao bookmarkTagDao;

    private Bookmark testBookmark;
    private Tag testTag1;
    private Tag testTag2;

    @BeforeEach
    void setUp() {
        // Create test bookmark
        testBookmark = TestDataBuilder.bookmark()
            .url("https://test-bookmark-tag.com")
            .title("Bookmark Tag Test")
            .description("Test for bookmark-tag relationships")
            .build();
        bookmarkDao.insert(testBookmark);

        // Create test tags
        testTag1 = TestDataBuilder.tag().name("java").build();
        testTag2 = TestDataBuilder.tag().name("spring").build();
        tagDao.insert(testTag1);
        tagDao.insert(testTag2);
    }

    @Test
    @DisplayName("Should insert bookmark-tag relationship successfully")
    void shouldInsertBookmarkTagRelationshipSuccessfully() {
        // When
        int result = bookmarkTagDao.insertBookmarkTag(testBookmark.getId(), testTag1.getId());

        // Then
        assertThat(result).isEqualTo(1);

        // Verify the relationship was created
        List<Tag> tags = bookmarkTagDao.findTagsByBookmarkId(testBookmark.getId());
        assertThat(tags).hasSize(1);
        assertThat(tags.get(0).getName()).isEqualTo("java");
    }

    @Test
    @DisplayName("Should find tags by bookmark ID")
    void shouldFindTagsByBookmarkId() {
        // Given
        bookmarkTagDao.insertBookmarkTag(testBookmark.getId(), testTag1.getId());
        bookmarkTagDao.insertBookmarkTag(testBookmark.getId(), testTag2.getId());

        // When
        List<Tag> result = bookmarkTagDao.findTagsByBookmarkId(testBookmark.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Tag::getName)
            .containsExactlyInAnyOrder("java", "spring");
        assertThat(result).extracting(Tag::getId)
            .containsExactlyInAnyOrder(testTag1.getId(), testTag2.getId());
    }

    @Test
    @DisplayName("Should return empty list when no tags associated with bookmark")
    void shouldReturnEmptyListWhenNoTagsAssociatedWithBookmark() {
        // When
        List<Tag> result = bookmarkTagDao.findTagsByBookmarkId(testBookmark.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list for non-existent bookmark ID")
    void shouldReturnEmptyListForNonExistentBookmarkId() {
        // When
        List<Tag> result = bookmarkTagDao.findTagsByBookmarkId(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should delete all bookmark-tag relationships by bookmark ID")
    void shouldDeleteAllBookmarkTagRelationshipsByBookmarkId() {
        // Given
        bookmarkTagDao.insertBookmarkTag(testBookmark.getId(), testTag1.getId());
        bookmarkTagDao.insertBookmarkTag(testBookmark.getId(), testTag2.getId());

        // Verify relationships exist
        List<Tag> tagsBeforeDelete = bookmarkTagDao.findTagsByBookmarkId(testBookmark.getId());
        assertThat(tagsBeforeDelete).hasSize(2);

        // When
        int result = bookmarkTagDao.deleteByBookmarkId(testBookmark.getId());

        // Then
        assertThat(result).isEqualTo(2); // Two relationships were deleted

        // Verify relationships were deleted
        List<Tag> tagsAfterDelete = bookmarkTagDao.findTagsByBookmarkId(testBookmark.getId());
        assertThat(tagsAfterDelete).isEmpty();
    }

    @Test
    @DisplayName("Should return 0 when deleting non-existent bookmark-tag relationships")
    void shouldReturn0WhenDeletingNonExistentBookmarkTagRelationships() {
        // When
        int result = bookmarkTagDao.deleteByBookmarkId(999L);

        // Then
        assertThat(result).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle multiple bookmarks with different tag sets")
    void shouldHandleMultipleBookmarksWithDifferentTagSets() {
        // Given
        Bookmark bookmark2 = TestDataBuilder.bookmark()
            .url("https://test-bookmark-tag-2.com")
            .title("Second Bookmark")
            .build();
        bookmarkDao.insert(bookmark2);

        Tag tag3 = TestDataBuilder.tag().name("test").build();
        tagDao.insert(tag3);

        // Bookmark 1 has tags: java, spring
        bookmarkTagDao.insertBookmarkTag(testBookmark.getId(), testTag1.getId());
        bookmarkTagDao.insertBookmarkTag(testBookmark.getId(), testTag2.getId());

        // Bookmark 2 has tags: java, test
        bookmarkTagDao.insertBookmarkTag(bookmark2.getId(), testTag1.getId());
        bookmarkTagDao.insertBookmarkTag(bookmark2.getId(), tag3.getId());

        // When
        List<Tag> bookmark1Tags = bookmarkTagDao.findTagsByBookmarkId(testBookmark.getId());
        List<Tag> bookmark2Tags = bookmarkTagDao.findTagsByBookmarkId(bookmark2.getId());

        // Then
        assertThat(bookmark1Tags).hasSize(2);
        assertThat(bookmark1Tags).extracting(Tag::getName)
            .containsExactlyInAnyOrder("java", "spring");

        assertThat(bookmark2Tags).hasSize(2);
        assertThat(bookmark2Tags).extracting(Tag::getName)
            .containsExactlyInAnyOrder("java", "test");
    }

    @Test
    @DisplayName("Should maintain tag order consistency")
    void shouldMaintainTagOrderConsistency() {
        // Given
        bookmarkTagDao.insertBookmarkTag(testBookmark.getId(), testTag1.getId());
        bookmarkTagDao.insertBookmarkTag(testBookmark.getId(), testTag2.getId());

        // When - retrieve tags multiple times
        List<Tag> result1 = bookmarkTagDao.findTagsByBookmarkId(testBookmark.getId());
        List<Tag> result2 = bookmarkTagDao.findTagsByBookmarkId(testBookmark.getId());

        // Then - results should be consistent
        assertThat(result1).hasSize(2);
        assertThat(result2).hasSize(2);
        assertThat(result1).extracting(Tag::getName)
            .containsExactlyInAnyOrderElementsOf(
                result2.stream().map(Tag::getName).toList()
            );
    }
}