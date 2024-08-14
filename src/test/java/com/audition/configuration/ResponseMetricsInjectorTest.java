package com.audition.configuration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ResponseMetricsInjectorTest {

    private MeterRegistry meterRegistry;
    private Counter totalRequestsCounter;
    private Counter errorCounter;
    private Timer.Sample timerSample;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ResponseMetricsInjector responseMetricsInjector;

    @BeforeEach
    void setUp() throws Exception {
        meterRegistry = Mockito.mock(MeterRegistry.class);
        totalRequestsCounter = Mockito.mock(Counter.class);
        errorCounter = Mockito.mock(Counter.class);
        timerSample = Mockito.mock(Timer.Sample.class);
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);

        // Create an instance of ResponseMetricsInjector
        responseMetricsInjector = new ResponseMetricsInjector(meterRegistry);

        // Use reflection to inject the mocked Counters into the private fields
        setPrivateField(responseMetricsInjector, "totalRequestsCounter", totalRequestsCounter);
        setPrivateField(responseMetricsInjector, "errorCounter", errorCounter);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testPreHandle() {
        try (MockedStatic<Timer> mockedTimer = mockStatic(Timer.class)) {
            // Mock the static Timer.start method
            mockedTimer.when(() -> Timer.start(meterRegistry)).thenReturn(timerSample);

            boolean result = responseMetricsInjector.preHandle(request, response, null);

            // Verify that Timer.start(meterRegistry) was called
            mockedTimer.verify(() -> Timer.start(eq(meterRegistry)));

            // Verify that the timer sample was stored in the request attribute
            verify(request).setAttribute(eq("timerSample"), eq(timerSample));

            // Verify that the totalRequestsCounter was incremented
            verify(totalRequestsCounter).increment();

            assertTrue(result);
        }
    }

    @Test
    void testAfterCompletion_Success() {
        // Setup request and response
        when(request.getAttribute("timerSample")).thenReturn(timerSample);
        when(response.getStatus()).thenReturn(200);

        try (MockedStatic<Timer> mockedTimer = mockStatic(Timer.class)) {
            Timer.Builder timerBuilder = mock(Timer.Builder.class);
            when(Timer.builder("http.server.requests")).thenReturn(timerBuilder);
            when(timerBuilder.tag(anyString(), anyString())).thenReturn(timerBuilder);
            when(timerBuilder.description(anyString())).thenReturn(timerBuilder);
            when(timerBuilder.register(meterRegistry)).thenReturn(mock(Timer.class));

            responseMetricsInjector.afterCompletion(request, response, null, null);

            // Verify that the timer sample was stopped
            verify(timerSample).stop(any(Timer.class));

            // Verify that errorCounter was not incremented since there was no error
            verify(errorCounter, never()).increment();
        }
    }

    @Test
    void testAfterCompletion_Error() {
        // Setup request and response
        when(request.getAttribute("timerSample")).thenReturn(timerSample);
        when(response.getStatus()).thenReturn(500);

        try (MockedStatic<Timer> mockedTimer = mockStatic(Timer.class)) {
            Timer.Builder timerBuilder = mock(Timer.Builder.class);
            when(Timer.builder("http.server.requests")).thenReturn(timerBuilder);
            when(timerBuilder.tag(anyString(), anyString())).thenReturn(timerBuilder);
            when(timerBuilder.description(anyString())).thenReturn(timerBuilder);
            when(timerBuilder.register(meterRegistry)).thenReturn(mock(Timer.class));

            responseMetricsInjector.afterCompletion(request, response, null, new Exception("Test Exception"));

            // Verify that the timer sample was stopped
            verify(timerSample).stop(any(Timer.class));

            // Verify that the errorCounter was incremented
            verify(errorCounter).increment();
        }
    }
}
