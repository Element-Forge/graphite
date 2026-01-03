package io.github.graphite.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for Graphite GraphQL client.
 *
 * <p>These properties can be configured in {@code application.properties} or
 * {@code application.yml} using the {@code graphite.*} prefix.</p>
 *
 * <p>Example configuration in {@code application.yml}:</p>
 * <pre>{@code
 * graphite:
 *   endpoint: https://api.example.com/graphql
 *   connect-timeout: 10s
 *   read-timeout: 30s
 *   default-headers:
 *     X-Custom-Header: value
 * }</pre>
 */
@ConfigurationProperties(prefix = "graphite")
public class GraphiteProperties {

    /**
     * The GraphQL endpoint URL.
     */
    private String endpoint;

    /**
     * Connection timeout for HTTP requests.
     * Default is 10 seconds.
     */
    private Duration connectTimeout = Duration.ofSeconds(10);

    /**
     * Read timeout for HTTP requests.
     * Default is 30 seconds.
     */
    private Duration readTimeout = Duration.ofSeconds(30);

    /**
     * Default headers to include in all requests.
     */
    private Map<String, String> defaultHeaders = new HashMap<>();

    /**
     * Whether to enable the Graphite client auto-configuration.
     * Default is true.
     */
    private boolean enabled = true;

    /**
     * Creates a new GraphiteProperties instance.
     */
    public GraphiteProperties() {
    }

    /**
     * Returns the GraphQL endpoint URL.
     *
     * @return the endpoint URL
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the GraphQL endpoint URL.
     *
     * @param endpoint the endpoint URL
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Returns the connection timeout.
     *
     * @return the connection timeout
     */
    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Sets the connection timeout.
     *
     * @param connectTimeout the connection timeout
     */
    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Returns the read timeout.
     *
     * @return the read timeout
     */
    public Duration getReadTimeout() {
        return readTimeout;
    }

    /**
     * Sets the read timeout.
     *
     * @param readTimeout the read timeout
     */
    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * Returns the default headers.
     *
     * @return map of default headers
     */
    public Map<String, String> getDefaultHeaders() {
        return defaultHeaders;
    }

    /**
     * Sets the default headers.
     *
     * @param defaultHeaders map of default headers
     */
    public void setDefaultHeaders(Map<String, String> defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
    }

    /**
     * Returns whether the auto-configuration is enabled.
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the auto-configuration is enabled.
     *
     * @param enabled true to enable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
