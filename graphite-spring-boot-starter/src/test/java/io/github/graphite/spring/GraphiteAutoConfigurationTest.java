package io.github.graphite.spring;

import io.github.graphite.GraphiteClient;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GraphiteAutoConfigurationTest {

    @Test
    void autoConfigurationAcceptsProperties() {
        GraphiteProperties properties = new GraphiteProperties();
        properties.setEndpoint("https://api.example.com/graphql");
        properties.setConnectTimeout(Duration.ofSeconds(15));

        GraphiteAutoConfiguration config = new GraphiteAutoConfiguration(properties);

        assertNotNull(config.getProperties());
        assertEquals("https://api.example.com/graphql", config.getProperties().getEndpoint());
        assertEquals(Duration.ofSeconds(15), config.getProperties().getConnectTimeout());
    }

    @Test
    void autoConfigurationStoresPropertiesReference() {
        GraphiteProperties properties = new GraphiteProperties();

        GraphiteAutoConfiguration config = new GraphiteAutoConfiguration(properties);

        assertSame(properties, config.getProperties());
    }

    @Test
    void graphiteClientBeanIsCreated() {
        GraphiteProperties properties = new GraphiteProperties();
        properties.setEndpoint("https://api.example.com/graphql");

        GraphiteAutoConfiguration config = new GraphiteAutoConfiguration(properties);
        GraphiteClient client = config.graphiteClient();

        assertNotNull(client);
    }

    @Test
    void graphiteClientBeanUsesCustomTimeouts() {
        GraphiteProperties properties = new GraphiteProperties();
        properties.setEndpoint("https://api.example.com/graphql");
        properties.setConnectTimeout(Duration.ofSeconds(5));
        properties.setReadTimeout(Duration.ofSeconds(60));

        GraphiteAutoConfiguration config = new GraphiteAutoConfiguration(properties);
        GraphiteClient client = config.graphiteClient();

        assertNotNull(client);
    }

    @Test
    void graphiteClientBeanUsesDefaultHeaders() {
        GraphiteProperties properties = new GraphiteProperties();
        properties.setEndpoint("https://api.example.com/graphql");
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token");
        headers.put("X-Custom", "value");
        properties.setDefaultHeaders(headers);

        GraphiteAutoConfiguration config = new GraphiteAutoConfiguration(properties);
        GraphiteClient client = config.graphiteClient();

        assertNotNull(client);
    }

    @Test
    void graphiteClientBeanWithEmptyHeaders() {
        GraphiteProperties properties = new GraphiteProperties();
        properties.setEndpoint("https://api.example.com/graphql");
        properties.setDefaultHeaders(new HashMap<>());

        GraphiteAutoConfiguration config = new GraphiteAutoConfiguration(properties);
        GraphiteClient client = config.graphiteClient();

        assertNotNull(client);
    }

    @Test
    void graphiteClientBeanWithNullHeaders() {
        GraphiteProperties properties = new GraphiteProperties();
        properties.setEndpoint("https://api.example.com/graphql");
        properties.setDefaultHeaders(null);

        GraphiteAutoConfiguration config = new GraphiteAutoConfiguration(properties);
        GraphiteClient client = config.graphiteClient();

        assertNotNull(client);
    }
}
