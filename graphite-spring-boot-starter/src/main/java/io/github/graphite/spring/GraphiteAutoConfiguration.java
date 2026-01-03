package io.github.graphite.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot auto-configuration for Graphite GraphQL client.
 *
 * <p>This auto-configuration is enabled when the {@code graphite.enabled}
 * property is {@code true} (the default).</p>
 */
@Configuration
@EnableConfigurationProperties(GraphiteProperties.class)
@ConditionalOnProperty(prefix = "graphite", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GraphiteAutoConfiguration {

    private final GraphiteProperties properties;

    /**
     * Creates a new GraphiteAutoConfiguration.
     *
     * @param properties the Graphite configuration properties
     */
    public GraphiteAutoConfiguration(GraphiteProperties properties) {
        this.properties = properties;
    }

    /**
     * Returns the Graphite configuration properties.
     *
     * @return the properties
     */
    public GraphiteProperties getProperties() {
        return properties;
    }
}
