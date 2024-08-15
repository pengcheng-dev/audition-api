package com.audition.configuration;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@SuppressWarnings("PMD.CloseResource")
class ResponseHeaderInjectorTest {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String SPAN_ID_HEADER = "X-Span-Id";
    private static final String CURRENT_SPAN_STR = "currentSpan";
    private static final String CONTEXT_STR = "context";

    private transient ResponseHeaderInjector responseHeaderInjector;
    private transient HttpServletRequest request;
    private transient HttpServletResponse response;
    private transient Span span;
    private transient SpanContext spanContext;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        final Tracer tracer = mock(Tracer.class);
        span = mock(Span.class);
        spanContext = mock(SpanContext.class);
        final SpanBuilder spanBuilder = mock(SpanBuilder.class);

        responseHeaderInjector = new ResponseHeaderInjector();
        // Set the private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(responseHeaderInjector, "tracer", tracer);

        // Default mock behavior
        when(tracer.spanBuilder(anyString())).thenReturn(spanBuilder);
        when(spanBuilder.setParent(any(Context.class))).thenReturn(spanBuilder);
        when(spanBuilder.startSpan()).thenReturn(span);
        when(span.getSpanContext()).thenReturn(spanContext);
    }

    @Test
    void testPreHandleWithExistingTraceIdAndSpanId() {
        final String traceId = "1234567890abcdef1234567890abcdef";
        final String spanId = "abcdef1234567890";

        when(request.getHeader(TRACE_ID_HEADER)).thenReturn(traceId);
        when(request.getHeader(SPAN_ID_HEADER)).thenReturn(spanId);
        when(spanContext.getTraceId()).thenReturn(traceId);
        when(spanContext.getSpanId()).thenReturn(spanId);

        // Replace null with a valid Object
        final Object handler = new Object();
        final boolean result = responseHeaderInjector.preHandle(request, response, handler);

        assertTrue(result);

        assertEquals(traceId, MDC.get("trace_id"));
        assertEquals(spanId, MDC.get("span_id"));
        verify(response).setHeader(TRACE_ID_HEADER, traceId);
        verify(response).setHeader(SPAN_ID_HEADER, spanId);
    }

    @Test
    void testPreHandleWithoutExistingTraceIdAndSpanId() {
        // Mock headers to return null for trace and span IDs
        when(request.getHeader(TRACE_ID_HEADER)).thenReturn(null);
        when(request.getHeader(SPAN_ID_HEADER)).thenReturn(null);

        // Mock SpanContext
        when(span.getSpanContext()).thenReturn(spanContext);
        when(spanContext.getTraceId()).thenReturn("new-trace-id");
        when(spanContext.getSpanId()).thenReturn("new-span-id");

        // Mock Context
        final Context mockContext = mock(Context.class);
        try (MockedStatic<Context> mockedContextStatic = mockStatic(Context.class)) {
            mockedContextStatic.when(Context::current).thenReturn(mockContext);
            when(mockContext.with(span)).thenReturn(mockContext);

            // Perform the preHandle operation
            final Object handler = new Object();
            final boolean result = responseHeaderInjector.preHandle(request, response, handler);

            // Assertions
            assertTrue(result);

            // Verify MDC interactions
            assertEquals("new-trace-id", MDC.get("trace_id"));
            assertEquals("new-span-id", MDC.get("span_id"));

            // Verify that headers are set correctly
            verify(response).setHeader(TRACE_ID_HEADER, "new-trace-id");
            verify(response).setHeader(SPAN_ID_HEADER, "new-span-id");

            // Verify that attributes are set in the request
            verify(request).setAttribute("currentSpan", span);
            verify(request).setAttribute("context", mockContext);
        }
    }

    @Test
    void testAfterCompletionWithContextAndException() {
        final Scope mockScope = mock(Scope.class);
        final Context mockContext = mock(Context.class);

        when(request.getAttribute(CURRENT_SPAN_STR)).thenReturn(span);
        when(request.getAttribute(CONTEXT_STR)).thenReturn(mockContext);
        when(mockContext.makeCurrent()).thenReturn(mockScope);

        final Exception exception = new RuntimeException("Test Exception");

        // Replace null with a valid Object
        final Object handler = new Object();
        responseHeaderInjector.afterCompletion(request, response, handler, exception);

        verify(span).recordException(exception);
        verify(span).setStatus(eq(StatusCode.ERROR), eq("Exception occurred during request processing"));
        //verify(span).end();
        verify(mockScope).close();
    }

    @Test
    void testAfterCompletionWithContextWithoutException() {
        final Scope mockScope = mock(Scope.class);
        final Context mockContext = mock(Context.class);

        when(request.getAttribute(CURRENT_SPAN_STR)).thenReturn(span);
        when(request.getAttribute(CONTEXT_STR)).thenReturn(mockContext);
        when(mockContext.makeCurrent()).thenReturn(mockScope);

        // Replace null with a valid Object
        final Object handler = new Object();
        responseHeaderInjector.afterCompletion(request, response, handler, null);

        verify(span, never()).recordException(any(Exception.class));
        verify(span).end();
        verify(mockScope).close();
    }

    @Test
    void testAfterCompletionWithoutContextAndException() {
        //mock context
        final Context mockContext = mock(Context.class);
        final Scope mockScope = mock(Scope.class);

        // No context is set in the request
        when(request.getAttribute(CURRENT_SPAN_STR)).thenReturn(span);
        when(request.getAttribute(CONTEXT_STR)).thenReturn(mockContext);

        when(mockContext.makeCurrent()).thenReturn(mockScope);

        // Replace null with a valid Object
        final Object handler = new Object();
        final Exception exception = new RuntimeException("Test Exception");

        when(span.recordException(exception)).thenReturn(span);
        when(span.setStatus(StatusCode.ERROR, "Exception occurred during request processing")).thenReturn(span);

        responseHeaderInjector.afterCompletion(request, response, handler, exception);

        verify(span).recordException(exception);
        verify(span).setStatus(eq(StatusCode.ERROR), eq("Exception occurred during request processing"));
        verify(span).end();
    }

    @Test
    void testAfterCompletionWithoutContextWithoutException() {
        // No context is set in the request
        when(request.getAttribute(CURRENT_SPAN_STR)).thenReturn(span);
        when(request.getAttribute(CONTEXT_STR)).thenReturn(null);

        // Replace null with a valid Object
        final Object handler = new Object();
        responseHeaderInjector.afterCompletion(request, response, handler, null);

        verify(span, never()).recordException(any(Exception.class));
        verify(span, never()).end();
    }
}
