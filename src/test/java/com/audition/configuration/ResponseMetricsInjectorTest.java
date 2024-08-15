package com.audition.configuration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResponseMetricsInjectorTest {

    private transient MeterRegistry meterRegistry;
    private transient Counter totalRequestsCounter;
    private transient Counter errorCounter;
    private transient Timer.Sample timerSample;
    private transient HttpServletRequest request;
    private transient HttpServletResponse response;
    private transient ResponseMetricsInjector responseMetricsInjector;

    @BeforeEach
    void setUp() throws Exception {
        meterRegistry = mock(MeterRegistry.class);
        totalRequestsCounter = mock(Counter.class);
        errorCounter = mock(Counter.class);
        timerSample = mock(Timer.Sample.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        // Create an instance of ResponseMetricsInjector
        responseMetricsInjector = new ResponseMetricsInjector(meterRegistry);

        // Use reflection to inject the mocked Counters into the private fields
        setPrivateField(responseMetricsInjector, "totalRequestsCounter", totalRequestsCounter);
        setPrivateField(responseMetricsInjector, "errorCounter", errorCounter);
    }

    private void setPrivateField(final Object target, final String fieldName, final Object value) throws NoSuchFieldException, IllegalAccessException {
        ReflectionTestUtils.setField(target, fieldName, value);
    }

    @Test
    void testPreHandle() {
        try (MockedStatic<Timer> mockedTimer = mockStatic(Timer.class)) {
            // Mock the static Timer.start method
            mockedTimer.when(() -> Timer.start(meterRegistry)).thenReturn(timerSample);

            // Replace null with a valid Object
            final Object handler = new Object();
            final boolean result = responseMetricsInjector.preHandle(request, response, handler);

            assertTrue(result);

            // Verify that Timer.start(meterRegistry) was called
            mockedTimer.verify(() -> Timer.start(eq(meterRegistry)));

            // Verify that the timer sample was stored in the request attribute
            verify(request).setAttribute(eq("timerSample"), eq(timerSample));

            // Verify that the totalRequestsCounter was incremented
            verify(totalRequestsCounter).increment();
        }
    }

    @Test
    void testAfterCompletionSuccess() {
        // Setup request and response
        when(request.getAttribute("timerSample")).thenReturn(timerSample);
        when(response.getStatus()).thenReturn(200);

        // Set up mock behavior for request and response
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/test-uri");
        when(response.getStatus()).thenReturn(200);

        try (MockedStatic<Timer> mockedTimer = mockStatic(Timer.class)) {
            final Timer.Builder timerBuilder = mock(Timer.Builder.class);
            when(Timer.builder("http.server.requests")).thenReturn(timerBuilder);
            when(timerBuilder.tag(anyString(), anyString())).thenReturn(timerBuilder);
            when(timerBuilder.description(anyString())).thenReturn(timerBuilder);
            when(timerBuilder.register(meterRegistry)).thenReturn(mock(Timer.class));

            // Replace null with a valid Object
            final Object handler = new Object();
            responseMetricsInjector.afterCompletion(request, response, handler, null);

            // Verify that the timer sample was stopped
            verify(timerSample).stop(any(Timer.class));

            // Verify that errorCounter was not incremented since there was no error
            verify(errorCounter, never()).increment();
        }
    }

    @Test
    void testAfterCompletionError() {
        // Setup request and response
        when(request.getAttribute("timerSample")).thenReturn(timerSample);
        when(response.getStatus()).thenReturn(500);

        // Set up mock behavior for request and response
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/test-uri");
        when(response.getStatus()).thenReturn(200);

        try (MockedStatic<Timer> mockedTimer = mockStatic(Timer.class)) {
            final Timer.Builder timerBuilder = mock(Timer.Builder.class);
            when(Timer.builder("http.server.requests")).thenReturn(timerBuilder);
            when(timerBuilder.tag(anyString(), anyString())).thenReturn(timerBuilder);
            when(timerBuilder.description(anyString())).thenReturn(timerBuilder);
            when(timerBuilder.register(meterRegistry)).thenReturn(mock(Timer.class));

            // Replace null with a valid Object
            final Object handler = new Object();
            responseMetricsInjector.afterCompletion(request, response, handler, new Exception("Test Exception"));

            // Verify that the timer sample was stopped
            verify(timerSample).stop(any(Timer.class));

            // Verify that the errorCounter was incremented
            verify(errorCounter).increment();
        }
    }
}
