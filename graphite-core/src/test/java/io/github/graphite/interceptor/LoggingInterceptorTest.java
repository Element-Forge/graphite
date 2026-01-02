package io.github.graphite.interceptor;

import io.github.graphite.http.HttpRequest;
import io.github.graphite.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoggingInterceptorTest {

    @Test
    void createReturnsInterceptor() {
        LoggingInterceptor interceptor = LoggingInterceptor.create();

        assertNotNull(interceptor);
    }

    @Test
    void createWithLoggerReturnsInterceptor() {
        Logger logger = mock(Logger.class);
        LoggingInterceptor interceptor = LoggingInterceptor.create(logger);

        assertNotNull(interceptor);
    }

    @Test
    void builderCreatesInterceptor() {
        Logger logger = mock(Logger.class);

        LoggingInterceptor interceptor = LoggingInterceptor.builder()
                .logger(logger)
                .logRequestBody(true)
                .logResponseBody(true)
                .build();

        assertNotNull(interceptor);
    }

    @Test
    void interceptRequestLogsWhenDebugEnabled() {
        Logger logger = mock(Logger.class);
        when(logger.isDebugEnabled()).thenReturn(true);

        LoggingInterceptor interceptor = LoggingInterceptor.create(logger);
        HttpRequest request = HttpRequest.builder().body("{}").build();

        HttpRequest result = interceptor.intercept(request, req -> req);

        assertSame(request, result);
        verify(logger).isDebugEnabled();
        verify(logger).debug(anyString(), anyInt());
    }

    @Test
    void interceptRequestDoesNotLogWhenDebugDisabled() {
        Logger logger = mock(Logger.class);
        when(logger.isDebugEnabled()).thenReturn(false);

        LoggingInterceptor interceptor = LoggingInterceptor.create(logger);
        HttpRequest request = HttpRequest.builder().body("{}").build();

        HttpRequest result = interceptor.intercept(request, req -> req);

        assertSame(request, result);
        verify(logger).isDebugEnabled();
        verify(logger, never()).debug(anyString(), anyInt());
    }

    @Test
    void interceptRequestLogsBodyWhenTraceEnabled() {
        Logger logger = mock(Logger.class);
        when(logger.isDebugEnabled()).thenReturn(true);
        when(logger.isTraceEnabled()).thenReturn(true);

        LoggingInterceptor interceptor = LoggingInterceptor.builder()
                .logger(logger)
                .logRequestBody(true)
                .build();
        HttpRequest request = HttpRequest.builder().body("{\"test\": true}").build();

        interceptor.intercept(request, req -> req);

        verify(logger).trace(eq("Request body: {}"), eq("{\"test\": true}"));
    }

    @Test
    void interceptResponseLogsWhenDebugEnabled() {
        Logger logger = mock(Logger.class);
        when(logger.isDebugEnabled()).thenReturn(true);

        LoggingInterceptor interceptor = LoggingInterceptor.create(logger);
        HttpResponse response = HttpResponse.builder()
                .statusCode(200)
                .body("{}")
                .duration(Duration.ofMillis(150))
                .build();

        HttpResponse result = interceptor.intercept(response, resp -> resp);

        assertSame(response, result);
        verify(logger).debug(eq("GraphQL Response: {} in {}ms"), eq(200), eq(150L));
    }

    @Test
    void interceptResponseDoesNotLogWhenDebugDisabled() {
        Logger logger = mock(Logger.class);
        when(logger.isDebugEnabled()).thenReturn(false);

        LoggingInterceptor interceptor = LoggingInterceptor.create(logger);
        HttpResponse response = HttpResponse.builder()
                .statusCode(200)
                .body("{}")
                .duration(Duration.ZERO)
                .build();

        HttpResponse result = interceptor.intercept(response, resp -> resp);

        assertSame(response, result);
        verify(logger, never()).debug(anyString(), anyInt(), anyLong());
    }

    @Test
    void interceptResponseLogsBodyWhenTraceEnabled() {
        Logger logger = mock(Logger.class);
        when(logger.isDebugEnabled()).thenReturn(true);
        when(logger.isTraceEnabled()).thenReturn(true);

        LoggingInterceptor interceptor = LoggingInterceptor.builder()
                .logger(logger)
                .logResponseBody(true)
                .build();
        HttpResponse response = HttpResponse.builder()
                .statusCode(200)
                .body("{\"data\": {}}")
                .duration(Duration.ZERO)
                .build();

        interceptor.intercept(response, resp -> resp);

        verify(logger).trace(eq("Response body: {}"), eq("{\"data\": {}}"));
    }

    @Test
    void builderRejectsNullLogger() {
        assertThrows(NullPointerException.class, () ->
                LoggingInterceptor.builder().logger(null));
    }

    @Test
    void implementsBothInterceptorInterfaces() {
        LoggingInterceptor interceptor = LoggingInterceptor.create();

        assertInstanceOf(RequestInterceptor.class, interceptor);
        assertInstanceOf(ResponseInterceptor.class, interceptor);
    }
}
