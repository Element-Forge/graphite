package io.github.graphite.auth;

import org.jetbrains.annotations.NotNull;

/**
 * Provider for authentication credentials.
 *
 * <p>Implementations of this interface supply authentication headers
 * for GraphQL requests. Common implementations include:</p>
 * <ul>
 *   <li>{@link BearerTokenAuth} - Bearer token authentication</li>
 *   <li>{@link ApiKeyAuth} - API key authentication</li>
 *   <li>{@link BasicAuth} - HTTP Basic authentication</li>
 * </ul>
 */
@FunctionalInterface
public interface AuthProvider {

    /**
     * Returns the authentication header value.
     *
     * <p>This method may be called multiple times and should return
     * the current valid authentication value. Implementations may
     * refresh tokens or perform other operations as needed.</p>
     *
     * @return the authentication header value
     */
    @NotNull
    String getAuthHeaderValue();

    /**
     * Returns the name of the authentication header.
     *
     * <p>The default is "Authorization", but implementations may
     * override this for custom header names (e.g., "X-API-Key").</p>
     *
     * @return the header name
     */
    @NotNull
    default String getAuthHeaderName() {
        return "Authorization";
    }
}
