package com.audition.configuration;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@SpringBootTest
class ResponseHeaderInjectorTest {

    @MockBean
    private Tracer tracer;

    private SpanBuilder mockSpanBuilder;
    private SpanContext mockSpanContext;

    private ResponseHeaderInjector responseHeaderInjector;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private Span mockSpan;
    private Scope mockScope;

    @BeforeEach
    void setUp() {
        mockSpanBuilder = Mockito.mock(SpanBuilder.class);
        mockSpanContext = Mockito.mock(SpanContext.class);
        mockSpan = Mockito.mock(Span.class);
        mockScope = Mockito.mock(Scope.class);
        mockRequest = Mockito.mock(HttpServletRequest.class);
        mockResponse = Mockito.mock(HttpServletResponse.class);

        when(tracer.spanBuilder(anyString())).thenReturn(mockSpanBuilder);
        when(mockSpanBuilder.startSpan()).thenReturn(mockSpan);
        when(mockSpan.makeCurrent()).thenReturn(mockScope);

        responseHeaderInjector = new ResponseHeaderInjector();
    }

    @Test
    void testPreHandle() {
        when(mockSpan.getSpanContext()).thenReturn(mockSpanContext);
        when(mockSpanContext.getTraceId()).thenReturn("test-trace-id");

        when(mockSpan.getSpanContext()).thenReturn(mockSpanContext);
        when(mockSpanContext.getSpanId()).thenReturn("test-span-id");

        responseHeaderInjector.preHandle(mockRequest, mockResponse, new Object());

        verify(mockResponse).setHeader("X-Trace-Id", "test-trace-id");
        verify(mockResponse).setHeader("X-Span-Id", "test-span-id");
        verify(mockRequest).setAttribute("currentSpan", mockSpan);
        verify(mockRequest).setAttribute("currentScope", mockScope);
        assertEquals("test-trace-id", MDC.get("trace_id"));
        assertEquals("test-span-id", MDC.get("span_id"));
    }

    @Test
    void testAfterCompletion() {
        when(mockRequest.getAttribute("currentSpan")).thenReturn(mockSpan);
        when(mockRequest.getAttribute("currentScope")).thenReturn(mockScope);

        responseHeaderInjector.afterCompletion(mockRequest, mockResponse, new Object(), null);

        verify(mockSpan).end();
        verify(mockScope).close();
        assertNull(MDC.get("trace_id"));
        assertNull(MDC.get("span_id"));
    }

    @Test
    void testAfterCompletionWithException() {
        Exception ex = new RuntimeException("Test exception");
        when(mockRequest.getAttribute("currentSpan")).thenReturn(mockSpan);
        when(mockRequest.getAttribute("currentScope")).thenReturn(mockScope);

        responseHeaderInjector.afterCompletion(mockRequest, mockResponse, new Object(), ex);

        verify(mockSpan).recordException(ex);
        verify(mockSpan).setStatus(any(), eq("Exception occurred during request processing"));
        verify(mockSpan).end();
        verify(mockScope).close();
        assertNull(MDC.get("trace_id"));
        assertNull(MDC.get("span_id"));
    }
}