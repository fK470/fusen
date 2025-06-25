package com.example.fusen.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.seasar.doma.jdbc.SelectOptions;

import com.example.fusen.TestDataBuilder;
import com.example.fusen.dao.BookmarkDao;
import com.example.fusen.dao.BookmarkTagDao;
import com.example.fusen.dao.TagDao;
import com.example.fusen.entity.Bookmark;
import com.example.fusen.entity.Tag;
import com.example.fusen.exception.BookmarkNotFoundException;
import com.example.fusen.exception.DuplicateUrlException;
import com.example.fusen.exception.InvalidUrlException;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookmarkService Tests")
class BookmarkServiceTest {

    @Mock
    private BookmarkDao bookmarkDao;

    @Mock
    private TagDao tagDao;

    @Mock
    private BookmarkTagDao bookmarkTagDao;

    @InjectMocks
    private BookmarkService bookmarkService;

    private Bookmark testBookmark;
    private Tag testTag;

    @BeforeEach
    void setUp() {
        testBookmark = TestDataBuilder.validBookmark();
        testTag = TestDataBuilder.validTag();
    }

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("Should return bookmarks with tags")
        void shouldReturnBookmarksWithTags() {
            // Given
            int limit = 10;
            int offset = 0;
            List<Bookmark> bookmarks = Arrays.asList(
                TestDataBuilder.bookmark().id(1L).build(),
                TestDataBuilder.bookmark().id(2L).build()
            );
            List<Tag> tags = Arrays.asList(testTag);

            when(bookmarkDao.findAll(any(SelectOptions.class))).thenReturn(bookmarks);
            when(bookmarkTagDao.findTagsByBookmarkId(anyLong())).thenReturn(tags);

            // When
            List<Bookmark> result = bookmarkService.findAll(limit, offset);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTags()).containsExactly(testTag);
            verify(bookmarkDao).findAll(any(SelectOptions.class));
            verify(bookmarkTagDao, times(2)).findTagsByBookmarkId(anyLong());
        }

        @Test
        @DisplayName("Should handle empty bookmark list")
        void shouldHandleEmptyBookmarkList() {
            // Given
            when(bookmarkDao.findAll(any(SelectOptions.class))).thenReturn(Arrays.asList());

            // When
            List<Bookmark> result = bookmarkService.findAll(10, 0);

            // Then
            assertThat(result).isEmpty();
            verify(bookmarkTagDao, never()).findTagsByBookmarkId(anyLong());
        }
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should return bookmark with tags when found")
        void shouldReturnBookmarkWithTagsWhenFound() {
            // Given
            Long bookmarkId = 1L;
            testBookmark.setId(bookmarkId);
            List<Tag> tags = Arrays.asList(testTag);

            when(bookmarkDao.findById(bookmarkId)).thenReturn(Optional.of(testBookmark));
            when(bookmarkTagDao.findTagsByBookmarkId(bookmarkId)).thenReturn(tags);

            // When
            Bookmark result = bookmarkService.findById(bookmarkId);

            // Then
            assertThat(result).isEqualTo(testBookmark);
            assertThat(result.getTags()).containsExactly(testTag);
            verify(bookmarkDao).findById(bookmarkId);
            verify(bookmarkTagDao).findTagsByBookmarkId(bookmarkId);
        }

        @Test
        @DisplayName("Should throw BookmarkNotFoundException when not found")
        void shouldThrowBookmarkNotFoundExceptionWhenNotFound() {
            // Given
            Long bookmarkId = 999L;
            when(bookmarkDao.findById(bookmarkId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> bookmarkService.findById(bookmarkId))
                .isInstanceOf(BookmarkNotFoundException.class)
                .hasMessageContaining("Bookmark not found with id: " + bookmarkId);

            verify(bookmarkDao).findById(bookmarkId);
            verify(bookmarkTagDao, never()).findTagsByBookmarkId(anyLong());
        }
    }

    @Nested
    @DisplayName("create Tests")
    class CreateTests {

        @Test
        @DisplayName("Should create bookmark successfully")
        void shouldCreateBookmarkSuccessfully() {
            // Given
            Set<Tag> tags = new HashSet<>(Arrays.asList(testTag));
            testBookmark.setTags(tags);

            when(bookmarkDao.existsByUrl(testBookmark.getUrl())).thenReturn(false);
            when(tagDao.findByName(testTag.getName())).thenReturn(Optional.of(testTag));

            // When
            Bookmark result = bookmarkService.create(testBookmark);

            // Then
            assertThat(result).isEqualTo(testBookmark);
            verify(bookmarkDao).existsByUrl(testBookmark.getUrl());
            verify(bookmarkDao).insert(testBookmark);
            verify(bookmarkTagDao).insertBookmarkTag(testBookmark.getId(), testTag.getId());
        }

        @Test
        @DisplayName("Should create new tags when they don't exist")
        void shouldCreateNewTagsWhenTheyDontExist() {
            // Given
            Set<Tag> tags = new HashSet<>(Arrays.asList(testTag));
            testBookmark.setTags(tags);

            when(bookmarkDao.existsByUrl(testBookmark.getUrl())).thenReturn(false);
            when(tagDao.findByName(testTag.getName())).thenReturn(Optional.empty());

            // When
            bookmarkService.create(testBookmark);

            // Then
            verify(tagDao).insert(testTag);
        }

        @Test
        @DisplayName("Should throw DuplicateUrlException when URL already exists")
        void shouldThrowDuplicateUrlExceptionWhenUrlAlreadyExists() {
            // Given
            when(bookmarkDao.existsByUrl(testBookmark.getUrl())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> bookmarkService.create(testBookmark))
                .isInstanceOf(DuplicateUrlException.class)
                .hasMessageContaining("Bookmark with URL already exists: " + testBookmark.getUrl());

            verify(bookmarkDao).existsByUrl(testBookmark.getUrl());
            verify(bookmarkDao, never()).insert(any(Bookmark.class));
        }

        @Test
        @DisplayName("Should throw InvalidUrlException for invalid URL")
        void shouldThrowInvalidUrlExceptionForInvalidUrl() {
            // Given
            testBookmark.setUrl("invalid-url");

            // When & Then
            assertThatThrownBy(() -> bookmarkService.create(testBookmark))
                .isInstanceOf(InvalidUrlException.class)
                .hasMessageContaining("Invalid URL format: invalid-url");

            verify(bookmarkDao, never()).existsByUrl(anyString());
            verify(bookmarkDao, never()).insert(any(Bookmark.class));
        }
    }

    @Nested
    @DisplayName("update Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update bookmark successfully")
        void shouldUpdateBookmarkSuccessfully() {
            // Given
            Long bookmarkId = 1L;
            testBookmark.setId(bookmarkId);
            Set<Tag> tags = new HashSet<>(Arrays.asList(testTag));
            testBookmark.setTags(tags);

            when(bookmarkDao.findById(bookmarkId)).thenReturn(Optional.of(testBookmark));
            when(bookmarkDao.findByUrl(testBookmark.getUrl())).thenReturn(Optional.empty());
            when(tagDao.findByName(testTag.getName())).thenReturn(Optional.of(testTag));

            // When
            Bookmark result = bookmarkService.update(testBookmark);

            // Then
            assertThat(result).isEqualTo(testBookmark);
            verify(bookmarkDao).findById(bookmarkId);
            verify(bookmarkDao).update(testBookmark);
            verify(bookmarkTagDao).deleteByBookmarkId(bookmarkId);
            verify(bookmarkTagDao).insertBookmarkTag(bookmarkId, testTag.getId());
        }

        @Test
        @DisplayName("Should throw BookmarkNotFoundException when bookmark not found")
        void shouldThrowBookmarkNotFoundExceptionWhenBookmarkNotFound() {
            // Given
            Long bookmarkId = 999L;
            testBookmark.setId(bookmarkId);

            when(bookmarkDao.findById(bookmarkId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> bookmarkService.update(testBookmark))
                .isInstanceOf(BookmarkNotFoundException.class)
                .hasMessageContaining("Bookmark not found with id: " + bookmarkId);

            verify(bookmarkDao).findById(bookmarkId);
            verify(bookmarkDao, never()).update(any(Bookmark.class));
        }

        @Test
        @DisplayName("Should throw DuplicateUrlException when URL exists for different bookmark")
        void shouldThrowDuplicateUrlExceptionWhenUrlExistsForDifferentBookmark() {
            // Given
            Long bookmarkId = 1L;
            Long existingBookmarkId = 2L;
            testBookmark.setId(bookmarkId);
            Bookmark existingBookmark = TestDataBuilder.bookmark().id(existingBookmarkId).build();

            when(bookmarkDao.findById(bookmarkId)).thenReturn(Optional.of(testBookmark));
            when(bookmarkDao.findByUrl(testBookmark.getUrl())).thenReturn(Optional.of(existingBookmark));

            // When & Then
            assertThatThrownBy(() -> bookmarkService.update(testBookmark))
                .isInstanceOf(DuplicateUrlException.class)
                .hasMessageContaining("Bookmark with URL already exists: " + testBookmark.getUrl());

            verify(bookmarkDao, never()).update(any(Bookmark.class));
        }
    }

    @Nested
    @DisplayName("delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete bookmark successfully")
        void shouldDeleteBookmarkSuccessfully() {
            // Given
            Long bookmarkId = 1L;
            testBookmark.setId(bookmarkId);

            when(bookmarkDao.findById(bookmarkId)).thenReturn(Optional.of(testBookmark));

            // When
            bookmarkService.delete(bookmarkId);

            // Then
            verify(bookmarkDao).findById(bookmarkId);
            verify(bookmarkTagDao).deleteByBookmarkId(bookmarkId);
            verify(bookmarkDao).delete(testBookmark);
        }

        @Test
        @DisplayName("Should throw BookmarkNotFoundException when bookmark not found")
        void shouldThrowBookmarkNotFoundExceptionWhenBookmarkNotFound() {
            // Given
            Long bookmarkId = 999L;

            when(bookmarkDao.findById(bookmarkId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> bookmarkService.delete(bookmarkId))
                .isInstanceOf(BookmarkNotFoundException.class)
                .hasMessageContaining("Bookmark not found with id: " + bookmarkId);

            verify(bookmarkDao).findById(bookmarkId);
            verify(bookmarkTagDao, never()).deleteByBookmarkId(anyLong());
            verify(bookmarkDao, never()).delete(any(Bookmark.class));
        }
    }

    @Nested
    @DisplayName("convertTags Tests")
    class ConvertTagsTests {

        @Test
        @DisplayName("Should convert valid tag names to Tag entities")
        void shouldConvertValidTagNamesToTagEntities() {
            // Given
            List<String> tagNames = Arrays.asList("java", "spring", "test");

            // When
            Set<Tag> result = bookmarkService.convertTags(tagNames);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).extracting(Tag::getName)
                .containsExactlyInAnyOrder("java", "spring", "test");
        }

        @Test
        @DisplayName("Should return empty set for null input")
        void shouldReturnEmptySetForNullInput() {
            // When
            Set<Tag> result = bookmarkService.convertTags(null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty set for empty list")
        void shouldReturnEmptySetForEmptyList() {
            // When
            Set<Tag> result = bookmarkService.convertTags(Arrays.asList());

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should filter out null and empty tag names")
        void shouldFilterOutNullAndEmptyTagNames() {
            // Given
            List<String> tagNames = Arrays.asList("java", null, "", "   ", "spring");

            // When
            Set<Tag> result = bookmarkService.convertTags(tagNames);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Tag::getName)
                .containsExactlyInAnyOrder("java", "spring");
        }
    }
}