package io.github.graphite.codegen;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Loads GraphQL schema content from various sources.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Load from file
 * String schema = SchemaLoader.fromFile(Path.of("schema.graphqls"));
 *
 * // Load from classpath resource
 * String schema = SchemaLoader.fromClasspath("/schema.graphqls");
 * }</pre>
 */
public final class SchemaLoader {

    private SchemaLoader() {
        // Utility class
    }

    /**
     * Loads schema content from a file path.
     *
     * @param path the path to the schema file
     * @return the schema content as a string
     * @throws IOException if the file cannot be read
     * @throws IllegalArgumentException if the file does not exist or is not a regular file
     */
    @NotNull
    public static String fromFile(@NotNull Path path) throws IOException {
        Objects.requireNonNull(path, "path must not be null");

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Schema file does not exist: " + path);
        }

        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Schema path is not a file: " + path);
        }

        return Files.readString(path, StandardCharsets.UTF_8);
    }

    /**
     * Loads schema content from a classpath resource.
     *
     * @param resourcePath the classpath resource path (e.g., "/schema.graphqls")
     * @return the schema content as a string
     * @throws IllegalArgumentException if the resource does not exist
     * @throws IOException if the resource cannot be read
     */
    @NotNull
    public static String fromClasspath(@NotNull String resourcePath) throws IOException {
        Objects.requireNonNull(resourcePath, "resourcePath must not be null");

        try (InputStream inputStream = SchemaLoader.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Schema resource not found: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Loads schema content from a string.
     *
     * <p>This method validates that the content is not empty.</p>
     *
     * @param content the schema content
     * @return the schema content
     * @throws IllegalArgumentException if the content is empty or blank
     */
    @NotNull
    public static String fromString(@NotNull String content) {
        Objects.requireNonNull(content, "content must not be null");

        if (content.isBlank()) {
            throw new IllegalArgumentException("Schema content must not be empty");
        }

        return content;
    }
}
