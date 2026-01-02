package io.github.graphite.interceptor;

import io.github.graphite.http.HttpRequest;
import org.jetbrains.annotations.NotNull;

/**
 * Interceptor for modifying HTTP requests before they are sent.
 *
 * <p>Request interceptors can be used to:</p>
 * <ul>
 *   <li>Add authentication headers</li>
 *   <li>Add tracing/correlation IDs</li>
 *   <li>Log outgoing requests</li>
 *   <li>Modify request content</li>
 * </ul>
 *
 * <p>Example:</p>
 * <pre>{@code
 * RequestInterceptor addCorrelationId = (request, chain) -> {
 *     HttpRequest modified = HttpRequest.builder()
 *         .body(request.body())
 *         .headers(request.headers())
 *         .header("X-Correlation-ID", UUID.randomUUID().toString())
 *         .build();
 *     return chain.proceed(modified);
 * };
 * }</pre>
 */
@FunctionalInterface
public interface RequestInterceptor {

    /**
     * Intercepts the request and optionally modifies it.
     *
     * @param request the original request
     * @param chain the interceptor chain to proceed with
     * @return the (potentially modified) request
     */
    @NotNull
    HttpRequest intercept(@NotNull HttpRequest request, @NotNull Chain chain);

    /**
     * Chain for proceeding with the next interceptor or final request.
     */
    @FunctionalInterface
    interface Chain {

        /**
         * Proceeds with the given request to the next interceptor or final destination.
         *
         * @param request the request to proceed with
         * @return the request (potentially modified by subsequent interceptors)
         */
        @NotNull
        HttpRequest proceed(@NotNull HttpRequest request);
    }
}
