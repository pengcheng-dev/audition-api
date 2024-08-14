package com.audition.web.advice;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.client.HttpClientErrorException;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ExceptionControllerAdviceTest {

    private MeterRegistry meterRegistry;
    private Counter counter;
    private AuditionLogger auditionLogger;
    private ExceptionControllerAdvice exceptionControllerAdvice;
    private Logger mockLogger;

    @BeforeEach
    void setUp() throws Exception {
        meterRegistry = Mockito.mock(MeterRegistry.class);
        counter = Mockito.mock(Counter.class);
        auditionLogger = Mockito.mock(AuditionLogger.class);
        mockLogger = Mockito.mock(Logger.class);

        // Mock the counter to be returned by the meterRegistry
        when(meterRegistry.counter(anyString(), anyString(), anyString())).thenReturn(counter);

        // Instantiate the class under test
        exceptionControllerAdvice = new ExceptionControllerAdvice();

        // Set the private fields using reflection
        Field meterRegistryField = ExceptionControllerAdvice.class.getDeclaredField("meterRegistry");
        meterRegistryField.setAccessible(true);
        meterRegistryField.set(exceptionControllerAdvice, meterRegistry);

        Field loggerField = ExceptionControllerAdvice.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        loggerField.set(exceptionControllerAdvice, auditionLogger);
    }

    @Test
    void testHandleHttpClientException() {
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found");

        ProblemDetail problemDetail = exceptionControllerAdvice.handleHttpClientException(exception);

        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals("404 Not Found", problemDetail.getDetail());

        verify(auditionLogger).logStandardProblemDetail(any(Logger.class), any(ProblemDetail.class), eq(exception));
    }

    @Test
    void testHandleMainException() {
        Exception exception = new Exception("General error");

        ProblemDetail problemDetail = exceptionControllerAdvice.handleMainException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.getStatus());
        assertEquals("General error", problemDetail.getDetail());

        verify(meterRegistry).counter(eq("exceptions.main"), eq("exception"), eq("Exception"));
        verify(counter).increment();
    }

    @Test
    void testHandleSystemException() {
        SystemException exception = new SystemException("System error", "System Error", 500);

        ProblemDetail problemDetail = exceptionControllerAdvice.handleSystemException(exception);

        assertEquals(500, problemDetail.getStatus());
        assertEquals("System error", problemDetail.getDetail());
        assertEquals("System Error", problemDetail.getTitle());

        verify(meterRegistry).counter(eq("exceptions.system"), eq("exception"), eq("SystemException"));
        verify(counter).increment();
    }
}
