package com.audition.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SystemExceptionTest {

    @Test
    void testDefaultConstructor() {
        SystemException exception = new SystemException();
        assertNull(exception.getMessage());
        assertNull(exception.getTitle());
        assertNull(exception.getStatusCode());
        assertNull(exception.getDetail());
    }

    @Test
    void testConstructorWithMessage() {
        String message = "An error occurred";
        SystemException exception = new SystemException(message);
        assertEquals(message, exception.getMessage());
        assertEquals(SystemException.DEFAULT_TITLE, exception.getTitle());
        assertNull(exception.getStatusCode());
        assertNull(exception.getDetail());
    }

    @Test
    void testConstructorWithMessageAndErrorCode() {
        String message = "An error occurred";
        Integer errorCode = 404;
        SystemException exception = new SystemException(message, errorCode);
        assertEquals(message, exception.getMessage());
        assertEquals(SystemException.DEFAULT_TITLE, exception.getTitle());
        assertEquals(errorCode, exception.getStatusCode());
        assertNull(exception.getDetail());
    }

    @Test
    void testConstructorWithMessageAndThrowable() {
        String message = "An error occurred";
        Throwable cause = new RuntimeException("Root cause");
        SystemException exception = new SystemException(message, cause);
        assertEquals(message, exception.getMessage());
        assertEquals(SystemException.DEFAULT_TITLE, exception.getTitle());
        assertNull(exception.getStatusCode());
        assertNull(exception.getDetail());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithDetailTitleAndErrorCode() {
        String detail = "Detailed message";
        String title = "Custom Title";
        Integer errorCode = 500;
        SystemException exception = new SystemException(detail, title, errorCode);
        assertEquals(detail, exception.getMessage());
        assertEquals(title, exception.getTitle());
        assertEquals(errorCode, exception.getStatusCode());
        assertEquals(detail, exception.getDetail());
    }

    @Test
    void testConstructorWithDetailTitleAndThrowable() {
        String detail = "Detailed message";
        String title = "Custom Title";
        Throwable cause = new RuntimeException("Root cause");
        SystemException exception = new SystemException(detail, title, cause);
        assertEquals(detail, exception.getMessage());
        assertEquals(title, exception.getTitle());
        assertEquals(500, exception.getStatusCode());
        assertEquals(detail, exception.getDetail());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithDetailErrorCodeAndThrowable() {
        String detail = "Detailed message";
        Integer errorCode = 500;
        Throwable cause = new RuntimeException("Root cause");
        SystemException exception = new SystemException(detail, errorCode, cause);
        assertEquals(detail, exception.getMessage());
        assertEquals(SystemException.DEFAULT_TITLE, exception.getTitle());
        assertEquals(errorCode, exception.getStatusCode());
        assertEquals(detail, exception.getDetail());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithDetailTitleErrorCodeAndThrowable() {
        String detail = "Detailed message";
        String title = "Custom Title";
        Integer errorCode = 500;
        Throwable cause = new RuntimeException("Root cause");
        SystemException exception = new SystemException(detail, title, errorCode, cause);
        assertEquals(detail, exception.getMessage());
        assertEquals(title, exception.getTitle());
        assertEquals(errorCode, exception.getStatusCode());
        assertEquals(detail, exception.getDetail());
        assertEquals(cause, exception.getCause());
    }
}
