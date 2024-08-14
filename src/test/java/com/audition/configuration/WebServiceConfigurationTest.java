package com.audition.configuration;

import com.audition.common.logging.AuditionLogger;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.text.SimpleDateFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class WebServiceConfigurationTest {

    @Autowired
    private WebServiceConfiguration webServiceConfiguration;

    @MockBean
    private ResponseHeaderInjector responseHeaderInjector;

    @MockBean
    private RequestLoggingInjector requestLoggingInjector;

    @MockBean
    private ResponseMetricsInjector responseMetricsInjector;

    @MockBean
    private AuditionLogger logger;

    @BeforeEach
    void setUp() {
        //webServiceConfiguration.logger = logger;
    }

    @Test
    void testAddInterceptors() {
        InterceptorRegistry mockRegistry = Mockito.mock(InterceptorRegistry.class);

        webServiceConfiguration.addInterceptors(mockRegistry);

        verify(mockRegistry, times(1)).addInterceptor(responseHeaderInjector);
        verify(mockRegistry, times(1)).addInterceptor(requestLoggingInjector);
        verify(mockRegistry, times(1)).addInterceptor(responseMetricsInjector);
    }

    @Test
    void testObjectMapper() {
        ObjectMapper objectMapper = webServiceConfiguration.objectMapper();

        assertNotNull(objectMapper);
        assertEquals("yyyy-MM-dd", ((SimpleDateFormat) objectMapper.getDateFormat()).toPattern());
        assertFalse(objectMapper.getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertEquals(PropertyNamingStrategies.LOWER_CAMEL_CASE, objectMapper.getPropertyNamingStrategy());
        assertFalse(objectMapper.getSerializationConfig().isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    @Test
    void testRestTemplate() {
        RestTemplate restTemplate = webServiceConfiguration.restTemplate();

        assertNotNull(restTemplate);
        assertTrue(restTemplate.getMessageConverters().get(0) instanceof MappingJackson2HttpMessageConverter);
        assertTrue(restTemplate.getInterceptors().get(1) instanceof ClientHttpRequestInterceptor);
        assertEquals(2, restTemplate.getInterceptors().size());
    }

    @Test
    void testLoggingInterceptor() {
        ClientHttpRequestInterceptor interceptor = webServiceConfiguration.loggingInterceptor();
        assertNotNull(interceptor);
    }
}
