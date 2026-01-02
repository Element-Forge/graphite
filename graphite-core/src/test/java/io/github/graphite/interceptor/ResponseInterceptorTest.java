package io.github.graphite.interceptor;

import io.github.graphite.http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ResponseInterceptorTest {

    @Test
    void interceptorCanModifyResponse() {
        ResponseInterceptor interceptor = (response, chain) -> {
            HttpResponse modified = HttpResponse.builder()
                    .statusCode(response.statusCode())
                    .body(response.body())
                    .header("X-Added", "value")
                    .duration(response.duration())
                    .build();
            return chain.proceed(modified);
        };

        HttpResponse original = HttpResponse.builder()
                .statusCode(200)
                .body("{}")
                .duration(Duration.ZERO)
                .build();
        HttpResponse result = interceptor.intercept(original, resp -> resp);

        assertEquals("value", result.headers().get("X-Added"));
    }

    @Test
    void interceptorCanPassThroughUnmodified() {
        ResponseInterceptor interceptor = (response, chain) -> chain.proceed(response);

        HttpResponse original = HttpResponse.builder()
                .statusCode(200)
                .body("{}")
                .duration(Duration.ZERO)
                .build();
        HttpResponse result = interceptor.intercept(original, resp -> resp);

        assertSame(original, result);
    }

    @Test
    void chainCanBeUsedAsLambda() {
        ResponseInterceptor.Chain chain = response -> HttpResponse.builder()
                .statusCode(response.statusCode())
                .body(response.body() + "-modified")
                .duration(response.duration())
                .build();

        HttpResponse original = HttpResponse.builder()
                .statusCode(200)
                .body("original")
                .duration(Duration.ZERO)
                .build();
        HttpResponse result = chain.proceed(original);

        assertEquals("original-modified", result.body());
    }

    @Test
    void multipleInterceptorsChain() {
        ResponseInterceptor first = (response, chain) -> {
            HttpResponse modified = HttpResponse.builder()
                    .statusCode(response.statusCode())
                    .body(response.body())
                    .header("First", "1")
                    .duration(response.duration())
                    .build();
            return chain.proceed(modified);
        };

        ResponseInterceptor second = (response, chain) -> {
            HttpResponse modified = HttpResponse.builder()
                    .statusCode(response.statusCode())
                    .body(response.body())
                    .headers(response.headers())
                    .header("Second", "2")
                    .duration(response.duration())
                    .build();
            return chain.proceed(modified);
        };

        HttpResponse original = HttpResponse.builder()
                .statusCode(200)
                .body("{}")
                .duration(Duration.ZERO)
                .build();

        // Simulate chaining: first -> second -> identity
        HttpResponse result = first.intercept(original, resp ->
                second.intercept(resp, r -> r));

        assertEquals("1", result.headers().get("First"));
        assertEquals("2", result.headers().get("Second"));
    }
}
