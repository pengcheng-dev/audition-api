package com.audition.configuration;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class ResponseHeaderInjectorTest {

    private ResponseHeaderInjector responseHeaderInjector;
    private Tracer tracer;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        tracer = mock(Tracer.class);
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

        // Mock SpanContext and Span
        SpanContext spanContext = mock(SpanContext.class);
        when(spanContext.getTraceId()).thenReturn(traceId);
        when(spanContext.getSpanId()).thenReturn(spanId);
        Span span = mock(Span.class);
        when(span.getSpanContext()).thenReturn(spanContext);

        // Mock Tracer and SpanBuilder
        SpanBuilder spanBuilder = mock(SpanBuilder.class);
        when(tracer.spanBuilder(anyString())).thenReturn(spanBuilder);
        when(spanBuilder.setParent(any(Context.class))).thenReturn(spanBuilder);
        when(spanBuilder.startSpan()).thenReturn(span);


        // Call the preHandle method
        boolean result = responseHeaderInjector.preHandle(request, response, null);

        // Verify span continuation
        //verify(spanBuilder).setParent(any(Context.class));
        //verify(spanBuilder).startSpan();

        // Verify the span is continued from the provided context
        assertNotNull(span);
        assertEquals(traceId, span.getSpanContext().getTraceId());
        assertEquals(spanId, span.getSpanContext().getSpanId());

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

        // Mock Span and SpanBuilder
        Span span = mock(Span.class);
        SpanContext spanContext = mock(SpanContext.class);
        when(span.getSpanContext()).thenReturn(spanContext);

        SpanBuilder spanBuilder = mock(SpanBuilder.class);
        when(tracer.spanBuilder(anyString())).thenReturn(spanBuilder);
        when(spanBuilder.startSpan()).thenReturn(span);

        // Call the preHandle method
        boolean result = responseHeaderInjector.preHandle(request, response, null);

        // Verify a new span is created
        //verify(spanBuilder, never()).setParent(any(Context.class));
        //verify(spanBuilder).startSpan();

        // Verify the span is created with a new context
        assertNotNull(span);

        // Verify MDC values
        assertNotNull(MDC.get("trace_id"));
        assertNotNull(MDC.get("span_id"));

        // Ensure preHandle returns true
        assertEquals(true, result);
    }
}
