package com.audition.configuration;

import com.audition.common.logging.AuditionLogger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@SpringBootTest
class RequestLoggingInjectorTest {

    private static final String TEST_URI_STR = "/test-uri";
    @MockBean
    private transient AuditionLogger logger;

    @Autowired
    private transient RequestLoggingInjector requestLoggingInjector;

    private transient HttpServletRequest mockRequest;
    private transient HttpServletResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockRequest = Mockito.mock(HttpServletRequest.class);
        mockResponse = Mockito.mock(HttpServletResponse.class);
    }

    @Test
    void testPreHandle() {
        when(mockRequest.getRequestURI()).thenReturn(TEST_URI_STR);
        when(mockRequest.getMethod()).thenReturn("GET");
        when(mockRequest.getQueryString()).thenReturn("param1=value1");
        when(mockRequest.getRemoteUser()).thenReturn("user123");

        requestLoggingInjector.preHandle(mockRequest, mockResponse, new Object());

        final String expectedLogMessage = new StringBuilder()
            .append("Request URI: ").append(TEST_URI_STR)
            .append("\nRequest Method: ").append("GET")
            .append("\nRequest Query String: ").append("param1=value1")
            .append("\nRequest Remote User: ").append("user123")
            .toString();

        verify(logger, times(1)).info(any(Logger.class), eq(expectedLogMessage));
    }

    @Test
    void testAfterCompletion() {
        when(mockRequest.getRequestURI()).thenReturn("/test-uri");
        when(mockResponse.getStatus()).thenReturn(200);

        requestLoggingInjector.afterCompletion(mockRequest, mockResponse, new Object(), null);

        verify(logger, times(1)).info(any(Logger.class), eq("Response Status: {}"), eq(200));
    }

    @Test
    void testAfterCompletionWithException() {
        final Exception ex = new RuntimeException("Test exception");
        when(mockRequest.getRequestURI()).thenReturn("/test-uri");

        requestLoggingInjector.afterCompletion(mockRequest, mockResponse, new Object(), ex);

        verify(logger, times(1)).logErrorWithException(any(Logger.class), eq("Request to /test-uri resulted in an exception"), eq(ex));
    }
}