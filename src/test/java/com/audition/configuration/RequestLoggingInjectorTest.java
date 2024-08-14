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

import static org.mockito.Mockito.*;

@SpringBootTest
class RequestLoggingInjectorTest {

    @MockBean
    private AuditionLogger logger;

    @Autowired
    private RequestLoggingInjector requestLoggingInjector;

    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockRequest = Mockito.mock(HttpServletRequest.class);
        mockResponse = Mockito.mock(HttpServletResponse.class);
    }

    @Test
    void testPreHandle() {
        when(mockRequest.getRequestURI()).thenReturn("/test-uri");
        when(mockRequest.getMethod()).thenReturn("GET");
        when(mockRequest.getQueryString()).thenReturn("param1=value1");
        when(mockRequest.getRemoteUser()).thenReturn("user123");

        requestLoggingInjector.preHandle(mockRequest, mockResponse, new Object());

        verify(logger, times(1)).info(any(Logger.class), eq("Request URI: {}"), eq("/test-uri"));
        verify(logger, times(1)).info(any(Logger.class), eq("Request Method: {}"), eq("GET"));
        verify(logger, times(1)).info(any(Logger.class), eq("Request Query String: {}"), eq("param1=value1"));
        verify(logger, times(1)).info(any(Logger.class), eq("Request Remote User: {}"), eq("user123"));
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
        Exception ex = new RuntimeException("Test exception");
        when(mockRequest.getRequestURI()).thenReturn("/test-uri");

        requestLoggingInjector.afterCompletion(mockRequest, mockResponse, new Object(), ex);

        verify(logger, times(1)).logErrorWithException(any(Logger.class), eq("Request to /test-uri resulted in an exception"), eq(ex));
    }
}