package com.audition.configuration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.*;

@SpringBootTest
class ResponseMetricsInjectorTest {

    @MockBean
    private MeterRegistry meterRegistry;

    @Autowired
    private ResponseMetricsInjector responseMetricsInjector;

    private Counter mockTotalRequestsCounter;
    private Counter mockErrorCounter;
    private Timer mockTimer;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockTotalRequestsCounter = Mockito.mock(Counter.class);
        mockErrorCounter = Mockito.mock(Counter.class);
        mockTimer = Mockito.mock(Timer.class);
        mockRequest = Mockito.mock(HttpServletRequest.class);
        mockResponse = Mockito.mock(HttpServletResponse.class);

        when(meterRegistry.counter("http.server.requests.total")).thenReturn(mockTotalRequestsCounter);
        when(meterRegistry.counter("http.server.requests.errors")).thenReturn(mockErrorCounter);
    }

    @Test
    void testPreHandle() {
        responseMetricsInjector.preHandle(mockRequest, mockResponse, new Object());

        verify(mockTotalRequestsCounter, times(1)).increment();
        verify(mockRequest, times(1)).setAttribute(eq("timerSample"), any(Timer.Sample.class));
    }

    @Test
    void testAfterCompletion() {
        Timer.Sample mockSample = Timer.start(meterRegistry);
        when(mockRequest.getAttribute("timerSample")).thenReturn(mockSample);
        when(mockResponse.getStatus()).thenReturn(200);

        responseMetricsInjector.afterCompletion(mockRequest, mockResponse, new Object(), null);

        verify(mockSample, times(1)).stop(any(Timer.class));
        verify(mockErrorCounter, never()).increment();
    }

    @Test
    void testAfterCompletionWithErrorStatus() {
        Timer.Sample mockSample = Timer.start(meterRegistry);
        when(mockRequest.getAttribute("timerSample")).thenReturn(mockSample);
        when(mockResponse.getStatus()).thenReturn(500);

        responseMetricsInjector.afterCompletion(mockRequest, mockResponse, new Object(), null);

        verify(mockSample, times(1)).stop(any(Timer.class));
        verify(mockErrorCounter, times(1)).increment();
    }

    @Test
    void testAfterCompletionWithException() {
        Timer.Sample mockSample = Timer.start(meterRegistry);
        when(mockRequest.getAttribute("timerSample")).thenReturn(mockSample);

        responseMetricsInjector.afterCompletion(mockRequest, mockResponse, new Object(), new RuntimeException("Test Exception"));

        verify(mockSample, times(1)).stop(any(Timer.class));
        verify(mockErrorCounter, times(1)).increment();
    }
}
