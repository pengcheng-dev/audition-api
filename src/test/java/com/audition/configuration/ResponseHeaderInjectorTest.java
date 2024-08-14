package com.audition.configuration;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResponseHeaderInjectorTest {

    private ResponseHeaderInjector responseHeaderInjector;
    private Tracer tracer;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        tracer = GlobalOpenTelemetry.getTracer("com.audition");
        responseHeaderInjector = new ResponseHeaderInjector();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    @Test
    void testPreHandleWithExistingTraceIdAndSpanId() {
        // Mock incoming trace ID and span ID
        String traceId = "1234567890abcdef1234567890abcdef";
        String spanId = "abcdef1234567890";
        when(request.getHeader("X-Trace-Id")).thenReturn(traceId);
        when(request.getHeader("X-Span-Id")).thenReturn(spanId);

        // Call the preHandle method
        boolean result = responseHeaderInjector.preHandle(request, response, null);

        // Verify the span is continued from the provided context
        Span currentSpan = (Span) request.getAttribute("currentSpan");
        assertNotNull(currentSpan);
        assertEquals(traceId, currentSpan.getSpanContext().getTraceId());
        assertEquals(spanId, currentSpan.getSpanContext().getSpanId());

        // Verify MDC values
        assertEquals(traceId, MDC.get("trace_id"));
        assertEquals(spanId, MDC.get("span_id"));

        // Ensure preHandle returns true
        assertEquals(true, result);
    }

    @Test
    void testPreHandleWithoutExistingTraceIdAndSpanId() {
        // No trace ID or span ID in headers
        when(request.getHeader(anyString())).thenReturn(null);

        // Call the preHandle method
        boolean result = responseHeaderInjector.preHandle(request, response, null);

        // Verify a new span is created
        Span currentSpan = (Span) request.getAttribute("currentSpan");
        assertNotNull(currentSpan);

        // Verify MDC values
        assertNotNull(MDC.get("trace_id"));
        assertNotNull(MDC.get("span_id"));

        // Ensure preHandle returns true
        assertEquals(true, result);
    }
}
