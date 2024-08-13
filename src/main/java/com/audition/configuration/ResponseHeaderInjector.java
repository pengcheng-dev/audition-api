package com.audition.configuration;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;

import io.opentelemetry.context.Scope;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

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
        System.out.println("--------------------------------------------");
    }

    /**
     * start tracing
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param handler not use
     * @return success or not
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        System.out.println("-----------++++++++++++++++++------------");
        // Start tracing
        Span currentSpan = tracer.spanBuilder("http_request").startSpan();

        System.out.println("traceid:" + currentSpan.getSpanContext().getTraceId());
        System.out.println("spanid:" + currentSpan.getSpanContext().getSpanId());

        // Make the span the current span
        Scope scope = currentSpan.makeCurrent();

        // Store both the span and scope in the request attributes
        request.setAttribute("currentSpan", currentSpan);
        request.setAttribute("currentScope", scope);

        return true;
    }

    /**
     * inject header
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param handler not use
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, org.springframework.web.servlet.ModelAndView modelAndView) {
        // Inject trace and span IDs into response headers
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++");

        Span currentSpan = (Span) request.getAttribute("currentSpan");
        System.out.println("traceid:" + currentSpan.getSpanContext().getTraceId());
        System.out.println("spanid:" + currentSpan.getSpanContext().getSpanId());
        if (currentSpan != null) {
            response.setHeader(TRACE_ID_HEADER, currentSpan.getSpanContext().getTraceId());
            response.setHeader(SPAN_ID_HEADER, currentSpan.getSpanContext().getSpanId());
        }
    }

    /**
     * ending tracing
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param handler not use
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        System.out.println("============================================");
        // End tracing
        Span currentSpan = (Span) request.getAttribute("currentSpan");
        Scope currentScope = (Scope) request.getAttribute("currentScope");


        System.out.println("traceid:" + currentSpan.getSpanContext().getTraceId());
        System.out.println("spanid:" + currentSpan.getSpanContext().getSpanId());

        if (currentSpan != null) {
            if (ex != null) {
                currentSpan.recordException(ex);
                currentSpan.setStatus(StatusCode.ERROR, "Exception occurred during request processing");
            }
            currentSpan.end();
        }

        if (currentScope != null) {
            currentScope.close();
        }
    }
}
