package io.github.graphite.codegen;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.introspection.IntrospectionResultToSchema;
import graphql.language.Document;
import graphql.schema.idl.SchemaPrinter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Fetches a GraphQL schema from a server using introspection.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * SchemaIntrospector introspector = SchemaIntrospector.builder()
 *     .endpoint("https://api.example.com/graphql")
 *     .header("Authorization", "Bearer token")
 *     .build();
 *
 * String schema = introspector.fetchSchema();
 * introspector.fetchSchemaToFile(Path.of("schema.graphqls"));
 * }</pre>
 */
public final class SchemaIntrospector {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaIntrospector.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String INTROSPECTION_QUERY = """
            query IntrospectionQuery {
              __schema {
                queryType { name }
                mutationType { name }
                subscriptionType { name }
                types {
                  ...FullType
                }
                directives {
                  name
                  description
                  locations
                  args {
                    ...InputValue
                  }
                }
              }
            }

            fragment FullType on __Type {
              kind
              name
              description
              fields(includeDeprecated: true) {
                name
                description
                args {
                  ...InputValue
                }
                type {
                  ...TypeRef
                }
                isDeprecated
                deprecationReason
              }
              inputFields {
                ...InputValue
              }
              interfaces {
                ...TypeRef
              }
              enumValues(includeDeprecated: true) {
                name
                description
                isDeprecated
                deprecationReason
              }
              possibleTypes {
                ...TypeRef
              }
            }

            fragment InputValue on __InputValue {
              name
              description
              type {
                ...TypeRef
              }
              defaultValue
            }

            fragment TypeRef on __Type {
              kind
              name
              ofType {
                kind
                name
                ofType {
                  kind
                  name
                  ofType {
                    kind
                    name
                    ofType {
                      kind
                      name
                      ofType {
                        kind
                        name
                        ofType {
                          kind
                          name
                          ofType {
                            kind
                            name
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
            """;

    private final String endpoint;
    private final Map<String, String> headers;
    private final Duration timeout;
    private final HttpClient httpClient;

    private SchemaIntrospector(Builder builder) {
        this.endpoint = builder.endpoint;
        this.headers = Map.copyOf(builder.headers);
        this.timeout = builder.timeout;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();
    }

    /**
     * Creates a new builder for SchemaIntrospector.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fetches the schema from the configured endpoint via introspection.
     *
     * @return the schema in SDL format
     * @throws IOException if the request fails
     * @throws InterruptedException if the request is interrupted
     */
    @NotNull
    public String fetchSchema() throws IOException, InterruptedException {
        LOG.info("Fetching schema via introspection from: {}", endpoint);

        String requestBody = OBJECT_MAPPER.writeValueAsString(
                Map.of("query", INTROSPECTION_QUERY)
        );

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));

        headers.forEach(requestBuilder::header);

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Introspection request failed with status " + response.statusCode()
                    + ": " + response.body());
        }

        return convertIntrospectionResultToSdl(response.body());
    }

    /**
     * Fetches the schema and writes it to a file.
     *
     * @param outputPath the path to write the schema to
     * @throws IOException if the request or file write fails
     * @throws InterruptedException if the request is interrupted
     */
    public void fetchSchemaToFile(@NotNull Path outputPath) throws IOException, InterruptedException {
        Objects.requireNonNull(outputPath, "outputPath must not be null");

        String schema = fetchSchema();
        Files.createDirectories(outputPath.getParent());
        Files.writeString(outputPath, schema);

        LOG.info("Schema written to: {}", outputPath);
    }

    @SuppressWarnings("unchecked")
    private String convertIntrospectionResultToSdl(String jsonResponse) throws IOException {
        JsonNode root = OBJECT_MAPPER.readTree(jsonResponse);

        JsonNode errors = root.get("errors");
        if (errors != null && !errors.isEmpty()) {
            throw new IOException("Introspection query returned errors: " + errors);
        }

        JsonNode data = root.get("data");
        if (data == null) {
            throw new IOException("Introspection response missing 'data' field");
        }

        Map<String, Object> introspectionResult = OBJECT_MAPPER.convertValue(data, Map.class);

        Document schemaDocument = new IntrospectionResultToSchema().createSchemaDefinition(introspectionResult);

        SchemaPrinter.Options options = SchemaPrinter.Options.defaultOptions()
                .includeDirectives(true)
                .includeSchemaDefinition(true);

        return new SchemaPrinter(options).print(schemaDocument);
    }

    /**
     * Builder for {@link SchemaIntrospector}.
     */
    public static final class Builder {

        private String endpoint;
        private final Map<String, String> headers = new HashMap<>();
        private Duration timeout = Duration.ofSeconds(30);

        private Builder() {
        }

        /**
         * Sets the GraphQL endpoint URL.
         *
         * @param endpoint the endpoint URL
         * @return this builder
         */
        @NotNull
        public Builder endpoint(@NotNull String endpoint) {
            this.endpoint = Objects.requireNonNull(endpoint, "endpoint must not be null");
            return this;
        }

        /**
         * Adds a header to include in the introspection request.
         *
         * @param name the header name
         * @param value the header value
         * @return this builder
         */
        @NotNull
        public Builder header(@NotNull String name, @NotNull String value) {
            Objects.requireNonNull(name, "header name must not be null");
            Objects.requireNonNull(value, "header value must not be null");
            this.headers.put(name, value);
            return this;
        }

        /**
         * Sets all headers to include in the introspection request.
         *
         * @param headers the headers map
         * @return this builder
         */
        @NotNull
        public Builder headers(@NotNull Map<String, String> headers) {
            Objects.requireNonNull(headers, "headers must not be null");
            this.headers.clear();
            this.headers.putAll(headers);
            return this;
        }

        /**
         * Sets the request timeout.
         *
         * @param timeout the timeout duration
         * @return this builder
         */
        @NotNull
        public Builder timeout(@NotNull Duration timeout) {
            this.timeout = Objects.requireNonNull(timeout, "timeout must not be null");
            return this;
        }

        /**
         * Builds the SchemaIntrospector.
         *
         * @return the configured SchemaIntrospector
         * @throws IllegalStateException if endpoint is not set
         */
        @NotNull
        public SchemaIntrospector build() {
            if (endpoint == null || endpoint.isBlank()) {
                throw new IllegalStateException("endpoint is required");
            }
            return new SchemaIntrospector(this);
        }
    }
}
