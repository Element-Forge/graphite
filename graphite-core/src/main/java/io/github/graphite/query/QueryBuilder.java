package io.github.graphite.query;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Fluent builder for constructing GraphQL query strings.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * String query = QueryBuilder.query("GetUser")
 *     .variable("id", "ID!")
 *     .field("user", args -> args.arg("id", "$id"),
 *         user -> user
 *             .field("id")
 *             .field("name")
 *             .field("email"))
 *     .build();
 *
 * // Produces:
 * // query GetUser($id: ID!) {
 * //   user(id: $id) {
 * //     id
 * //     name
 * //     email
 * //   }
 * // }
 * }</pre>
 */
public final class QueryBuilder {

    private final OperationType operationType;
    private final String operationName;
    private final Map<String, String> variables = new LinkedHashMap<>();
    private final List<FieldBuilder> fields = new ArrayList<>();

    private QueryBuilder(OperationType operationType, String operationName) {
        this.operationType = operationType;
        this.operationName = operationName;
    }

    /**
     * Creates a new query builder.
     *
     * @return a new query builder
     */
    public static QueryBuilder query() {
        return new QueryBuilder(OperationType.QUERY, null);
    }

    /**
     * Creates a new named query builder.
     *
     * @param operationName the operation name
     * @return a new query builder
     */
    public static QueryBuilder query(@NotNull String operationName) {
        Objects.requireNonNull(operationName, "operationName must not be null");
        return new QueryBuilder(OperationType.QUERY, operationName);
    }

    /**
     * Creates a new mutation builder.
     *
     * @return a new mutation builder
     */
    public static QueryBuilder mutation() {
        return new QueryBuilder(OperationType.MUTATION, null);
    }

    /**
     * Creates a new named mutation builder.
     *
     * @param operationName the operation name
     * @return a new mutation builder
     */
    public static QueryBuilder mutation(@NotNull String operationName) {
        Objects.requireNonNull(operationName, "operationName must not be null");
        return new QueryBuilder(OperationType.MUTATION, operationName);
    }

    /**
     * Creates a new subscription builder.
     *
     * @return a new subscription builder
     */
    public static QueryBuilder subscription() {
        return new QueryBuilder(OperationType.SUBSCRIPTION, null);
    }

    /**
     * Creates a new named subscription builder.
     *
     * @param operationName the operation name
     * @return a new subscription builder
     */
    public static QueryBuilder subscription(@NotNull String operationName) {
        Objects.requireNonNull(operationName, "operationName must not be null");
        return new QueryBuilder(OperationType.SUBSCRIPTION, operationName);
    }

    /**
     * Adds a variable definition.
     *
     * @param name the variable name (without $)
     * @param type the GraphQL type (e.g., "ID!", "String", "[Int!]!")
     * @return this builder
     */
    public QueryBuilder variable(@NotNull String name, @NotNull String type) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(type, "type must not be null");
        variables.put(name, type);
        return this;
    }

    /**
     * Adds a simple field without arguments or selections.
     *
     * @param name the field name
     * @return this builder
     */
    public QueryBuilder field(@NotNull String name) {
        Objects.requireNonNull(name, "name must not be null");
        fields.add(new FieldBuilder(name));
        return this;
    }

    /**
     * Adds a field with nested selections.
     *
     * @param name the field name
     * @param selections consumer to configure nested field selections
     * @return this builder
     */
    public QueryBuilder field(@NotNull String name, @NotNull java.util.function.Consumer<SelectionBuilder> selections) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(selections, "selections must not be null");
        FieldBuilder field = new FieldBuilder(name);
        SelectionBuilder selectionBuilder = new SelectionBuilder();
        selections.accept(selectionBuilder);
        field.selections = selectionBuilder.fields;
        fields.add(field);
        return this;
    }

    /**
     * Adds a field with arguments.
     *
     * @param name the field name
     * @param arguments consumer to configure arguments
     * @return this builder
     */
    public QueryBuilder fieldWithArgs(@NotNull String name, @NotNull java.util.function.Consumer<ArgumentBuilder> arguments) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(arguments, "arguments must not be null");
        FieldBuilder field = new FieldBuilder(name);
        ArgumentBuilder argBuilder = new ArgumentBuilder();
        arguments.accept(argBuilder);
        field.arguments = argBuilder.arguments;
        fields.add(field);
        return this;
    }

    /**
     * Adds a field with arguments and nested selections.
     *
     * @param name the field name
     * @param arguments consumer to configure arguments
     * @param selections consumer to configure nested field selections
     * @return this builder
     */
    public QueryBuilder field(@NotNull String name,
                              @NotNull java.util.function.Consumer<ArgumentBuilder> arguments,
                              @NotNull java.util.function.Consumer<SelectionBuilder> selections) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(arguments, "arguments must not be null");
        Objects.requireNonNull(selections, "selections must not be null");
        FieldBuilder field = new FieldBuilder(name);
        ArgumentBuilder argBuilder = new ArgumentBuilder();
        arguments.accept(argBuilder);
        field.arguments = argBuilder.arguments;
        SelectionBuilder selectionBuilder = new SelectionBuilder();
        selections.accept(selectionBuilder);
        field.selections = selectionBuilder.fields;
        fields.add(field);
        return this;
    }

    /**
     * Builds the GraphQL query string.
     *
     * @return the query string
     */
    @NotNull
    public String build() {
        StringBuilder sb = new StringBuilder();

        // Operation type and name
        sb.append(operationType.keyword);
        if (operationName != null) {
            sb.append(" ").append(operationName);
        }

        // Variables
        if (!variables.isEmpty()) {
            sb.append("(");
            boolean first = true;
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                if (!first) sb.append(", ");
                sb.append("$").append(entry.getKey()).append(": ").append(entry.getValue());
                first = false;
            }
            sb.append(")");
        }

        // Selection set
        sb.append(" {\n");
        for (FieldBuilder field : fields) {
            appendField(sb, field, 1);
        }
        sb.append("}");

        return sb.toString();
    }

    private void appendField(StringBuilder sb, FieldBuilder field, int indent) {
        sb.append("  ".repeat(indent));

        // Field name with optional alias
        if (field.alias != null) {
            sb.append(field.alias).append(": ");
        }
        sb.append(field.name);

        // Arguments
        if (!field.arguments.isEmpty()) {
            sb.append("(");
            boolean first = true;
            for (Map.Entry<String, Object> entry : field.arguments.entrySet()) {
                if (!first) sb.append(", ");
                sb.append(entry.getKey()).append(": ");
                appendValue(sb, entry.getValue());
                first = false;
            }
            sb.append(")");
        }

        // Nested selections
        if (!field.selections.isEmpty()) {
            sb.append(" {\n");
            for (FieldBuilder nested : field.selections) {
                appendField(sb, nested, indent + 1);
            }
            sb.append("  ".repeat(indent)).append("}");
        }

        sb.append("\n");
    }

    private void appendValue(StringBuilder sb, Object value) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String str) {
            if (str.startsWith("$")) {
                // Variable reference
                sb.append(str);
            } else {
                // String literal
                sb.append("\"").append(escapeString(str)).append("\"");
            }
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof Enum<?> e) {
            sb.append(e.name());
        } else if (value instanceof List<?> list) {
            sb.append("[");
            boolean first = true;
            for (Object item : list) {
                if (!first) sb.append(", ");
                appendValue(sb, item);
                first = false;
            }
            sb.append("]");
        } else if (value instanceof Map<?, ?> map) {
            sb.append("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(", ");
                sb.append(entry.getKey()).append(": ");
                appendValue(sb, entry.getValue());
                first = false;
            }
            sb.append("}");
        } else {
            sb.append(value.toString());
        }
    }

    private String escapeString(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * GraphQL operation types.
     */
    private enum OperationType {
        QUERY("query"),
        MUTATION("mutation"),
        SUBSCRIPTION("subscription");

        final String keyword;

        OperationType(String keyword) {
            this.keyword = keyword;
        }
    }

    /**
     * Internal field representation.
     */
    private static class FieldBuilder {
        final String name;
        String alias;
        Map<String, Object> arguments = new LinkedHashMap<>();
        List<FieldBuilder> selections = new ArrayList<>();

        FieldBuilder(String name) {
            this.name = name;
        }
    }

    /**
     * Builder for field selections.
     */
    public static final class SelectionBuilder {
        private final List<FieldBuilder> fields = new ArrayList<>();

        /**
         * Adds a simple field.
         *
         * @param name the field name
         * @return this builder
         */
        public SelectionBuilder field(@NotNull String name) {
            Objects.requireNonNull(name, "name must not be null");
            fields.add(new FieldBuilder(name));
            return this;
        }

        /**
         * Adds a field with an alias.
         *
         * @param alias the alias
         * @param name the field name
         * @return this builder
         */
        public SelectionBuilder field(@NotNull String alias, @NotNull String name) {
            Objects.requireNonNull(alias, "alias must not be null");
            Objects.requireNonNull(name, "name must not be null");
            FieldBuilder field = new FieldBuilder(name);
            field.alias = alias;
            fields.add(field);
            return this;
        }

        /**
         * Adds a field with nested selections.
         *
         * @param name the field name
         * @param selections consumer to configure nested selections
         * @return this builder
         */
        public SelectionBuilder field(@NotNull String name, @NotNull java.util.function.Consumer<SelectionBuilder> selections) {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(selections, "selections must not be null");
            FieldBuilder field = new FieldBuilder(name);
            SelectionBuilder nested = new SelectionBuilder();
            selections.accept(nested);
            field.selections = nested.fields;
            fields.add(field);
            return this;
        }

        /**
         * Adds a field with arguments.
         *
         * @param name the field name
         * @param arguments consumer to configure arguments
         * @return this builder
         */
        public SelectionBuilder fieldWithArgs(@NotNull String name, @NotNull java.util.function.Consumer<ArgumentBuilder> arguments) {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(arguments, "arguments must not be null");
            FieldBuilder field = new FieldBuilder(name);
            ArgumentBuilder argBuilder = new ArgumentBuilder();
            arguments.accept(argBuilder);
            field.arguments = argBuilder.arguments;
            fields.add(field);
            return this;
        }

        /**
         * Adds a field with arguments and nested selections.
         *
         * @param name the field name
         * @param arguments consumer to configure arguments
         * @param selections consumer to configure nested selections
         * @return this builder
         */
        public SelectionBuilder field(@NotNull String name,
                                      @NotNull java.util.function.Consumer<ArgumentBuilder> arguments,
                                      @NotNull java.util.function.Consumer<SelectionBuilder> selections) {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(arguments, "arguments must not be null");
            Objects.requireNonNull(selections, "selections must not be null");
            FieldBuilder field = new FieldBuilder(name);
            ArgumentBuilder argBuilder = new ArgumentBuilder();
            arguments.accept(argBuilder);
            field.arguments = argBuilder.arguments;
            SelectionBuilder nested = new SelectionBuilder();
            selections.accept(nested);
            field.selections = nested.fields;
            fields.add(field);
            return this;
        }
    }

    /**
     * Builder for field arguments.
     */
    public static final class ArgumentBuilder {
        private final Map<String, Object> arguments = new LinkedHashMap<>();

        /**
         * Adds an argument with a value.
         *
         * @param name the argument name
         * @param value the argument value (String, Number, Boolean, Enum, List, Map, or variable reference starting with $)
         * @return this builder
         */
        public ArgumentBuilder arg(@NotNull String name, Object value) {
            Objects.requireNonNull(name, "name must not be null");
            arguments.put(name, value);
            return this;
        }
    }
}
