package com.resume.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "groq.api.key=test-key"
})
class ResumeAiBackendApplicationTests {

    @Test
    void contextLoads() {
    }
}