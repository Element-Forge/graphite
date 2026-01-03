package io.github.graphite.maven;

import io.github.graphite.codegen.SchemaIntrospector;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

/**
 * Maven plugin goal for downloading a GraphQL schema via introspection.
 *
 * <p>Connects to a GraphQL endpoint, executes an introspection query,
 * and saves the resulting schema in SDL format.</p>
 *
 * <p>Example usage in pom.xml:</p>
 * <pre>{@code
 * <plugin>
 *     <groupId>io.github.graphite</groupId>
 *     <artifactId>graphite-maven-plugin</artifactId>
 *     <version>1.0.0</version>
 *     <executions>
 *         <execution>
 *             <goals>
 *                 <goal>introspect</goal>
 *             </goals>
 *         </execution>
 *     </executions>
 *     <configuration>
 *         <endpoint>https://api.example.com/graphql</endpoint>
 *         <outputFile>${project.basedir}/src/main/graphql/schema.graphqls</outputFile>
 *         <headers>
 *             <Authorization>Bearer ${env.API_TOKEN}</Authorization>
 *         </headers>
 *     </configuration>
 * </plugin>
 * }</pre>
 */
@Mojo(name = "introspect")
public class IntrospectMojo extends AbstractMojo {

    /**
     * The GraphQL endpoint URL for introspection.
     */
    @Parameter(property = "graphite.endpoint", required = true)
    private String endpoint;

    /**
     * The output file path for the downloaded schema.
     */
    @Parameter(property = "graphite.outputFile", required = true)
    private File outputFile;

    /**
     * HTTP headers to include in the introspection request.
     */
    @Parameter
    private Map<String, String> headers;

    /**
     * Request timeout in seconds.
     * Defaults to 30 seconds.
     */
    @Parameter(property = "graphite.timeoutSeconds", defaultValue = "30")
    private int timeoutSeconds;

    /**
     * Creates a new IntrospectMojo.
     */
    public IntrospectMojo() {
        // Default constructor for Maven
    }

    @Override
    public void execute() throws MojoExecutionException {
        validateConfiguration();

        getLog().info("Graphite: Introspecting schema from " + endpoint);

        SchemaIntrospector.Builder builder = SchemaIntrospector.builder()
                .endpoint(endpoint)
                .timeout(Duration.ofSeconds(timeoutSeconds));

        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.header(entry.getKey(), entry.getValue());
            }
        }

        try {
            Path outputPath = outputFile.toPath();

            // Ensure parent directories exist
            if (outputPath.getParent() != null) {
                java.nio.file.Files.createDirectories(outputPath.getParent());
            }

            SchemaIntrospector introspector = builder.build();
            introspector.fetchSchemaToFile(outputPath);

            getLog().info("Graphite: Schema downloaded to " + outputPath);

        } catch (IOException e) {
            throw new MojoExecutionException("Failed to download GraphQL schema", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MojoExecutionException("Schema introspection was interrupted", e);
        }
    }

    private void validateConfiguration() throws MojoExecutionException {
        if (endpoint == null || endpoint.isBlank()) {
            throw new MojoExecutionException("endpoint is required");
        }
        if (outputFile == null) {
            throw new MojoExecutionException("outputFile is required");
        }
        if (timeoutSeconds <= 0) {
            throw new MojoExecutionException("timeoutSeconds must be positive");
        }
    }
}
