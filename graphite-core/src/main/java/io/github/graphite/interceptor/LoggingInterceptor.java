package io.github.graphite.interceptor;

import io.github.graphite.http.HttpRequest;
import io.github.graphite.http.HttpResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Interceptor that logs HTTP requests and responses.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Use default logger
 * LoggingInterceptor interceptor = LoggingInterceptor.create();
 *
 * // Use custom logger
 * LoggingInterceptor interceptor = LoggingInterceptor.create(myLogger);
 *
 * // With body logging (use with caution - may log sensitive data)
 * LoggingInterceptor interceptor = LoggingInterceptor.builder()
 *     .logRequestBody(true)
 *     .logResponseBody(true)
 *     .build();
 * }</pre>
 */
public final class LoggingInterceptor implements RequestInterceptor, ResponseInterceptor {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(LoggingInterceptor.class);

    private final Logger logger;
    private final boolean logRequestBody;
    private final boolean logResponseBody;

    private LoggingInterceptor(Builder builder) {
        this.logger = builder.logger;
        this.logRequestBody = builder.logRequestBody;
        this.logResponseBody = builder.logResponseBody;
    }

    /**
     * Creates a logging interceptor with default settings.
     *
     * @return a new logging interceptor
     */
    public static LoggingInterceptor create() {
        return builder().build();
    }

    /**
     * Creates a logging interceptor with a custom logger.
     *
     * @param logger the logger to use
     * @return a new logging interceptor
     */
    public static LoggingInterceptor create(@NotNull Logger logger) {
        return builder().logger(logger).build();
    }

    /**
     * Creates a new builder for configuring a LoggingInterceptor.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    @NotNull
    public HttpRequest intercept(@NotNull HttpRequest request, @NotNull RequestInterceptor.Chain chain) {
        if (logger.isDebugEnabled()) {
            logger.debug("GraphQL Request: {} headers", request.headers().size());
            if (logRequestBody && logger.isTraceEnabled()) {
                logger.trace("Request body: {}", request.body());
            }
        }
        return chain.proceed(request);
    }

    @Override
    @NotNull
    public HttpResponse intercept(@NotNull HttpResponse response, @NotNull ResponseInterceptor.Chain chain) {
        if (logger.isDebugEnabled()) {
            logger.debug("GraphQL Response: {} in {}ms",
                    response.statusCode(),
                    response.duration().toMillis());
            if (logResponseBody && logger.isTraceEnabled()) {
                logger.trace("Response body: {}", response.body());
            }
        }
        return chain.proceed(response);
    }

    /**
     * Builder for configuring {@link LoggingInterceptor} instances.
     */
    public static final class Builder {

        private Logger logger = DEFAULT_LOGGER;
        private boolean logRequestBody = false;
        private boolean logResponseBody = false;

        private Builder() {
        }

        /**
         * Sets the logger to use.
         *
         * @param logger the logger
         * @return this builder
         */
        @NotNull
        public Builder logger(@NotNull Logger logger) {
            this.logger = Objects.requireNonNull(logger, "logger must not be null");
            return this;
        }

        /**
         * Enables logging of request bodies at TRACE level.
         *
         * <p>Warning: This may log sensitive data.</p>
         *
         * @param logRequestBody whether to log request bodies
         * @return this builder
         */
        @NotNull
        public Builder logRequestBody(boolean logRequestBody) {
            this.logRequestBody = logRequestBody;
            return this;
        }

        /**
         * Enables logging of response bodies at TRACE level.
         *
         * <p>Warning: This may log sensitive data.</p>
         *
         * @param logResponseBody whether to log response bodies
         * @return this builder
         */
        @NotNull
        public Builder logResponseBody(boolean logResponseBody) {
            this.logResponseBody = logResponseBody;
            return this;
        }

        /**
         * Builds the configured LoggingInterceptor.
         *
         * @return the configured interceptor
         */
        @NotNull
        public LoggingInterceptor build() {
            return new LoggingInterceptor(this);
        }
    }
}
