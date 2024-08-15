package com.audition.common.logging;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

@Component
public class AuditionLogger {

    public void info(final Logger logger, final String message) {
        if (logger.isInfoEnabled()) {
            logger.info(message);
        }
    }

    public void info(final Logger logger, final String message, final Object object) {
        if (logger.isInfoEnabled()) {
            logger.info(message, object);
        }
    }

    public void debug(final Logger logger, final String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(message);
        }
    }

    public void warn(final Logger logger, final String message) {
        if (logger.isWarnEnabled()) {
            logger.warn(message);
        }
    }

    public void error(final Logger logger, final String message) {
        if (logger.isErrorEnabled()) {
            logger.error(message);
        }
    }

    public void logErrorWithException(final Logger logger, final String message, final Exception e) {
        if (logger.isErrorEnabled()) {
            logger.error(message, e);
        }
    }

    public void logStandardProblemDetail(final Logger logger, final ProblemDetail problemDetail, final Exception e) {
        if (logger.isErrorEnabled()) {
            final var message = createStandardProblemDetailMessage(problemDetail);
            logger.error(message, e);
        }
    }

    public void logHttpStatusCodeError(final Logger logger, final String message, final Integer errorCode) {
        if (logger.isErrorEnabled()) {
            logger.error(createBasicErrorResponseMessage(errorCode, message) + "\n");
        }
    }

    private String createStandardProblemDetailMessage(final ProblemDetail standardProblemDetail) {
        final StringBuilder message = new StringBuilder();

        if (standardProblemDetail.getStatus() > 0) {
            message.append("Status: ").append(standardProblemDetail.getStatus()).append(" - ");
        }
        if (StringUtils.isNotBlank(standardProblemDetail.getTitle())) {
            message.append("Title: ").append(standardProblemDetail.getTitle()).append(" - ");
        }
        if (StringUtils.isNotBlank(standardProblemDetail.getDetail())) {
            message.append("Detail: ").append(standardProblemDetail.getDetail()).append(" - ");
        }
        if (standardProblemDetail.getInstance() != null && StringUtils.isNotBlank(
            standardProblemDetail.getInstance().toString())) {
            message.append("Instance: ").append(standardProblemDetail.getInstance());
        }

        return message.toString();
    }

    private String createBasicErrorResponseMessage(final Integer errorCode, final String message) {
        return "Error Code: " + errorCode + " - Message: " + message;
    }
}
