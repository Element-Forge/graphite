package io.github.graphite.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Fluent builder for constructing GraphQL response JSON.
 *
 * <p>This builder provides a convenient way to create properly formatted
 * GraphQL responses for use with {@link GraphiteMockServer}.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * String response = GraphiteResponseBuilder.success()
 *     .data("user", Map.of("id", "1", "name", "John"))
 *     .build();
 *
 * String errorResponse = GraphiteResponseBuilder.error("Not found")
 *     .errorCode("NOT_FOUND")
 *     .errorPath("user")
 *     .build();
 * }</pre>
 */
public final class GraphiteResponseBuilder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Map<String, Object> data = new LinkedHashMap<>();
    private final List<ErrorBuilder> errors = new ArrayList<>();
    private final Map<String, Object> extensions = new LinkedHashMap<>();

    private GraphiteResponseBuilder() {
    }

    /**
     * Creates a new builder for a successful response.
     *
     * @return a new builder
     */
    public static GraphiteResponseBuilder success() {
        return new GraphiteResponseBuilder();
    }

    /**
     * Creates a new builder with an initial error.
     *
     * @param message the error message
     * @return a new builder with the error
     */
    public static GraphiteResponseBuilder error(String message) {
        Objects.requireNonNull(message, "message must not be null");
        GraphiteResponseBuilder builder = new GraphiteResponseBuilder();
        builder.errors.add(new ErrorBuilder(message));
        return builder;
    }

    /**
     * Adds a data field to the response.
     *
     * @param key the field name
     * @param value the field value
     * @return this builder
     */
    public GraphiteResponseBuilder data(String key, Object value) {
        Objects.requireNonNull(key, "key must not be null");
        data.put(key, value);
        return this;
    }

    /**
     * Sets the entire data object.
     *
     * @param dataMap the data map
     * @return this builder
     */
    public GraphiteResponseBuilder data(Map<String, Object> dataMap) {
        Objects.requireNonNull(dataMap, "dataMap must not be null");
        data.clear();
        data.putAll(dataMap);
        return this;
    }

    /**
     * Adds a null data field (useful for partial errors).
     *
     * @param key the field name
     * @return this builder
     */
    public GraphiteResponseBuilder nullData(String key) {
        Objects.requireNonNull(key, "key must not be null");
        data.put(key, null);
        return this;
    }

    /**
     * Adds an error to the response.
     *
     * @param message the error message
     * @return this builder
     */
    public GraphiteResponseBuilder addError(String message) {
        Objects.requireNonNull(message, "message must not be null");
        errors.add(new ErrorBuilder(message));
        return this;
    }

    /**
     * Adds an error with additional details.
     *
     * @param message the error message
     * @param code the error code
     * @return this builder
     */
    public GraphiteResponseBuilder addError(String message, String code) {
        Objects.requireNonNull(message, "message must not be null");
        ErrorBuilder error = new ErrorBuilder(message);
        if (code != null) {
            error.code(code);
        }
        errors.add(error);
        return this;
    }

    /**
     * Adds an error with path information.
     *
     * @param message the error message
     * @param path the error path elements
     * @return this builder
     */
    public GraphiteResponseBuilder addErrorWithPath(String message, Object... path) {
        Objects.requireNonNull(message, "message must not be null");
        ErrorBuilder error = new ErrorBuilder(message);
        for (Object p : path) {
            error.path(p);
        }
        errors.add(error);
        return this;
    }

    /**
     * Sets an error code on the last added error.
     *
     * @param code the error code
     * @return this builder
     */
    public GraphiteResponseBuilder errorCode(String code) {
        if (!errors.isEmpty()) {
            errors.get(errors.size() - 1).code(code);
        }
        return this;
    }

    /**
     * Adds a path element to the last added error.
     *
     * @param pathElement the path element (String for field name, Integer for array index)
     * @return this builder
     */
    public GraphiteResponseBuilder errorPath(Object pathElement) {
        if (!errors.isEmpty()) {
            errors.get(errors.size() - 1).path(pathElement);
        }
        return this;
    }

    /**
     * Adds location information to the last added error.
     *
     * @param line the line number
     * @param column the column number
     * @return this builder
     */
    public GraphiteResponseBuilder errorLocation(int line, int column) {
        if (!errors.isEmpty()) {
            errors.get(errors.size() - 1).location(line, column);
        }
        return this;
    }

    /**
     * Adds an extension field to the response.
     *
     * @param key the extension key
     * @param value the extension value
     * @return this builder
     */
    public GraphiteResponseBuilder extension(String key, Object value) {
        Objects.requireNonNull(key, "key must not be null");
        extensions.put(key, value);
        return this;
    }

    /**
     * Builds the JSON response string.
     *
     * @return the JSON response
     */
    public String build() {
        try {
            ObjectNode root = MAPPER.createObjectNode();

            // Add data
            if (!data.isEmpty() || errors.isEmpty()) {
                ObjectNode dataNode = MAPPER.valueToTree(data);
                root.set("data", dataNode);
            }

            // Add errors
            if (!errors.isEmpty()) {
                ArrayNode errorsNode = root.putArray("errors");
                for (ErrorBuilder error : errors) {
                    errorsNode.add(error.toNode());
                }
            }

            // Add extensions
            if (!extensions.isEmpty()) {
                ObjectNode extNode = MAPPER.valueToTree(extensions);
                root.set("extensions", extNode);
            }

            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to build JSON response", e);
        }
    }

    /**
     * Internal error builder.
     */
    private static class ErrorBuilder {
        private final String message;
        private final List<Object> path = new ArrayList<>();
        private final List<Map<String, Integer>> locations = new ArrayList<>();
        private final Map<String, Object> extensions = new LinkedHashMap<>();

        ErrorBuilder(String message) {
            this.message = message;
        }

        ErrorBuilder code(String code) {
            extensions.put("code", code);
            return this;
        }

        ErrorBuilder path(Object element) {
            path.add(element);
            return this;
        }

        ErrorBuilder location(int line, int column) {
            locations.add(Map.of("line", line, "column", column));
            return this;
        }

        ObjectNode toNode() {
            ObjectNode node = MAPPER.createObjectNode();
            node.put("message", message);

            if (!path.isEmpty()) {
                ArrayNode pathNode = node.putArray("path");
                for (Object p : path) {
                    if (p instanceof Integer) {
                        pathNode.add((Integer) p);
                    } else {
                        pathNode.add(p.toString());
                    }
                }
            }

            if (!locations.isEmpty()) {
                ArrayNode locNode = node.putArray("locations");
                for (Map<String, Integer> loc : locations) {
                    ObjectNode locObj = MAPPER.createObjectNode();
                    locObj.put("line", loc.get("line"));
                    locObj.put("column", loc.get("column"));
                    locNode.add(locObj);
                }
            }

            if (!extensions.isEmpty()) {
                ObjectNode extNode = MAPPER.valueToTree(extensions);
                node.set("extensions", extNode);
            }

            return node;
        }
    }
}
