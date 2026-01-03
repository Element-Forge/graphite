package io.github.graphite.spring;

import io.github.graphite.GraphiteClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot auto-configuration for Graphite GraphQL client.
 *
 * <p>This auto-configuration is enabled when:</p>
 * <ul>
 *   <li>The {@code graphite.enabled} property is {@code true} (the default)</li>
 *   <li>The {@link GraphiteClient} class is on the classpath</li>
 * </ul>
 *
 * <p>A {@link GraphiteClient} bean is created when:</p>
 * <ul>
 *   <li>The {@code graphite.endpoint} property is set</li>
 *   <li>No other {@link GraphiteClient} bean is already defined</li>
 * </ul>
 */
@Configuration
@ConditionalOnClass(GraphiteClient.class)
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
     * Creates a {@link GraphiteClient} bean configured from the application properties.
     *
     * <p>This bean is only created when:</p>
     * <ul>
     *   <li>The {@code graphite.endpoint} property is set</li>
     *   <li>No other {@link GraphiteClient} bean is already defined</li>
     * </ul>
     *
     * @return the configured GraphiteClient
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "graphite", name = "endpoint")
    public GraphiteClient graphiteClient() {
        GraphiteClient.Builder builder = GraphiteClient.builder()
                .endpoint(properties.getEndpoint())
                .connectTimeout(properties.getConnectTimeout())
                .readTimeout(properties.getReadTimeout());

        if (properties.getDefaultHeaders() != null && !properties.getDefaultHeaders().isEmpty()) {
            builder.defaultHeaders(properties.getDefaultHeaders());
        }

        return builder.build();
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
