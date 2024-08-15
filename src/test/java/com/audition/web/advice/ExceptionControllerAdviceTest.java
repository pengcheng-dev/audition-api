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

import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExceptionControllerAdviceTest {

    private transient MeterRegistry meterRegistry;
    private transient Counter counter;
    private transient AuditionLogger auditionLogger;
    private transient ExceptionControllerAdvice exceptionControllerAdvice;

    @BeforeEach
    void setUp() throws Exception {
        meterRegistry = Mockito.mock(MeterRegistry.class);
        counter = Mockito.mock(Counter.class);
        auditionLogger = Mockito.mock(AuditionLogger.class);

        // Mock the counter to be returned by the meterRegistry
        when(meterRegistry.counter(anyString(), anyString(), anyString())).thenReturn(counter);

        // Instantiate the class under test
        exceptionControllerAdvice = new ExceptionControllerAdvice();

        // Set the private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(exceptionControllerAdvice, "meterRegistry", meterRegistry);
        ReflectionTestUtils.setField(exceptionControllerAdvice, "logger", auditionLogger);
    }

    @Test
    void testHandleHttpClientException() {
        final HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found");

        final ProblemDetail problemDetail = exceptionControllerAdvice.handleHttpClientException(exception);

        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals("404 Not Found", problemDetail.getDetail());

        verify(auditionLogger).logStandardProblemDetail(any(Logger.class), any(ProblemDetail.class), eq(exception));
    }

    @Test
    void testHandleMainException() {
        final Exception exception = new Exception("General error");

        final ProblemDetail problemDetail = exceptionControllerAdvice.handleMainException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.getStatus());
        assertEquals("General error", problemDetail.getDetail());

        verify(meterRegistry).counter(eq("exceptions.main"), eq("exception"), eq("Exception"));
        verify(counter).increment();
    }

    @Test
    void testHandleSystemException() {
        final SystemException exception = new SystemException("System error", "System Error", 500);

        final ProblemDetail problemDetail = exceptionControllerAdvice.handleSystemException(exception);

        assertEquals(500, problemDetail.getStatus());
        assertEquals("System error", problemDetail.getDetail());
        assertEquals("System Error", problemDetail.getTitle());

        verify(meterRegistry).counter(eq("exceptions.system"), eq("exception"), eq("SystemException"));
        verify(counter).increment();
    }
}
