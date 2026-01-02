package io.github.graphite.auth;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Authentication provider for API key authentication.
 *
 * <p>API keys can be sent in a custom header (default: "X-API-Key")
 * or in the standard Authorization header.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Default header name (X-API-Key)
 * AuthProvider auth = ApiKeyAuth.of("my-api-key");
 *
 * // Custom header name
 * AuthProvider auth = ApiKeyAuth.of("my-api-key", "X-Custom-Key");
 *
 * // Dynamic API key
 * AuthProvider auth = ApiKeyAuth.of(() -> keyService.getApiKey());
 * }</pre>
 */
public final class ApiKeyAuth implements AuthProvider {

    private static final String DEFAULT_HEADER_NAME = "X-API-Key";

    private final Supplier<String> keySupplier;
    private final String headerName;

    private ApiKeyAuth(Supplier<String> keySupplier, String headerName) {
        this.keySupplier = keySupplier;
        this.headerName = headerName;
    }

    /**
     * Creates an API key auth provider with a static key and default header name.
     *
     * @param apiKey the API key
     * @return the auth provider
     */
    public static ApiKeyAuth of(@NotNull String apiKey) {
        return of(apiKey, DEFAULT_HEADER_NAME);
    }

    /**
     * Creates an API key auth provider with a static key and custom header name.
     *
     * @param apiKey the API key
     * @param headerName the header name to use
     * @return the auth provider
     */
    public static ApiKeyAuth of(@NotNull String apiKey, @NotNull String headerName) {
        Objects.requireNonNull(apiKey, "apiKey must not be null");
        Objects.requireNonNull(headerName, "headerName must not be null");
        return new ApiKeyAuth(() -> apiKey, headerName);
    }

    /**
     * Creates an API key auth provider with a dynamic key supplier.
     *
     * @param keySupplier the key supplier
     * @return the auth provider
     */
    public static ApiKeyAuth of(@NotNull Supplier<String> keySupplier) {
        return of(keySupplier, DEFAULT_HEADER_NAME);
    }

    /**
     * Creates an API key auth provider with a dynamic key supplier and custom header.
     *
     * @param keySupplier the key supplier
     * @param headerName the header name to use
     * @return the auth provider
     */
    public static ApiKeyAuth of(@NotNull Supplier<String> keySupplier, @NotNull String headerName) {
        Objects.requireNonNull(keySupplier, "keySupplier must not be null");
        Objects.requireNonNull(headerName, "headerName must not be null");
        return new ApiKeyAuth(keySupplier, headerName);
    }

    @Override
    @NotNull
    public String getAuthHeaderValue() {
        String key = keySupplier.get();
        Objects.requireNonNull(key, "key supplier returned null");
        return key;
    }

    @Override
    @NotNull
    public String getAuthHeaderName() {
        return headerName;
    }
}
