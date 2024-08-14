package com.audition.configuration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Add incoming request count, error count and timer for each request
 */
@Component
public class ResponseMetricsInjector implements HandlerInterceptor {

    private final MeterRegistry meterRegistry;
    private final Counter totalRequestsCounter;
    private final Counter errorCounter;

    /**
     * Initialize counters and register to registry
     * @param meterRegistry MeterRegistry
     */
    public ResponseMetricsInjector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize counters
        this.totalRequestsCounter = Counter.builder("http.server.requests.total")
            .description("Total number of HTTP requests")
            .register(meterRegistry);

        this.errorCounter = Counter.builder("http.server.requests.errors")
            .description("Total number of HTTP request errors")
            .register(meterRegistry);
    }

    /**
     * start timer and request counter increment
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param handler  Not used
     * @return true
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Start timer for request processing time
        Timer.Sample sample = Timer.start(meterRegistry);
        request.setAttribute("timerSample", sample);

        // Increment the total request counter
        totalRequestsCounter.increment();

        return true;
    }

    /**
     * register timer and error counter increment if exception happened
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param handler  Not used
     * @param ex exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
        Exception ex) {
        // Record the request processing time using Micrometer
        Timer.Sample sample = (Timer.Sample) request.getAttribute("timerSample");
        if (sample != null) {
            sample.stop(Timer.builder("http.server.requests")
                .tag("method", request.getMethod())
                .tag("uri", request.getRequestURI())
                .tag("status", Integer.toString(response.getStatus()))
                .description("HTTP Server Requests")
                .register(meterRegistry));
        }

        // Increment the error counter if there was an exception
        if (ex != null || response.getStatus() >= 400) {
            errorCounter.increment();
        }
    }

}
