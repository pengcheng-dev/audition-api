package com.audition.configuration;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@SpringBootTest
class WebServiceConfigurationTest {

    @Autowired
    private transient WebServiceConfiguration webServiceConfiguration;

    @MockBean
    private transient ResponseHeaderInjector responseHeaderInjector;

    @MockBean
    private transient RequestLoggingInjector requestLoggingInjector;

    @MockBean
    private transient ResponseMetricsInjector responseMetricsInjector;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testAddInterceptors() {
        final InterceptorRegistry mockRegistry = Mockito.mock(InterceptorRegistry.class);

        webServiceConfiguration.addInterceptors(mockRegistry);

        verify(mockRegistry, times(1)).addInterceptor(responseHeaderInjector);
        verify(mockRegistry, times(1)).addInterceptor(requestLoggingInjector);
        verify(mockRegistry, times(1)).addInterceptor(responseMetricsInjector);
    }

    @Test
    void testObjectMapper() {
        final ObjectMapper objectMapper = webServiceConfiguration.objectMapper();

        assertNotNull(objectMapper);
        assertEquals("yyyy-MM-dd", ((SimpleDateFormat) objectMapper.getDateFormat()).toPattern());
        assertFalse(objectMapper.getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertEquals(PropertyNamingStrategies.LOWER_CAMEL_CASE, objectMapper.getPropertyNamingStrategy());
        assertFalse(objectMapper.getSerializationConfig().isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    @Test
    void testRestTemplate() {
        final RestTemplate restTemplate = webServiceConfiguration.restTemplate();

        assertNotNull(restTemplate);
        assertTrue(restTemplate.getMessageConverters().get(0) instanceof MappingJackson2HttpMessageConverter);
        assertTrue(restTemplate.getInterceptors().get(1) instanceof ClientHttpRequestInterceptor);
        assertEquals(2, restTemplate.getInterceptors().size());
    }

    @Test
    void testLoggingInterceptor() {
        final ClientHttpRequestInterceptor interceptor = webServiceConfiguration.loggingInterceptor();
        assertNotNull(interceptor);
    }
}
