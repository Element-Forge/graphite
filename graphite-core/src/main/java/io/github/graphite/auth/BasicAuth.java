package io.github.graphite.auth;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Authentication provider for HTTP Basic authentication.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Static credentials
 * AuthProvider auth = BasicAuth.of("username", "password");
 *
 * // Dynamic credentials
 * AuthProvider auth = BasicAuth.of(
 *     () -> credentialService.getUsername(),
 *     () -> credentialService.getPassword()
 * );
 * }</pre>
 */
public final class BasicAuth implements AuthProvider {

    private final Supplier<String> usernameSupplier;
    private final Supplier<String> passwordSupplier;

    private BasicAuth(Supplier<String> usernameSupplier, Supplier<String> passwordSupplier) {
        this.usernameSupplier = usernameSupplier;
        this.passwordSupplier = passwordSupplier;
    }

    /**
     * Creates a Basic auth provider with static credentials.
     *
     * @param username the username
     * @param password the password
     * @return the auth provider
     */
    public static BasicAuth of(@NotNull String username, @NotNull String password) {
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(password, "password must not be null");
        return new BasicAuth(() -> username, () -> password);
    }

    /**
     * Creates a Basic auth provider with dynamic credential suppliers.
     *
     * @param usernameSupplier the username supplier
     * @param passwordSupplier the password supplier
     * @return the auth provider
     */
    public static BasicAuth of(
            @NotNull Supplier<String> usernameSupplier,
            @NotNull Supplier<String> passwordSupplier) {
        Objects.requireNonNull(usernameSupplier, "usernameSupplier must not be null");
        Objects.requireNonNull(passwordSupplier, "passwordSupplier must not be null");
        return new BasicAuth(usernameSupplier, passwordSupplier);
    }

    @Override
    @NotNull
    public String getAuthHeaderValue() {
        String username = usernameSupplier.get();
        String password = passwordSupplier.get();
        Objects.requireNonNull(username, "username supplier returned null");
        Objects.requireNonNull(password, "password supplier returned null");

        String credentials = username + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}
