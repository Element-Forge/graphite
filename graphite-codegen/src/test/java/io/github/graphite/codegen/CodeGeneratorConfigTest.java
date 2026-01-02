package io.github.graphite.codegen;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CodeGeneratorConfigTest {

    @Test
    void builderCreatesConfig() {
        CodeGeneratorConfig config = CodeGeneratorConfig.builder()
                .packageName("com.example.graphql")
                .outputDirectory(Path.of("build/generated"))
                .schemaPath(Path.of("schema.graphqls"))
                .build();

        assertEquals("com.example.graphql", config.packageName());
        assertEquals(Path.of("build/generated"), config.outputDirectory());
        assertEquals(Path.of("schema.graphqls"), config.schemaPath());
    }

    @Test
    void hasDefaultScalarMappings() {
        CodeGeneratorConfig config = CodeGeneratorConfig.builder()
                .packageName("com.example")
                .outputDirectory(Path.of("out"))
                .schemaPath(Path.of("schema.graphqls"))
                .build();

        Map<String, String> mappings = config.scalarMappings();
        assertEquals("java.time.OffsetDateTime", mappings.get("DateTime"));
        assertEquals("java.time.LocalDate", mappings.get("Date"));
        assertEquals("java.time.LocalTime", mappings.get("Time"));
        assertEquals("java.util.UUID", mappings.get("UUID"));
        assertEquals("java.math.BigDecimal", mappings.get("BigDecimal"));
        assertEquals("java.lang.Long", mappings.get("Long"));
        assertEquals("java.net.URI", mappings.get("URL"));
    }

    @Test
    void customScalarMappingOverridesDefault() {
        CodeGeneratorConfig config = CodeGeneratorConfig.builder()
                .packageName("com.example")
                .outputDirectory(Path.of("out"))
                .schemaPath(Path.of("schema.graphqls"))
                .scalarMapping("DateTime", "java.time.Instant")
                .build();

        assertEquals("java.time.Instant", config.scalarMappings().get("DateTime"));
    }

    @Test
    void customScalarMappingAddsNew() {
        CodeGeneratorConfig config = CodeGeneratorConfig.builder()
                .packageName("com.example")
                .outputDirectory(Path.of("out"))
                .schemaPath(Path.of("schema.graphqls"))
                .scalarMapping("JSON", "com.fasterxml.jackson.databind.JsonNode")
                .build();

        assertEquals("com.fasterxml.jackson.databind.JsonNode", config.scalarMappings().get("JSON"));
    }

    @Test
    void scalarMappingsReplaceAll() {
        CodeGeneratorConfig config = CodeGeneratorConfig.builder()
                .packageName("com.example")
                .outputDirectory(Path.of("out"))
                .schemaPath(Path.of("schema.graphqls"))
                .scalarMappings(Map.of("Custom", "java.lang.String"))
                .build();

        assertEquals(1, config.scalarMappings().size());
        assertEquals("java.lang.String", config.scalarMappings().get("Custom"));
        assertNull(config.scalarMappings().get("DateTime"));
    }

    @Test
    void scalarMappingsAreUnmodifiable() {
        CodeGeneratorConfig config = CodeGeneratorConfig.builder()
                .packageName("com.example")
                .outputDirectory(Path.of("out"))
                .schemaPath(Path.of("schema.graphqls"))
                .build();

        assertThrows(UnsupportedOperationException.class, () ->
                config.scalarMappings().put("New", "java.lang.String"));
    }

    @Test
    void hasDefaultTypeSuffix() {
        CodeGeneratorConfig config = CodeGeneratorConfig.builder()
                .packageName("com.example")
                .outputDirectory(Path.of("out"))
                .schemaPath(Path.of("schema.graphqls"))
                .build();

        assertEquals("", config.typeSuffix());
    }

    @Test
    void customTypeSuffix() {
        CodeGeneratorConfig config = CodeGeneratorConfig.builder()
                .packageName("com.example")
                .outputDirectory(Path.of("out"))
                .schemaPath(Path.of("schema.graphqls"))
                .typeSuffix("Type")
                .build();

        assertEquals("Type", config.typeSuffix());
    }

    @Test
    void nullTypeSuffixBecomesEmpty() {
        CodeGeneratorConfig config = CodeGeneratorConfig.builder()
                .packageName("com.example")
                .outputDirectory(Path.of("out"))
                .schemaPath(Path.of("schema.graphqls"))
                .typeSuffix(null)
                .build();

        assertEquals("", config.typeSuffix());
    }

    @Test
    void hasDefaultInputSuffix() {
        CodeGeneratorConfig config = CodeGeneratorConfig.builder()
                .packageName("com.example")
                .outputDirectory(Path.of("out"))
                .schemaPath(Path.of("schema.graphqls"))
                .build();

        assertEquals("", config.inputSuffix());
    }

    @Test
    void customInputSuffix() {
        CodeGeneratorConfig config = CodeGeneratorConfig.builder()
                .packageName("com.example")
                .outputDirectory(Path.of("out"))
                .schemaPath(Path.of("schema.graphqls"))
                .inputSuffix("Input")
                .build();

        assertEquals("Input", config.inputSuffix());
    }

    @Test
    void nullInputSuffixBecomesEmpty() {
        CodeGeneratorConfig config = CodeGeneratorConfig.builder()
                .packageName("com.example")
                .outputDirectory(Path.of("out"))
                .schemaPath(Path.of("schema.graphqls"))
                .inputSuffix(null)
                .build();

        assertEquals("", config.inputSuffix());
    }

    @Test
    void generateBuildersDefaultsToTrue() {
        CodeGeneratorConfig config = CodeGeneratorConfig.builder()
                .packageName("com.example")
                .outputDirectory(Path.of("out"))
                .schemaPath(Path.of("schema.graphqls"))
                .build();

        assertTrue(config.generateBuilders());
    }

    @Test
    void generateBuildersCanBeDisabled() {
        CodeGeneratorConfig config = CodeGeneratorConfig.builder()
                .packageName("com.example")
                .outputDirectory(Path.of("out"))
                .schemaPath(Path.of("schema.graphqls"))
                .generateBuilders(false)
                .build();

        assertFalse(config.generateBuilders());
    }

    @Test
    void builderRequiresPackageName() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                CodeGeneratorConfig.builder()
                        .outputDirectory(Path.of("out"))
                        .schemaPath(Path.of("schema.graphqls"))
                        .build());

        assertEquals("packageName is required", ex.getMessage());
    }

    @Test
    void builderRequiresOutputDirectory() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                CodeGeneratorConfig.builder()
                        .packageName("com.example")
                        .schemaPath(Path.of("schema.graphqls"))
                        .build());

        assertEquals("outputDirectory is required", ex.getMessage());
    }

    @Test
    void builderRequiresSchemaPath() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                CodeGeneratorConfig.builder()
                        .packageName("com.example")
                        .outputDirectory(Path.of("out"))
                        .build());

        assertEquals("schemaPath is required", ex.getMessage());
    }

    @Test
    void builderRejectsNullPackageName() {
        assertThrows(NullPointerException.class, () ->
                CodeGeneratorConfig.builder().packageName(null));
    }

    @Test
    void builderRejectsNullOutputDirectory() {
        assertThrows(NullPointerException.class, () ->
                CodeGeneratorConfig.builder().outputDirectory(null));
    }

    @Test
    void builderRejectsNullSchemaPath() {
        assertThrows(NullPointerException.class, () ->
                CodeGeneratorConfig.builder().schemaPath(null));
    }

    @Test
    void builderRejectsNullScalarMappingKey() {
        assertThrows(NullPointerException.class, () ->
                CodeGeneratorConfig.builder().scalarMapping(null, "java.lang.String"));
    }

    @Test
    void builderRejectsNullScalarMappingValue() {
        assertThrows(NullPointerException.class, () ->
                CodeGeneratorConfig.builder().scalarMapping("Custom", null));
    }

    @Test
    void builderRejectsNullScalarMappings() {
        assertThrows(NullPointerException.class, () ->
                CodeGeneratorConfig.builder().scalarMappings(null));
    }
}
