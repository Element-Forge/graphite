package io.github.graphite.test;

import io.github.graphite.GraphiteError;
import org.assertj.core.api.AbstractAssert;

import java.util.List;
import java.util.Objects;

/**
 * AssertJ assertions for {@link GraphiteError}.
 */
public class GraphiteErrorAssert extends AbstractAssert<GraphiteErrorAssert, GraphiteError> {

    /**
     * Creates a new assertion for a GraphiteError.
     *
     * @param actual the error to assert on
     */
    public GraphiteErrorAssert(GraphiteError actual) {
        super(actual, GraphiteErrorAssert.class);
    }

    /**
     * Verifies that the error has the expected message.
     *
     * @param message the expected message
     * @return this assertion for chaining
     */
    public GraphiteErrorAssert hasMessage(String message) {
        isNotNull();
        Objects.requireNonNull(message, "message must not be null");
        if (!message.equals(actual.message())) {
            failWithMessage("Expected error message to be <%s> but was <%s>", message, actual.message());
        }
        return this;
    }

    /**
     * Verifies that the error message contains the given text.
     *
     * @param text the text to search for
     * @return this assertion for chaining
     */
    public GraphiteErrorAssert hasMessageContaining(String text) {
        isNotNull();
        Objects.requireNonNull(text, "text must not be null");
        if (!actual.message().contains(text)) {
            failWithMessage("Expected error message to contain <%s> but was <%s>", text, actual.message());
        }
        return this;
    }

    /**
     * Verifies that the error has locations.
     *
     * @return this assertion for chaining
     */
    public GraphiteErrorAssert hasLocations() {
        isNotNull();
        if (actual.locations() == null || actual.locations().isEmpty()) {
            failWithMessage("Expected error to have locations but had none");
        }
        return this;
    }

    /**
     * Verifies that the error has no locations.
     *
     * @return this assertion for chaining
     */
    public GraphiteErrorAssert hasNoLocations() {
        isNotNull();
        if (actual.locations() != null && !actual.locations().isEmpty()) {
            failWithMessage("Expected error to have no locations but had: %s", actual.locations());
        }
        return this;
    }

    /**
     * Verifies that the error has a location at the specified line and column.
     *
     * @param line the expected line number
     * @param column the expected column number
     * @return this assertion for chaining
     */
    public GraphiteErrorAssert hasLocationAt(int line, int column) {
        isNotNull();
        hasLocations();
        boolean found = actual.locations().stream()
                .anyMatch(loc -> loc.line() == line && loc.column() == column);
        if (!found) {
            failWithMessage("Expected error to have location at line %d, column %d but locations were: %s",
                    line, column, actual.locations());
        }
        return this;
    }

    /**
     * Verifies that the error has a path.
     *
     * @return this assertion for chaining
     */
    public GraphiteErrorAssert hasPath() {
        isNotNull();
        if (actual.path() == null || actual.path().isEmpty()) {
            failWithMessage("Expected error to have path but had none");
        }
        return this;
    }

    /**
     * Verifies that the error has no path.
     *
     * @return this assertion for chaining
     */
    public GraphiteErrorAssert hasNoPath() {
        isNotNull();
        if (actual.path() != null && !actual.path().isEmpty()) {
            failWithMessage("Expected error to have no path but had: %s", actual.path());
        }
        return this;
    }

    /**
     * Verifies that the error has the expected path.
     *
     * @param expectedPath the expected path elements
     * @return this assertion for chaining
     */
    public GraphiteErrorAssert hasPath(Object... expectedPath) {
        isNotNull();
        hasPath();
        List<Object> expected = List.of(expectedPath);
        if (!expected.equals(actual.path())) {
            failWithMessage("Expected error path to be %s but was %s", expected, actual.path());
        }
        return this;
    }

    /**
     * Verifies that the error path contains the given element.
     *
     * @param element the path element to search for
     * @return this assertion for chaining
     */
    public GraphiteErrorAssert hasPathContaining(Object element) {
        isNotNull();
        hasPath();
        if (!actual.path().contains(element)) {
            failWithMessage("Expected error path to contain <%s> but was %s", element, actual.path());
        }
        return this;
    }

    /**
     * Verifies that the error has extensions.
     *
     * @return this assertion for chaining
     */
    public GraphiteErrorAssert hasExtensions() {
        isNotNull();
        if (actual.extensions() == null || actual.extensions().isEmpty()) {
            failWithMessage("Expected error to have extensions but had none");
        }
        return this;
    }

    /**
     * Verifies that the error has no extensions.
     *
     * @return this assertion for chaining
     */
    public GraphiteErrorAssert hasNoExtensions() {
        isNotNull();
        if (actual.extensions() != null && !actual.extensions().isEmpty()) {
            failWithMessage("Expected error to have no extensions but had: %s", actual.extensions());
        }
        return this;
    }

    /**
     * Verifies that the error has the specified extension.
     *
     * @param key the extension key
     * @return this assertion for chaining
     */
    public GraphiteErrorAssert hasExtension(String key) {
        isNotNull();
        Objects.requireNonNull(key, "key must not be null");
        hasExtensions();
        if (!actual.extensions().containsKey(key)) {
            failWithMessage("Expected error to have extension <%s> but extensions were: %s",
                    key, actual.extensions());
        }
        return this;
    }

    /**
     * Verifies that the error has the specified extension with the given value.
     *
     * @param key the extension key
     * @param value the expected value
     * @return this assertion for chaining
     */
    public GraphiteErrorAssert hasExtension(String key, Object value) {
        isNotNull();
        Objects.requireNonNull(key, "key must not be null");
        hasExtension(key);
        Object actualValue = actual.extensions().get(key);
        if (!Objects.equals(actualValue, value)) {
            failWithMessage("Expected extension <%s> to be <%s> but was <%s>", key, value, actualValue);
        }
        return this;
    }

    /**
     * Verifies that the error has the specified code.
     *
     * @param code the expected error code
     * @return this assertion for chaining
     */
    public GraphiteErrorAssert hasCode(String code) {
        return hasExtension("code", code);
    }
}
