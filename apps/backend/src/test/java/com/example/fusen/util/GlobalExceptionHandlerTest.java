package com.example.fusen.util;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fusen.exception.BookmarkNotFoundException;
import com.example.fusen.exception.DuplicateUrlException;
import com.example.fusen.exception.InvalidUrlException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@WebMvcTest(controllers = {GlobalExceptionHandlerTest.TestController.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("BookmarkNotFoundException Handling")
    class BookmarkNotFoundExceptionTests {

        @Test
        @DisplayName("Should return 404 with proper error response for BookmarkNotFoundException")
        void shouldReturn404WithProperErrorResponseForBookmarkNotFoundException() throws Exception {
            // When & Then
            mockMvc.perform(get("/test/bookmark-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode", is("BOOKMARK_NOT_FOUND")))
                .andExpect(jsonPath("$.message", is("Test bookmark not found")));
        }
    }

    @Nested
    @DisplayName("DuplicateUrlException Handling")
    class DuplicateUrlExceptionTests {

        @Test
        @DisplayName("Should return 409 with proper error response for DuplicateUrlException")
        void shouldReturn409WithProperErrorResponseForDuplicateUrlException() throws Exception {
            // When & Then
            mockMvc.perform(get("/test/duplicate-url"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode", is("DUPLICATE_URL")))
                .andExpect(jsonPath("$.message", is("Test duplicate URL")));
        }
    }

    @Nested
    @DisplayName("InvalidUrlException Handling")
    class InvalidUrlExceptionTests {

        @Test
        @DisplayName("Should return 400 with proper error response for InvalidUrlException")
        void shouldReturn400WithProperErrorResponseForInvalidUrlException() throws Exception {
            // When & Then
            mockMvc.perform(get("/test/invalid-url"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode", is("INVALID_URL")))
                .andExpect(jsonPath("$.message", is("Test invalid URL")));
        }
    }

    @Nested
    @DisplayName("Validation Exception Handling")
    class ValidationExceptionTests {

        @Test
        @DisplayName("Should return 400 with validation error for invalid request body")
        void shouldReturn400WithValidationErrorForInvalidRequestBody() throws Exception {
            // Given
            String invalidRequestJson = """
                {
                    "name": ""
                }
                """;

            // When & Then
            mockMvc.perform(post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Validation failed")));
        }

        @Test
        @DisplayName("Should return 400 with validation error for missing required field")
        void shouldReturn400WithValidationErrorForMissingRequiredField() throws Exception {
            // Given
            String invalidRequestJson = """
                {
                }
                """;

            // When & Then
            mockMvc.perform(post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_ERROR")));
        }
    }

    @Nested
    @DisplayName("Generic Exception Handling")
    class GenericExceptionTests {

        @Test
        @DisplayName("Should return 500 with proper error response for generic exception")
        void shouldReturn500WithProperErrorResponseForGenericException() throws Exception {
            // When & Then
            mockMvc.perform(get("/test/generic-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode", is("INTERNAL_SERVER_ERROR")))
                .andExpect(jsonPath("$.message", is("An unexpected error occurred: Test generic error")));
        }
    }

    // Test Configuration
    @TestConfiguration
    static class TestConfig {
        @Bean
        public TestController testController() {
            return new TestController();
        }
    }

    // Test Controller to trigger exceptions
    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/bookmark-not-found")
        public void throwBookmarkNotFoundException() {
            throw new BookmarkNotFoundException("Test bookmark not found");
        }

        @GetMapping("/duplicate-url")
        public void throwDuplicateUrlException() {
            throw new DuplicateUrlException("Test duplicate URL");
        }

        @GetMapping("/invalid-url")
        public void throwInvalidUrlException() {
            throw new InvalidUrlException("Test invalid URL");
        }

        @GetMapping("/generic-error")
        public void throwGenericException() {
            throw new RuntimeException("Test generic error");
        }

        @PostMapping("/validation")
        public void testValidation(@Valid @RequestBody TestRequest request) {
            // This method will trigger validation errors
        }
    }

    // Test Request DTO for validation testing
    static class TestRequest {
        @NotBlank(message = "Name is required")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}