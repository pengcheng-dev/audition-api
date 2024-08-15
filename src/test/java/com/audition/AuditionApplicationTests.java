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
    private transient AuditionService auditionService;

    @Autowired
    private transient AuditionController auditionController;

    @Autowired
    private transient Environment env;

    @Test
    void contextLoads() {
        assertThat(auditionService).isNotNull();
        assertThat(auditionController).isNotNull();
    }

    @Test
    void testPropertyLoading() {
        // Verify that the spring.application.name property is correctly loaded
        final String expectedValue = "audition-api";
        final String actualValue = env.getProperty("spring.application.name");
        assertThat(actualValue).isEqualTo(expectedValue);
    }
}
