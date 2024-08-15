package com.audition.configuration;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Inject openTelemetry trace and span Ids in the response headers.
 */
@Component
public class ResponseHeaderInjector implements HandlerInterceptor {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String SPAN_ID_HEADER = "X-Span-Id";

    private final transient Tracer tracer;

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
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {

        // Extract trace ID and span ID from incoming request headers if present
        final String incomingTraceId = request.getHeader(TRACE_ID_HEADER);
        final String incomingSpanId = request.getHeader(SPAN_ID_HEADER);

        Span currentSpan;

        if (incomingTraceId != null && incomingSpanId != null) {
            currentSpan = tracer.spanBuilder("http_request")
                .setParent(Context.current().with(Span.wrap(SpanContext.createFromRemoteParent(
                    incomingTraceId,
                    incomingSpanId,
                    TraceFlags.getDefault(),
                    TraceState.getDefault()))))
                .startSpan();
        } else {
            currentSpan = tracer.spanBuilder("http_request").startSpan();
        }

        // Add trace and span IDs to MDC
        MDC.put("trace_id", currentSpan.getSpanContext().getTraceId());
        MDC.put("span_id", currentSpan.getSpanContext().getSpanId());

        // Inject trace ID and span ID into response headers
        response.setHeader(TRACE_ID_HEADER, currentSpan.getSpanContext().getTraceId());
        response.setHeader(SPAN_ID_HEADER, currentSpan.getSpanContext().getSpanId());

        // Store the span and context in the request attributes for later use
        Context context;
        context = Context.current().with(currentSpan);
        request.setAttribute("currentSpan", currentSpan);
        request.setAttribute("context", context);

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
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response, final Object handler,
        final Exception ex) {
        // Retrieve the current span and context from the request
        final Span currentSpan = (Span) request.getAttribute("currentSpan");
        final Context context = (Context) request.getAttribute("context");

        if (currentSpan != null && context != null) {
            try (Scope scope = context.makeCurrent()) {
                // Record exception if it occurred
                if (ex != null) {
                    currentSpan.recordException(ex);
                    currentSpan.setStatus(StatusCode.ERROR, "Exception occurred during request processing");
                }
                // End the span
                currentSpan.end();
            }
        }

        MDC.clear();
    }
}
