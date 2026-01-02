package io.github.graphite.interceptor;

import io.github.graphite.http.HttpRequest;
import io.github.graphite.http.HttpResponse;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class MetricsInterceptorTest {

    private MeterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
    }

    @Test
    void createReturnsInterceptor() {
        MetricsInterceptor interceptor = MetricsInterceptor.create(registry);

        assertNotNull(interceptor);
    }

    @Test
    void builderCreatesInterceptor() {
        MetricsInterceptor interceptor = MetricsInterceptor.builder(registry)
                .requestCounterName("custom.requests")
                .responseTimerName("custom.responses")
                .build();

        assertNotNull(interceptor);
    }

    @Test
    void interceptRequestIncrementsCounter() {
        MetricsInterceptor interceptor = MetricsInterceptor.create(registry);
        HttpRequest request = HttpRequest.builder().body("{}").build();

        interceptor.intercept(request, req -> req);

        Counter counter = registry.find("graphite.requests").counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count());
    }

    @Test
    void interceptRequestIncrementsCounterMultipleTimes() {
        MetricsInterceptor interceptor = MetricsInterceptor.create(registry);
        HttpRequest request = HttpRequest.builder().body("{}").build();

        interceptor.intercept(request, req -> req);
        interceptor.intercept(request, req -> req);
        interceptor.intercept(request, req -> req);

        Counter counter = registry.find("graphite.requests").counter();
        assertNotNull(counter);
        assertEquals(3.0, counter.count());
    }

    @Test
    void interceptRequestPassesRequestToChain() {
        MetricsInterceptor interceptor = MetricsInterceptor.create(registry);
        HttpRequest request = HttpRequest.builder().body("test").build();

        HttpRequest result = interceptor.intercept(request, req -> req);

        assertSame(request, result);
    }

    @Test
    void interceptResponseRecordsDuration() {
        MetricsInterceptor interceptor = MetricsInterceptor.create(registry);
        HttpResponse response = HttpResponse.builder()
                .statusCode(200)
                .body("{}")
                .duration(Duration.ofMillis(150))
                .build();

        interceptor.intercept(response, resp -> resp);

        Timer timer = registry.find("graphite.responses").tag("status", "200").timer();
        assertNotNull(timer);
        assertEquals(1, timer.count());
        assertTrue(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS) >= 150);
    }

    @Test
    void interceptResponseTagsWithStatusCode() {
        MetricsInterceptor interceptor = MetricsInterceptor.create(registry);

        HttpResponse ok = HttpResponse.builder()
                .statusCode(200)
                .body("{}")
                .duration(Duration.ofMillis(100))
                .build();
        HttpResponse error = HttpResponse.builder()
                .statusCode(500)
                .body("{}")
                .duration(Duration.ofMillis(200))
                .build();

        interceptor.intercept(ok, resp -> resp);
        interceptor.intercept(error, resp -> resp);

        Timer okTimer = registry.find("graphite.responses").tag("status", "200").timer();
        Timer errorTimer = registry.find("graphite.responses").tag("status", "500").timer();

        assertNotNull(okTimer);
        assertNotNull(errorTimer);
        assertEquals(1, okTimer.count());
        assertEquals(1, errorTimer.count());
    }

    @Test
    void interceptResponsePassesResponseToChain() {
        MetricsInterceptor interceptor = MetricsInterceptor.create(registry);
        HttpResponse response = HttpResponse.builder()
                .statusCode(200)
                .body("{}")
                .duration(Duration.ZERO)
                .build();

        HttpResponse result = interceptor.intercept(response, resp -> resp);

        assertSame(response, result);
    }

    @Test
    void customMetricNamesAreUsed() {
        MetricsInterceptor interceptor = MetricsInterceptor.builder(registry)
                .requestCounterName("my.requests")
                .responseTimerName("my.responses")
                .build();

        HttpRequest request = HttpRequest.builder().body("{}").build();
        HttpResponse response = HttpResponse.builder()
                .statusCode(200)
                .body("{}")
                .duration(Duration.ofMillis(50))
                .build();

        interceptor.intercept(request, req -> req);
        interceptor.intercept(response, resp -> resp);

        Counter counter = registry.find("my.requests").counter();
        Timer timer = registry.find("my.responses").tag("status", "200").timer();

        assertNotNull(counter);
        assertNotNull(timer);
        assertEquals(1.0, counter.count());
        assertEquals(1, timer.count());
    }

    @Test
    void builderRejectsNullRegistry() {
        assertThrows(NullPointerException.class, () ->
                MetricsInterceptor.builder(null));
    }

    @Test
    void builderRejectsNullRequestCounterName() {
        assertThrows(NullPointerException.class, () ->
                MetricsInterceptor.builder(registry).requestCounterName(null));
    }

    @Test
    void builderRejectsNullResponseTimerName() {
        assertThrows(NullPointerException.class, () ->
                MetricsInterceptor.builder(registry).responseTimerName(null));
    }

    @Test
    void implementsBothInterceptorInterfaces() {
        MetricsInterceptor interceptor = MetricsInterceptor.create(registry);

        assertInstanceOf(RequestInterceptor.class, interceptor);
        assertInstanceOf(ResponseInterceptor.class, interceptor);
    }
}
