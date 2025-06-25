package com.example.fusen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import com.example.fusen.controller.BookmarkControllerTest;
import com.example.fusen.dao.BookmarkDaoTest;
import com.example.fusen.dao.BookmarkTagDaoTest;
import com.example.fusen.dao.TagDaoTest;
import com.example.fusen.service.BookmarkServiceTest;
import com.example.fusen.util.GlobalExceptionHandlerTest;

/**
 * Comprehensive test suite for Fusen bookmark management application
 * 
 * This test suite covers:
 * - Service layer unit tests (BookmarkService)
 * - Controller layer integration tests (BookmarkController)
 * - Data access layer tests (BookmarkDao, TagDao, BookmarkTagDao)
 * - Exception handling tests (GlobalExceptionHandler)
 * 
 * Test Infrastructure:
 * - H2 in-memory database for fast unit testing
 * - Spring Boot Test with MockMvc for web layer testing
 * - Mockito for mocking dependencies
 * - AssertJ for fluent assertions
 * - TestDataBuilder for consistent test data creation
 * 
 * Coverage includes:
 * - All CRUD operations for bookmarks and tags
 * - URL validation and duplicate detection
 * - Tag relationship management
 * - Error scenarios and exception handling
 * - Request validation and response formatting
 * - Database operations and data integrity
 */
@Suite
@SelectClasses({
    BookmarkServiceTest.class,
    BookmarkControllerTest.class,
    BookmarkDaoTest.class,
    TagDaoTest.class,
    BookmarkTagDaoTest.class,
    GlobalExceptionHandlerTest.class
})
@DisplayName("Fusen Backend Comprehensive Test Suite")
public class TestSuite {

    @Test
    @DisplayName("Test Suite Documentation")
    void testSuiteInfo() {
        // This test serves as documentation for the test suite
        // All actual tests are in the individual test classes
    }
}