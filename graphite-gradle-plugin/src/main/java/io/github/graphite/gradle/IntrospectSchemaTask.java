package io.github.graphite.gradle;

import io.github.graphite.codegen.SchemaIntrospector;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

/**
 * Gradle task that fetches a GraphQL schema via introspection query.
 *
 * <p>This task connects to a GraphQL endpoint, executes an introspection
 * query, and saves the resulting schema in SDL format.</p>
 */
public abstract class IntrospectSchemaTask extends DefaultTask {

    /**
     * Creates a new IntrospectSchemaTask.
     */
    public IntrospectSchemaTask() {
        setGroup("graphite");
        setDescription("Downloads GraphQL schema via introspection query.");
    }

    /**
     * The GraphQL endpoint URL for introspection.
     *
     * @return the endpoint property
     */
    @Input
    public abstract Property<String> getEndpoint();

    /**
     * HTTP headers to include in the introspection request.
     *
     * @return the headers property
     */
    @Input
    @Optional
    public abstract MapProperty<String, String> getHeaders();

    /**
     * The output file path for the downloaded schema.
     *
     * @return the output file property
     */
    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    /**
     * Request timeout in seconds.
     *
     * @return the timeout property
     */
    @Input
    @Optional
    public abstract Property<Integer> getTimeoutSeconds();

    /**
     * Executes the schema introspection.
     *
     * @throws IOException if the request fails
     * @throws InterruptedException if the request is interrupted
     */
    @TaskAction
    public void introspect() throws IOException, InterruptedException {
        String endpoint = getEndpoint().get();
        Path outputPath = getOutputFile().get().getAsFile().toPath();

        SchemaIntrospector.Builder builder = SchemaIntrospector.builder()
                .endpoint(endpoint);

        if (getHeaders().isPresent()) {
            Map<String, String> headers = getHeaders().get();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.header(entry.getKey(), entry.getValue());
            }
        }

        if (getTimeoutSeconds().isPresent()) {
            builder.timeout(Duration.ofSeconds(getTimeoutSeconds().get()));
        }

        SchemaIntrospector introspector = builder.build();
        introspector.fetchSchemaToFile(outputPath);

        getLogger().lifecycle("Graphite: Schema downloaded to {}", outputPath);
    }
}
