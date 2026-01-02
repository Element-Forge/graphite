package io.github.graphite.interceptor;

import io.github.graphite.http.HttpResponse;
import org.jetbrains.annotations.NotNull;

/**
 * Interceptor for processing HTTP responses after they are received.
 *
 * <p>Response interceptors can be used to:</p>
 * <ul>
 *   <li>Log responses</li>
 *   <li>Record metrics</li>
 *   <li>Transform response content</li>
 *   <li>Handle specific error conditions</li>
 * </ul>
 *
 * <p>Example:</p>
 * <pre>{@code
 * ResponseInterceptor logResponse = (response, chain) -> {
 *     logger.info("Response: {} in {}ms", response.statusCode(), response.duration().toMillis());
 *     return chain.proceed(response);
 * };
 * }</pre>
 */
@FunctionalInterface
public interface ResponseInterceptor {

    /**
     * Intercepts the response and optionally modifies it.
     *
     * @param response the original response
     * @param chain the interceptor chain to proceed with
     * @return the (potentially modified) response
     */
    @NotNull
    HttpResponse intercept(@NotNull HttpResponse response, @NotNull Chain chain);

    /**
     * Chain for proceeding with the next interceptor or final response.
     */
    @FunctionalInterface
    interface Chain {

        /**
         * Proceeds with the given response to the next interceptor or final destination.
         *
         * @param response the response to proceed with
         * @return the response (potentially modified by subsequent interceptors)
         */
        @NotNull
        HttpResponse proceed(@NotNull HttpResponse response);
    }
}
