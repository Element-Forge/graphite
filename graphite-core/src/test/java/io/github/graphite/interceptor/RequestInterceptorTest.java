package io.github.graphite.interceptor;

import io.github.graphite.http.HttpRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestInterceptorTest {

    @Test
    void interceptorCanModifyRequest() {
        RequestInterceptor interceptor = (request, chain) -> {
            HttpRequest modified = HttpRequest.builder()
                    .body(request.body())
                    .headers(request.headers())
                    .header("X-Added", "value")
                    .build();
            return chain.proceed(modified);
        };

        HttpRequest original = HttpRequest.builder().body("{}").build();
        HttpRequest result = interceptor.intercept(original, req -> req);

        assertEquals("value", result.headers().get("X-Added"));
    }

    @Test
    void interceptorCanPassThroughUnmodified() {
        RequestInterceptor interceptor = (request, chain) -> chain.proceed(request);

        HttpRequest original = HttpRequest.builder().body("{}").header("Key", "Value").build();
        HttpRequest result = interceptor.intercept(original, req -> req);

        assertSame(original, result);
    }

    @Test
    void chainCanBeUsedAsLambda() {
        RequestInterceptor.Chain chain = request -> HttpRequest.builder()
                .body(request.body() + "-modified")
                .build();

        HttpRequest original = HttpRequest.builder().body("original").build();
        HttpRequest result = chain.proceed(original);

        assertEquals("original-modified", result.body());
    }

    @Test
    void multipleInterceptorsChain() {
        RequestInterceptor first = (request, chain) -> {
            HttpRequest modified = HttpRequest.builder()
                    .body(request.body())
                    .header("First", "1")
                    .build();
            return chain.proceed(modified);
        };

        RequestInterceptor second = (request, chain) -> {
            HttpRequest modified = HttpRequest.builder()
                    .body(request.body())
                    .headers(request.headers())
                    .header("Second", "2")
                    .build();
            return chain.proceed(modified);
        };

        HttpRequest original = HttpRequest.builder().body("{}").build();

        // Simulate chaining: first -> second -> identity
        HttpRequest result = first.intercept(original, req ->
                second.intercept(req, r -> r));

        assertEquals("1", result.headers().get("First"));
        assertEquals("2", result.headers().get("Second"));
    }
}
