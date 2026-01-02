package io.github.graphite.gradle;

import io.github.graphite.codegen.CodeGeneratorConfig;
import io.github.graphite.codegen.GraphiteCodeGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Gradle task that generates GraphQL client code from a schema file.
 *
 * <p>This task uses the Graphite code generator to produce type-safe
 * Java classes for querying a GraphQL API.</p>
 */
@CacheableTask
public abstract class GenerateGraphiteClientTask extends DefaultTask {

    /**
     * Creates a new GenerateGraphiteClientTask.
     */
    public GenerateGraphiteClientTask() {
        setGroup("graphite");
        setDescription("Generates GraphQL client code from the schema.");
    }

    /**
     * The path to the GraphQL schema file.
     *
     * @return the schema path property
     */
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getSchemaPath();

    /**
     * The base package name for generated classes.
     *
     * @return the package name property
     */
    @Input
    public abstract Property<String> getPackageName();

    /**
     * The output directory for generated source files.
     *
     * @return the output directory property
     */
    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    /**
     * Whether to generate builder classes for input types.
     *
     * @return the generate builders property
     */
    @Input
    @Optional
    public abstract Property<Boolean> getGenerateBuilders();

    /**
     * Custom scalar type mappings from GraphQL scalar names to Java class names.
     *
     * @return the scalar mapping property
     */
    @Input
    @Optional
    public abstract MapProperty<String, String> getScalarMapping();

    /**
     * Executes the code generation.
     *
     * @throws IOException if reading the schema or writing files fails
     */
    @TaskAction
    public void generate() throws IOException {
        Path schemaPath = getSchemaPath().get().getAsFile().toPath();
        Path outputDir = getOutputDirectory().get().getAsFile().toPath();
        String packageName = getPackageName().get();

        CodeGeneratorConfig.Builder configBuilder = CodeGeneratorConfig.builder()
                .schemaPath(schemaPath)
                .outputDirectory(outputDir)
                .packageName(packageName);

        if (getGenerateBuilders().isPresent()) {
            configBuilder.generateBuilders(getGenerateBuilders().get());
        }

        if (getScalarMapping().isPresent()) {
            Map<String, String> mappings = getScalarMapping().get();
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                configBuilder.scalarMapping(entry.getKey(), entry.getValue());
            }
        }

        CodeGeneratorConfig config = configBuilder.build();
        GraphiteCodeGenerator generator = new GraphiteCodeGenerator(config);

        GraphiteCodeGenerator.GenerationResult result = generator.generate();

        getLogger().lifecycle("Graphite: Generated {} files to {}",
                result.fileCount(), result.outputDirectory());
    }
}
