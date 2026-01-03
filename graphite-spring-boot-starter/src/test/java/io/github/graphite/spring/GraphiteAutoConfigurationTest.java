package io.github.graphite.spring;

import org.junit.jupiter.api.Test;

import java.time.Duration;

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
}
