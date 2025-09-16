package com.app.usochicamochabackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UsochicamochaBackendApplicationTests {

    @Test
    void contextLoads() {
        // Test that the Spring context loads successfully
    }

    @Test
    @DirtiesContext
    void applicationStartsSuccessfully() {
        // Test that the application starts without errors
        // This is an integration test that verifies all beans are properly configured
    }
}
