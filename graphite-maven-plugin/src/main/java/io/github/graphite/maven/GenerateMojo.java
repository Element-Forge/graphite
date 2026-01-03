package io.github.graphite.maven;

import io.github.graphite.codegen.CodeGeneratorConfig;
import io.github.graphite.codegen.GraphiteCodeGenerator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Maven plugin goal for Graphite GraphQL client code generation.
 *
 * <p>Generates type-safe GraphQL client code from a schema file.</p>
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
 *                 <goal>generate</goal>
 *             </goals>
 *         </execution>
 *     </executions>
 *     <configuration>
 *         <schemaPath>${project.basedir}/src/main/graphql/schema.graphqls</schemaPath>
 *         <packageName>com.example.graphql</packageName>
 *     </configuration>
 * </plugin>
 * }</pre>
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GenerateMojo extends AbstractMojo {

    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Path to the GraphQL schema file.
     */
    @Parameter(property = "graphite.schemaPath", required = true)
    private File schemaPath;

    /**
     * The base package name for generated classes.
     */
    @Parameter(property = "graphite.packageName", required = true)
    private String packageName;

    /**
     * The output directory for generated source files.
     * Defaults to {@code target/generated-sources/graphite}.
     */
    @Parameter(
            property = "graphite.outputDirectory",
            defaultValue = "${project.build.directory}/generated-sources/graphite"
    )
    private File outputDirectory;

    /**
     * Whether to generate builder classes for input types.
     * Defaults to {@code true}.
     */
    @Parameter(property = "graphite.generateBuilders", defaultValue = "true")
    private boolean generateBuilders;

    /**
     * Custom scalar type mappings from GraphQL scalar names to Java class names.
     */
    @Parameter
    private Map<String, String> scalarMapping;

    /**
     * Creates a new GenerateMojo.
     */
    public GenerateMojo() {
        // Default constructor for Maven
    }

    @Override
    public void execute() throws MojoExecutionException {
        validateConfiguration();

        Path schemaFilePath = schemaPath.toPath();
        Path outputDirPath = outputDirectory.toPath();

        getLog().info("Graphite: Generating client code from " + schemaFilePath);

        CodeGeneratorConfig.Builder configBuilder = CodeGeneratorConfig.builder()
                .schemaPath(schemaFilePath)
                .outputDirectory(outputDirPath)
                .packageName(packageName)
                .generateBuilders(generateBuilders);

        if (scalarMapping != null && !scalarMapping.isEmpty()) {
            for (Map.Entry<String, String> entry : scalarMapping.entrySet()) {
                configBuilder.scalarMapping(entry.getKey(), entry.getValue());
            }
        }

        try {
            CodeGeneratorConfig config = configBuilder.build();
            GraphiteCodeGenerator generator = new GraphiteCodeGenerator(config);
            GraphiteCodeGenerator.GenerationResult result = generator.generate();

            getLog().info("Graphite: Generated " + result.fileCount() + " files to " + outputDirPath);

            // Add generated sources to compile path
            project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
            getLog().debug("Added " + outputDirectory + " to compile source roots");

        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate GraphQL client code", e);
        }
    }

    private void validateConfiguration() throws MojoExecutionException {
        if (schemaPath == null) {
            throw new MojoExecutionException("schemaPath is required");
        }
        if (!schemaPath.exists()) {
            throw new MojoExecutionException("Schema file does not exist: " + schemaPath);
        }
        if (!schemaPath.isFile()) {
            throw new MojoExecutionException("schemaPath must be a file: " + schemaPath);
        }
        if (packageName == null || packageName.isBlank()) {
            throw new MojoExecutionException("packageName is required");
        }
    }
}
