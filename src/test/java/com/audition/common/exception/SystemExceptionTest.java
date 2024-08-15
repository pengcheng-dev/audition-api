package com.audition.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SystemExceptionTest {

    private static final String MESSAGE = "An error occurred";
    private static final String DETAILED_MESSAGE = "Detailed message";
    private static final String ROOT_CAUSE_MESSAGE = "Root cause";

    @Test
    void testDefaultConstructor() {
        final SystemException exception = new SystemException();
        assertNull(exception.getMessage());
        assertNull(exception.getTitle());
        assertNull(exception.getStatusCode());
        assertNull(exception.getDetail());
    }

    @Test
    void testConstructorWithMessage() {
        final String message = MESSAGE;
        final SystemException exception = new SystemException(message);
        assertEquals(message, exception.getMessage());
        assertEquals(SystemException.DEFAULT_TITLE, exception.getTitle());
        assertNull(exception.getStatusCode());
        assertNull(exception.getDetail());
    }

    @Test
    void testConstructorWithMessageAndErrorCode() {
        final String message = MESSAGE;
        final Integer errorCode = 404;
        final SystemException exception = new SystemException(message, errorCode);
        assertEquals(message, exception.getMessage());
        assertEquals(SystemException.DEFAULT_TITLE, exception.getTitle());
        assertEquals(errorCode, exception.getStatusCode());
        assertNull(exception.getDetail());
    }

    @Test
    void testConstructorWithMessageAndThrowable() {
        final String message = MESSAGE;
        final Throwable cause = new RuntimeException(ROOT_CAUSE_MESSAGE);
        final SystemException exception = new SystemException(message, cause);
        assertEquals(message, exception.getMessage());
        assertEquals(SystemException.DEFAULT_TITLE, exception.getTitle());
        assertNull(exception.getStatusCode());
        assertNull(exception.getDetail());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithDetailTitleAndErrorCode() {
        final String detail = DETAILED_MESSAGE;
        final String title = "Custom Title";
        final Integer errorCode = 500;
        final SystemException exception = new SystemException(detail, title, errorCode);
        assertEquals(detail, exception.getMessage());
        assertEquals(title, exception.getTitle());
        assertEquals(errorCode, exception.getStatusCode());
        assertEquals(detail, exception.getDetail());
    }

    @Test
    void testConstructorWithDetailTitleAndThrowable() {
        final String detail = DETAILED_MESSAGE;
        final String title = "Custom Title";
        final Throwable cause = new RuntimeException(ROOT_CAUSE_MESSAGE);
        final SystemException exception = new SystemException(detail, title, cause);
        assertEquals(detail, exception.getMessage());
        assertEquals(title, exception.getTitle());
        assertEquals(500, exception.getStatusCode());
        assertEquals(detail, exception.getDetail());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithDetailErrorCodeAndThrowable() {
        final String detail = DETAILED_MESSAGE;
        final Integer errorCode = 500;
        final Throwable cause = new RuntimeException(ROOT_CAUSE_MESSAGE);
        final SystemException exception = new SystemException(detail, errorCode, cause);
        assertEquals(detail, exception.getMessage());
        assertEquals(SystemException.DEFAULT_TITLE, exception.getTitle());
        assertEquals(errorCode, exception.getStatusCode());
        assertEquals(detail, exception.getDetail());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithDetailTitleErrorCodeAndThrowable() {
        final String detail = DETAILED_MESSAGE;
        final String title = "Custom Title";
        final Integer errorCode = 500;
        final Throwable cause = new RuntimeException(ROOT_CAUSE_MESSAGE);
        final SystemException exception = new SystemException(detail, title, errorCode, cause);
        assertEquals(detail, exception.getMessage());
        assertEquals(title, exception.getTitle());
        assertEquals(errorCode, exception.getStatusCode());
        assertEquals(detail, exception.getDetail());
        assertEquals(cause, exception.getCause());
    }
}
