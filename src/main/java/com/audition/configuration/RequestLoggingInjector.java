package com.audition.configuration;

import com.audition.common.logging.AuditionLogger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestLoggingInjector implements HandlerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(RequestLoggingInjector.class);

    @Autowired
    private AuditionLogger logger;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Log request details
        logger.info(LOG, "Request URI: {}", request.getRequestURI());
        logger.info(LOG, "Request Method: {}", request.getMethod());
        logger.info(LOG, "Request Query String: {}", request.getQueryString());
        logger.info(LOG, "Request Remote User: {}", request.getRemoteUser());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
        Exception ex) {
        // Log response status and any exceptions
        logger.info(LOG, "Response Status: {}", response.getStatus());
        if (ex != null) {
            logger.logErrorWithException(LOG, "Request to " + request.getRequestURI() + " resulted in an exception",
                ex);
        }
    }

}
