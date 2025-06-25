package com.example.fusen;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base test class providing common test configuration
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseTest {

    @BeforeEach
    void setUp() {
        // Common setup logic for all tests
        setupTestData();
    }

    protected void setupTestData() {
        // Override in subclasses if needed
    }
}