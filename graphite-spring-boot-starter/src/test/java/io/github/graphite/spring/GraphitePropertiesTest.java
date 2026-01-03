package io.github.graphite.spring;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GraphitePropertiesTest {

    @Test
    void defaultValues() {
        GraphiteProperties properties = new GraphiteProperties();

        assertNull(properties.getEndpoint());
        assertEquals(Duration.ofSeconds(10), properties.getConnectTimeout());
        assertEquals(Duration.ofSeconds(30), properties.getReadTimeout());
        assertTrue(properties.getDefaultHeaders().isEmpty());
        assertTrue(properties.isEnabled());
    }

    @Test
    void setEndpoint() {
        GraphiteProperties properties = new GraphiteProperties();
        properties.setEndpoint("https://api.example.com/graphql");

        assertEquals("https://api.example.com/graphql", properties.getEndpoint());
    }

    @Test
    void setConnectTimeout() {
        GraphiteProperties properties = new GraphiteProperties();
        properties.setConnectTimeout(Duration.ofSeconds(5));

        assertEquals(Duration.ofSeconds(5), properties.getConnectTimeout());
    }

    @Test
    void setReadTimeout() {
        GraphiteProperties properties = new GraphiteProperties();
        properties.setReadTimeout(Duration.ofMinutes(1));

        assertEquals(Duration.ofMinutes(1), properties.getReadTimeout());
    }

    @Test
    void setDefaultHeaders() {
        GraphiteProperties properties = new GraphiteProperties();
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token");
        headers.put("X-Custom", "value");
        properties.setDefaultHeaders(headers);

        assertEquals(2, properties.getDefaultHeaders().size());
        assertEquals("Bearer token", properties.getDefaultHeaders().get("Authorization"));
        assertEquals("value", properties.getDefaultHeaders().get("X-Custom"));
    }

    @Test
    void setEnabled() {
        GraphiteProperties properties = new GraphiteProperties();
        properties.setEnabled(false);

        assertFalse(properties.isEnabled());
    }

    @Test
    void setEnabledTrue() {
        GraphiteProperties properties = new GraphiteProperties();
        properties.setEnabled(false);
        properties.setEnabled(true);

        assertTrue(properties.isEnabled());
    }
}
