package com.app.usochicamochabackend.auth.infrastructure.repository;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.utils.TestDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepositoryJpa userRepository;

    @Test
    void findByUsername_ShouldReturnUser_WhenUserExists() {
        // Given
        UserEntity user = TestDataBuilder.createTestUser();
        user.setId(null); // Let JPA generate the ID
        entityManager.persistAndFlush(user);

        // When
        Optional<UserEntity> foundUser = userRepository.findByUsername("testuser");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
        assertEquals("test@example.com", foundUser.get().getEmail());
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // When
        Optional<UserEntity> foundUser = userRepository.findByUsername("nonexistent");

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    void getUserEntityById_ShouldReturnUser_WhenUserExists() {
        // Given
        UserEntity user = TestDataBuilder.createTestUser();
        user.setId(null); // Let JPA generate the ID
        UserEntity savedUser = entityManager.persistAndFlush(user);

        // When
        UserEntity foundUser = userRepository.getUserEntityById(savedUser.getId());

        // Then
        assertNotNull(foundUser);
        assertEquals(savedUser.getId(), foundUser.getId());
        assertEquals("testuser", foundUser.getUsername());
    }

    @Test
    void save_ShouldPersistUser() {
        // Given
        UserEntity user = TestDataBuilder.createTestUser();
        user.setId(null); // Let JPA generate the ID

        // When
        UserEntity savedUser = userRepository.save(user);

        // Then
        assertNotNull(savedUser.getId());
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("test@example.com", savedUser.getEmail());
        assertTrue(savedUser.getStatus());
    }
}
