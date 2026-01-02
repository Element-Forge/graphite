package io.github.graphite.auth;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Authentication provider for Bearer token authentication.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Static token
 * AuthProvider auth = BearerTokenAuth.of("my-token");
 *
 * // Dynamic token (e.g., refreshable)
 * AuthProvider auth = BearerTokenAuth.of(() -> tokenService.getAccessToken());
 * }</pre>
 */
public final class BearerTokenAuth implements AuthProvider {

    private final Supplier<String> tokenSupplier;

    private BearerTokenAuth(Supplier<String> tokenSupplier) {
        this.tokenSupplier = tokenSupplier;
    }

    /**
     * Creates a Bearer token auth provider with a static token.
     *
     * @param token the bearer token
     * @return the auth provider
     */
    public static BearerTokenAuth of(@NotNull String token) {
        Objects.requireNonNull(token, "token must not be null");
        return new BearerTokenAuth(() -> token);
    }

    /**
     * Creates a Bearer token auth provider with a dynamic token supplier.
     *
     * <p>The supplier is called each time authentication is needed,
     * allowing for token refresh.</p>
     *
     * @param tokenSupplier the token supplier
     * @return the auth provider
     */
    public static BearerTokenAuth of(@NotNull Supplier<String> tokenSupplier) {
        Objects.requireNonNull(tokenSupplier, "tokenSupplier must not be null");
        return new BearerTokenAuth(tokenSupplier);
    }

    @Override
    @NotNull
    public String getAuthHeaderValue() {
        String token = tokenSupplier.get();
        Objects.requireNonNull(token, "token supplier returned null");
        return "Bearer " + token;
    }
}
