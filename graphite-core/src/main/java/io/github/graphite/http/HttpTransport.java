package io.github.graphite.http;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

/**
 * Abstraction for HTTP communication with a GraphQL endpoint.
 *
 * <p>This interface allows different HTTP client implementations to be used
 * with the GraphiteClient. The default implementation uses Java's built-in
 * {@link java.net.http.HttpClient}.</p>
 */
public interface HttpTransport extends Closeable {

    /**
     * Sends a GraphQL request synchronously.
     *
     * @param request the request to send
     * @return the response from the server
     * @throws GraphiteHttpException if an HTTP error occurs
     */
    @NotNull
    HttpResponse send(@NotNull HttpRequest request);

    /**
     * Sends a GraphQL request asynchronously.
     *
     * @param request the request to send
     * @return a future that will complete with the response
     */
    @NotNull
    CompletableFuture<HttpResponse> sendAsync(@NotNull HttpRequest request);

    @Override
    void close();
}
