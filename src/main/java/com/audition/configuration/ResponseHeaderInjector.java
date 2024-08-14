package com.audition.configuration;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;

import io.opentelemetry.context.Scope;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import org.slf4j.MDC;

/**
 * Inject openTelemetry trace and span Ids in the response headers.
 */
@Component
public class ResponseHeaderInjector implements HandlerInterceptor {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String SPAN_ID_HEADER = "X-Span-Id";

    private final Tracer tracer;

    public ResponseHeaderInjector() {
        this.tracer = GlobalOpenTelemetry.getTracer("com.audition");
    }

    /**
     * Start tracing and inject trace and span IDs into response headers.
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param handler  Not used
     * @return true to proceed with the request
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // Extract trace ID and span ID from incoming request headers if present
        String incomingTraceId = request.getHeader(TRACE_ID_HEADER);
        String incomingSpanId = request.getHeader(SPAN_ID_HEADER);

        // Check if the incoming headers contain trace and span IDs
        Span currentSpan;
        if (incomingTraceId != null && incomingSpanId != null) {
            // If incoming trace and span IDs are present, create a span with this context
            currentSpan = tracer.spanBuilder("http_request")
                .setParent(io.opentelemetry.context.Context.current().with(Span.wrap(SpanContext.createFromRemoteParent(
                    incomingTraceId,
                    incomingSpanId,
                    TraceFlags.getDefault(),
                    TraceState.getDefault()))))
                .startSpan();
        } else {
            // Otherwise, start a new span
            currentSpan = tracer.spanBuilder("http_request").startSpan();
        }

        // Make the span the current span
        Scope scope = currentSpan.makeCurrent();

        // Add trace and span IDs to MDC
        MDC.put("trace_id", currentSpan.getSpanContext().getTraceId());
        MDC.put("span_id", currentSpan.getSpanContext().getSpanId());

        // Inject trace ID and span ID into response headers
        response.setHeader(TRACE_ID_HEADER, currentSpan.getSpanContext().getTraceId());
        response.setHeader(SPAN_ID_HEADER, currentSpan.getSpanContext().getSpanId());

        // Store both the span and scope in the request attributes for later use
        request.setAttribute("currentSpan", currentSpan);
        request.setAttribute("currentScope", scope);

        return true;
    }

    /**
     * Complete the trace and close the scope.
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param handler  Not used
     * @param ex       Any exception that occurred during request processing
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Retrieve the current span and scope from the request
        Span currentSpan = (Span) request.getAttribute("currentSpan");
        Scope currentScope = (Scope) request.getAttribute("currentScope");

        if (currentSpan != null) {
            // Record exception if it occurred
            if (ex != null) {
                currentSpan.recordException(ex);
                currentSpan.setStatus(StatusCode.ERROR, "Exception occurred during request processing");
            }
            // End the span
            currentSpan.end();
        }

        // Close the scope if it exists
        if (currentScope != null) {
            currentScope.close();
        }
        MDC.clear();
    }
}
