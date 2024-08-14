package com.audition;

import com.audition.service.AuditionService;
import com.audition.web.AuditionController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.core.env.Environment;


@SpringBootTest
class AuditionApplicationTests {

    @Autowired
    private AuditionService auditionService;

    @Autowired
    private AuditionController auditionController;

    @Autowired
    private Environment env;

    @Test
    void contextLoads() {
        assertThat(auditionService).isNotNull();
        assertThat(auditionController).isNotNull();
    }

    @Test
    void testPropertyLoading() {
        // Verify that the spring.application.name property is correctly loaded
        String expectedValue = "audition-api";
        String actualValue = env.getProperty("spring.application.name");
        assertThat(actualValue).isEqualTo(expectedValue);
    }
}
