package com.example.fusen.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.seasar.doma.jdbc.SelectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.fusen.BaseTest;
import com.example.fusen.TestDataBuilder;
import com.example.fusen.entity.Bookmark;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("BookmarkDao Integration Tests")
class BookmarkDaoTest extends BaseTest {

    @Autowired
    private BookmarkDao bookmarkDao;

    private Bookmark testBookmark;

    @BeforeEach
    void setUp() {
        testBookmark = TestDataBuilder.bookmark()
            .url("https://test-dao.com")
            .title("DAO Test Bookmark")
            .description("Test bookmark for DAO layer")
            .build();
    }

    @Test
    @DisplayName("Should find all bookmarks with pagination")
    void shouldFindAllBookmarksWithPagination() {
        // Given
        bookmarkDao.insert(testBookmark);
        Bookmark bookmark2 = TestDataBuilder.bookmark()
            .url("https://test-dao-2.com")
            .title("DAO Test Bookmark 2")
            .build();
        bookmarkDao.insert(bookmark2);

        SelectOptions options = SelectOptions.get().limit(1).offset(0);

        // When
        List<Bookmark> result = bookmarkDao.findAll(options);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUrl()).isIn("https://test-dao.com", "https://test-dao-2.com");
    }

    @Test
    @DisplayName("Should find bookmark by ID when exists")
    void shouldFindBookmarkByIdWhenExists() {
        // Given
        bookmarkDao.insert(testBookmark);
        Long bookmarkId = testBookmark.getId();

        // When
        Optional<Bookmark> result = bookmarkDao.findById(bookmarkId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(bookmarkId);
        assertThat(result.get().getUrl()).isEqualTo("https://test-dao.com");
        assertThat(result.get().getTitle()).isEqualTo("DAO Test Bookmark");
        assertThat(result.get().getDescription()).isEqualTo("Test bookmark for DAO layer");
    }

    @Test
    @DisplayName("Should return empty when bookmark not found by ID")
    void shouldReturnEmptyWhenBookmarkNotFoundById() {
        // When
        Optional<Bookmark> result = bookmarkDao.findById(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should check if bookmark exists by URL")
    void shouldCheckIfBookmarkExistsByUrl() {
        // Given
        bookmarkDao.insert(testBookmark);

        // When
        boolean exists = bookmarkDao.existsByUrl("https://test-dao.com");
        boolean notExists = bookmarkDao.existsByUrl("https://nonexistent.com");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should insert bookmark successfully")
    void shouldInsertBookmarkSuccessfully() {
        // When
        int result = bookmarkDao.insert(testBookmark);

        // Then
        assertThat(result).isEqualTo(1);
        assertThat(testBookmark.getId()).isNotNull();
        assertThat(testBookmark.getCreatedAt()).isNotNull();
        assertThat(testBookmark.getUpdatedAt()).isNotNull();

        // Verify the bookmark was actually inserted
        Optional<Bookmark> inserted = bookmarkDao.findById(testBookmark.getId());
        assertThat(inserted).isPresent();
        assertThat(inserted.get().getUrl()).isEqualTo("https://test-dao.com");
    }

    @Test
    @DisplayName("Should update bookmark successfully")
    void shouldUpdateBookmarkSuccessfully() {
        // Given
        bookmarkDao.insert(testBookmark);
        Long bookmarkId = testBookmark.getId();

        // Modify the bookmark
        testBookmark.setUrl("https://updated-dao.com");
        testBookmark.setTitle("Updated DAO Test Bookmark");
        testBookmark.setDescription("Updated description");

        // When
        int result = bookmarkDao.update(testBookmark);

        // Then
        assertThat(result).isEqualTo(1);

        // Verify the bookmark was actually updated
        Optional<Bookmark> updated = bookmarkDao.findById(bookmarkId);
        assertThat(updated).isPresent();
        assertThat(updated.get().getUrl()).isEqualTo("https://updated-dao.com");
        assertThat(updated.get().getTitle()).isEqualTo("Updated DAO Test Bookmark");
        assertThat(updated.get().getDescription()).isEqualTo("Updated description");
    }

    @Test
    @DisplayName("Should delete bookmark successfully")
    void shouldDeleteBookmarkSuccessfully() {
        // Given
        bookmarkDao.insert(testBookmark);
        Long bookmarkId = testBookmark.getId();

        // Verify bookmark exists before deletion
        assertThat(bookmarkDao.findById(bookmarkId)).isPresent();

        // When
        int result = bookmarkDao.delete(testBookmark);

        // Then
        assertThat(result).isEqualTo(1);

        // Verify the bookmark was actually deleted
        assertThat(bookmarkDao.findById(bookmarkId)).isEmpty();
    }

    @Test
    @DisplayName("Should find bookmark by URL when exists")
    void shouldFindBookmarkByUrlWhenExists() {
        // Given
        bookmarkDao.insert(testBookmark);

        // When
        Optional<Bookmark> result = bookmarkDao.findByUrl("https://test-dao.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUrl()).isEqualTo("https://test-dao.com");
        assertThat(result.get().getTitle()).isEqualTo("DAO Test Bookmark");
    }

    @Test
    @DisplayName("Should return empty when bookmark not found by URL")
    void shouldReturnEmptyWhenBookmarkNotFoundByUrl() {
        // When
        Optional<Bookmark> result = bookmarkDao.findByUrl("https://nonexistent.com");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle empty results with pagination")
    void shouldHandleEmptyResultsWithPagination() {
        // Given
        SelectOptions options = SelectOptions.get().limit(10).offset(100);

        // When
        List<Bookmark> result = bookmarkDao.findAll(options);

        // Then
        assertThat(result).isEmpty();
    }
}