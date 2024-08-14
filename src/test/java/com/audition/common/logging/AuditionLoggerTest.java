package com.audition.common.logging;

import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ProblemDetail;

import static org.mockito.Mockito.*;

@SpringBootTest
class AuditionLoggerTest {

    private AuditionLogger auditionLogger;
    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        auditionLogger = new AuditionLogger();
        mockLogger = Mockito.mock(Logger.class);
    }

    @Test
    void testInfoWithMessage() {
        when(mockLogger.isInfoEnabled()).thenReturn(true);
        auditionLogger.info(mockLogger, "Test message");
        verify(mockLogger, times(1)).info("Test message");
    }

    @Test
    void testInfoWithMessageAndObject() {
        when(mockLogger.isInfoEnabled()).thenReturn(true);
        auditionLogger.info(mockLogger, "Test message: {}", 123);
        verify(mockLogger, times(1)).info("Test message: {}", 123);
    }

    @Test
    void testDebugWithMessage() {
        when(mockLogger.isDebugEnabled()).thenReturn(true);
        auditionLogger.debug(mockLogger, "Debug message");
        verify(mockLogger, times(1)).debug("Debug message");
    }

    @Test
    void testWarnWithMessage() {
        when(mockLogger.isWarnEnabled()).thenReturn(true);
        auditionLogger.warn(mockLogger, "Warn message");
        verify(mockLogger, times(1)).warn("Warn message");
    }

    @Test
    void testErrorWithMessage() {
        when(mockLogger.isErrorEnabled()).thenReturn(true);
        auditionLogger.error(mockLogger, "Error message");
        verify(mockLogger, times(1)).error("Error message");
    }

    @Test
    void testLogErrorWithException() {
        Exception exception = new RuntimeException("Test exception");
        when(mockLogger.isErrorEnabled()).thenReturn(true);
        auditionLogger.logErrorWithException(mockLogger, "Error message", exception);
        verify(mockLogger, times(1)).error("Error message", exception);
    }

    @Test
    void testLogStandardProblemDetail() {
        ProblemDetail problemDetail = ProblemDetail.forStatus(500);
        problemDetail.setTitle("Test Title");
        problemDetail.setDetail("Test Detail");
        problemDetail.setInstance(URI.create("/test-instance"));

        Exception exception = new RuntimeException("Test exception");
        when(mockLogger.isErrorEnabled()).thenReturn(true);
        auditionLogger.logStandardProblemDetail(mockLogger, problemDetail, exception);

        verify(mockLogger, times(1)).error(contains("Status: 500"), eq(exception));
        verify(mockLogger, times(1)).error(contains("Title: Test Title"), eq(exception));
        verify(mockLogger, times(1)).error(contains("Detail: Test Detail"), eq(exception));
        verify(mockLogger, times(1)).error(contains("Instance: /test-instance"), eq(exception));
    }

    @Test
    void testLogHttpStatusCodeError() {
        when(mockLogger.isErrorEnabled()).thenReturn(true);
        auditionLogger.logHttpStatusCodeError(mockLogger, "Error occurred", 404);
        verify(mockLogger, times(1)).error("Error Code: 404 - Message: Error occurred\n");
    }
}
