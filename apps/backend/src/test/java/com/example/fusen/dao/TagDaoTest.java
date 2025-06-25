package com.example.fusen.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.fusen.BaseTest;
import com.example.fusen.TestDataBuilder;
import com.example.fusen.entity.Tag;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("TagDao Integration Tests")
class TagDaoTest extends BaseTest {

    @Autowired
    private TagDao tagDao;

    private Tag testTag;

    @BeforeEach
    void setUp() {
        testTag = TestDataBuilder.tag()
            .name("test-tag-dao")
            .build();
    }

    @Test
    @DisplayName("Should insert tag successfully")
    void shouldInsertTagSuccessfully() {
        // When
        int result = tagDao.insert(testTag);

        // Then
        assertThat(result).isEqualTo(1);
        assertThat(testTag.getId()).isNotNull();
        assertThat(testTag.getCreatedAt()).isNotNull();
        assertThat(testTag.getUpdatedAt()).isNotNull();

        // Verify the tag was actually inserted
        Optional<Tag> inserted = tagDao.findByName("test-tag-dao");
        assertThat(inserted).isPresent();
        assertThat(inserted.get().getName()).isEqualTo("test-tag-dao");
    }

    @Test
    @DisplayName("Should find tag by name when exists")
    void shouldFindTagByNameWhenExists() {
        // Given
        tagDao.insert(testTag);

        // When
        Optional<Tag> result = tagDao.findByName("test-tag-dao");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("test-tag-dao");
        assertThat(result.get().getId()).isEqualTo(testTag.getId());
    }

    @Test
    @DisplayName("Should return empty when tag not found by name")
    void shouldReturnEmptyWhenTagNotFoundByName() {
        // When
        Optional<Tag> result = tagDao.findByName("nonexistent-tag");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle case-sensitive tag name search")
    void shouldHandleCaseSensitiveTagNameSearch() {
        // Given
        tagDao.insert(testTag);

        // When
        Optional<Tag> lowerCase = tagDao.findByName("test-tag-dao");
        Optional<Tag> upperCase = tagDao.findByName("TEST-TAG-DAO");

        // Then
        assertThat(lowerCase).isPresent();
        assertThat(upperCase).isEmpty(); // Assuming case-sensitive behavior
    }

    @Test
    @DisplayName("Should insert multiple tags with different names")
    void shouldInsertMultipleTagsWithDifferentNames() {
        // Given
        Tag tag1 = TestDataBuilder.tag().name("java").build();
        Tag tag2 = TestDataBuilder.tag().name("spring").build();
        Tag tag3 = TestDataBuilder.tag().name("doma").build();

        // When
        tagDao.insert(tag1);
        tagDao.insert(tag2);
        tagDao.insert(tag3);

        // Then
        assertThat(tagDao.findByName("java")).isPresent();
        assertThat(tagDao.findByName("spring")).isPresent();
        assertThat(tagDao.findByName("doma")).isPresent();
    }
}