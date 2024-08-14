package com.audition.web.advice;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = "management.metrics.export.enabled=false")
class ExceptionControllerAdviceTest {

    @Autowired
    private ExceptionControllerAdvice exceptionControllerAdvice;

    @MockBean
    private AuditionLogger logger;

    @MockBean
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testHandleHttpClientException() {
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request");

        ProblemDetail problemDetail = exceptionControllerAdvice.handleHttpClientException(exception);

        assertNotNull(problemDetail);
        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        verify(logger, times(1)).logStandardProblemDetail(any(Logger.class), eq(problemDetail), eq(exception));
    }

    @Test
    void testHandleMainException() {
        Exception exception = new RuntimeException("Unexpected error");

        ProblemDetail problemDetail = exceptionControllerAdvice.handleMainException(exception);

        assertNotNull(problemDetail);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.getStatus());
        verify(logger, times(1)).logErrorWithException(any(Logger.class), anyString(), eq(exception));
        verify(meterRegistry, times(1)).counter("exceptions.main", "exception", exception.getClass().getSimpleName());
    }

    @Test
    void testHandleSystemException() {
        SystemException exception = new SystemException("System error", "System Failure", 500);

        ProblemDetail problemDetail = exceptionControllerAdvice.handleSystemException(exception);

        assertNotNull(problemDetail);
        assertEquals(500, problemDetail.getStatus());
        assertEquals("System Failure", problemDetail.getTitle());
        assertEquals("System error", problemDetail.getDetail());
        verify(logger, times(1)).logErrorWithException(any(Logger.class), anyString(), eq(exception));
        verify(meterRegistry, times(1)).counter("exceptions.system", "exception", exception.getClass().getSimpleName());
    }
}
