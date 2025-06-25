package com.example.fusen.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.fusen.TestDataBuilder;
import com.example.fusen.entity.Bookmark;
import com.example.fusen.entity.Tag;
import com.example.fusen.exception.BookmarkNotFoundException;
import com.example.fusen.exception.DuplicateUrlException;
import com.example.fusen.exception.InvalidUrlException;
import com.example.fusen.service.BookmarkService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(BookmarkController.class)
@ActiveProfiles("test")
@DisplayName("BookmarkController Integration Tests")
class BookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookmarkService bookmarkService;

    @Autowired
    private ObjectMapper objectMapper;

    private Bookmark testBookmark;
    private Tag testTag;

    @BeforeEach
    void setUp() {
        testTag = TestDataBuilder.tag().id(1L).name("java").build();
        Set<Tag> tags = new HashSet<>();
        tags.add(testTag);
        
        testBookmark = TestDataBuilder.bookmark()
            .id(1L)
            .url("https://example.com")
            .title("Test Title")
            .description("Test Description")
            .tags(tags)
            .createdAt(LocalDateTime.of(2023, 1, 1, 10, 0, 0))
            .updatedAt(LocalDateTime.of(2023, 1, 1, 10, 0, 0))
            .build();
    }

    @Nested
    @DisplayName("GET /api/v1/bookmarks Tests")
    class GetAllBookmarksTests {

        @Test
        @DisplayName("Should return all bookmarks with default pagination")
        void shouldReturnAllBookmarksWithDefaultPagination() throws Exception {
            // Given
            List<Bookmark> bookmarks = Arrays.asList(testBookmark);
            when(bookmarkService.findAll(10, 0)).thenReturn(bookmarks);

            // When & Then
            mockMvc.perform(get("/api/v1/bookmarks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].url", is("https://example.com")))
                .andExpect(jsonPath("$[0].title", is("Test Title")))
                .andExpect(jsonPath("$[0].description", is("Test Description")))
                .andExpect(jsonPath("$[0].tags", hasSize(1)))
                .andExpect(jsonPath("$[0].tags[0]", is("java")))
                .andExpect(jsonPath("$[0].createdAt", is("2023-01-01T10:00:00Z")))
                .andExpect(jsonPath("$[0].updatedAt", is("2023-01-01T10:00:00Z")));

            verify(bookmarkService).findAll(10, 0);
        }

        @Test
        @DisplayName("Should return bookmarks with custom pagination")
        void shouldReturnBookmarksWithCustomPagination() throws Exception {
            // Given
            when(bookmarkService.findAll(5, 10)).thenReturn(Arrays.asList());

            // When & Then
            mockMvc.perform(get("/api/v1/bookmarks")
                .param("limit", "5")
                .param("offset", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

            verify(bookmarkService).findAll(5, 10);
        }

        @Test
        @DisplayName("Should return empty list when no bookmarks exist")
        void shouldReturnEmptyListWhenNoBookmarksExist() throws Exception {
            // Given
            when(bookmarkService.findAll(anyInt(), anyInt())).thenReturn(Arrays.asList());

            // When & Then
            mockMvc.perform(get("/api/v1/bookmarks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/bookmarks/{id} Tests")
    class GetBookmarkByIdTests {

        @Test
        @DisplayName("Should return bookmark when found")
        void shouldReturnBookmarkWhenFound() throws Exception {
            // Given
            when(bookmarkService.findById(1L)).thenReturn(testBookmark);

            // When & Then
            mockMvc.perform(get("/api/v1/bookmarks/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.url", is("https://example.com")))
                .andExpect(jsonPath("$.title", is("Test Title")))
                .andExpect(jsonPath("$.description", is("Test Description")))
                .andExpect(jsonPath("$.tags", hasSize(1)))
                .andExpect(jsonPath("$.tags[0]", is("java")));

            verify(bookmarkService).findById(1L);
        }

        @Test
        @DisplayName("Should return 404 when bookmark not found")
        void shouldReturn404WhenBookmarkNotFound() throws Exception {
            // Given
            when(bookmarkService.findById(999L))
                .thenThrow(new BookmarkNotFoundException("Bookmark not found with id: 999"));

            // When & Then
            mockMvc.perform(get("/api/v1/bookmarks/999"))
                .andExpect(status().isNotFound());

            verify(bookmarkService).findById(999L);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/bookmarks Tests")
    class CreateBookmarkTests {

        @Test
        @DisplayName("Should create bookmark successfully")
        void shouldCreateBookmarkSuccessfully() throws Exception {
            // Given
            String requestJson = """
                {
                    "url": "https://example.com",
                    "title": "Test Title",
                    "description": "Test Description",
                    "tags": ["java", "spring"]
                }
                """;

            Set<Tag> convertedTags = new HashSet<>();
            convertedTags.add(TestDataBuilder.tag().name("java").build());
            convertedTags.add(TestDataBuilder.tag().name("spring").build());

            when(bookmarkService.convertTags(Arrays.asList("java", "spring"))).thenReturn(convertedTags);
            when(bookmarkService.create(any(Bookmark.class))).thenReturn(testBookmark);

            // When & Then
            mockMvc.perform(post("/api/v1/bookmarks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.url", is("https://example.com")))
                .andExpect(jsonPath("$.title", is("Test Title")))
                .andExpect(jsonPath("$.description", is("Test Description")));

            verify(bookmarkService).convertTags(Arrays.asList("java", "spring"));
            verify(bookmarkService).create(any(Bookmark.class));
        }

        @Test
        @DisplayName("Should return 400 for invalid URL format")
        void shouldReturn400ForInvalidUrlFormat() throws Exception {
            // Given
            String requestJson = """
                {
                    "url": "invalid-url",
                    "title": "Test Title"
                }
                """;

            // When & Then
            mockMvc.perform(post("/api/v1/bookmarks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for missing required fields")
        void shouldReturn400ForMissingRequiredFields() throws Exception {
            // Given
            String requestJson = """
                {
                    "title": "Test Title"
                }
                """;

            // When & Then
            mockMvc.perform(post("/api/v1/bookmarks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 409 for duplicate URL")
        void shouldReturn409ForDuplicateUrl() throws Exception {
            // Given
            String requestJson = """
                {
                    "url": "https://example.com",
                    "title": "Test Title"
                }
                """;

            when(bookmarkService.convertTags(any())).thenReturn(new HashSet<>());
            when(bookmarkService.create(any(Bookmark.class)))
                .thenThrow(new DuplicateUrlException("Bookmark with URL already exists"));

            // When & Then
            mockMvc.perform(post("/api/v1/bookmarks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 400 for invalid URL in service layer")
        void shouldReturn400ForInvalidUrlInServiceLayer() throws Exception {
            // Given
            String requestJson = """
                {
                    "url": "https://invalid-url-format",
                    "title": "Test Title"
                }
                """;

            when(bookmarkService.convertTags(any())).thenReturn(new HashSet<>());
            when(bookmarkService.create(any(Bookmark.class)))
                .thenThrow(new InvalidUrlException("Invalid URL format"));

            // When & Then
            mockMvc.perform(post("/api/v1/bookmarks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/bookmarks/{id} Tests")
    class UpdateBookmarkTests {

        @Test
        @DisplayName("Should update bookmark successfully")
        void shouldUpdateBookmarkSuccessfully() throws Exception {
            // Given
            String requestJson = """
                {
                    "url": "https://updated-example.com",
                    "title": "Updated Title",
                    "description": "Updated Description",
                    "tags": ["java", "updated"]
                }
                """;

            Bookmark updatedBookmark = TestDataBuilder.bookmark()
                .id(1L)
                .url("https://updated-example.com")
                .title("Updated Title")
                .description("Updated Description")
                .build();

            Set<Tag> convertedTags = new HashSet<>();
            convertedTags.add(TestDataBuilder.tag().name("java").build());
            convertedTags.add(TestDataBuilder.tag().name("updated").build());

            when(bookmarkService.findById(1L)).thenReturn(testBookmark);
            when(bookmarkService.convertTags(Arrays.asList("java", "updated"))).thenReturn(convertedTags);
            when(bookmarkService.update(any(Bookmark.class))).thenReturn(updatedBookmark);

            // When & Then
            mockMvc.perform(put("/api/v1/bookmarks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.url", is("https://updated-example.com")))
                .andExpect(jsonPath("$.title", is("Updated Title")))
                .andExpect(jsonPath("$.description", is("Updated Description")));

            verify(bookmarkService).findById(1L);
            verify(bookmarkService).convertTags(Arrays.asList("java", "updated"));
            verify(bookmarkService).update(any(Bookmark.class));
        }

        @Test
        @DisplayName("Should return 404 when bookmark not found for update")
        void shouldReturn404WhenBookmarkNotFoundForUpdate() throws Exception {
            // Given
            String requestJson = """
                {
                    "url": "https://example.com",
                    "title": "Test Title"
                }
                """;

            when(bookmarkService.findById(999L))
                .thenThrow(new BookmarkNotFoundException("Bookmark not found with id: 999"));

            // When & Then
            mockMvc.perform(put("/api/v1/bookmarks/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isNotFound());

            verify(bookmarkService).findById(999L);
        }

        @Test
        @DisplayName("Should return 400 for invalid request body")
        void shouldReturn400ForInvalidRequestBody() throws Exception {
            // Given
            String requestJson = """
                {
                    "url": "invalid-url",
                    "title": "Test Title"
                }
                """;

            // When & Then
            mockMvc.perform(put("/api/v1/bookmarks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/bookmarks/{id} Tests")
    class DeleteBookmarkTests {

        @Test
        @DisplayName("Should delete bookmark successfully")
        void shouldDeleteBookmarkSuccessfully() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/v1/bookmarks/1"))
                .andExpect(status().isNoContent());

            verify(bookmarkService).delete(1L);
        }

        @Test
        @DisplayName("Should return 404 when bookmark not found for deletion")
        void shouldReturn404WhenBookmarkNotFoundForDeletion() throws Exception {
            // Given
            doThrow(new BookmarkNotFoundException("Bookmark not found with id: 999"))
                .when(bookmarkService).delete(999L);

            // When & Then
            mockMvc.perform(delete("/api/v1/bookmarks/999"))
                .andExpect(status().isNotFound());

            verify(bookmarkService).delete(999L);
        }
    }
}