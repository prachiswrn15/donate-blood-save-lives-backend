package com.example.DonateBlood.SaveLives;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:sqlite:./target/context-test.db", "ADMIN_EMAIL=", "ADMIN_PASSWORD="})
class ApplicationTests {

    @Test
    void contextLoads() {
        assertTrue(true); // Dummy assertion to ensure the test is not empty
    }
}
