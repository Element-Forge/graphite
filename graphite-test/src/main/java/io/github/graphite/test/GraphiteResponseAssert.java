package io.github.graphite.test;

import io.github.graphite.GraphiteError;
import io.github.graphite.GraphiteResponse;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.ListAssert;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * AssertJ assertions for {@link GraphiteResponse}.
 *
 * @param <D> the data type of the response
 */
public class GraphiteResponseAssert<D> extends AbstractAssert<GraphiteResponseAssert<D>, GraphiteResponse<D>> {

    /**
     * Creates a new assertion for a GraphiteResponse.
     *
     * @param actual the response to assert on
     */
    public GraphiteResponseAssert(GraphiteResponse<D> actual) {
        super(actual, GraphiteResponseAssert.class);
    }

    /**
     * Verifies that the response has no errors.
     *
     * @return this assertion for chaining
     */
    public GraphiteResponseAssert<D> hasNoErrors() {
        isNotNull();
        if (actual.hasErrors()) {
            failWithMessage("Expected response to have no errors but had: %s", actual.errors());
        }
        return this;
    }

    /**
     * Verifies that the response has errors.
     *
     * @return this assertion for chaining
     */
    public GraphiteResponseAssert<D> hasErrors() {
        isNotNull();
        if (!actual.hasErrors()) {
            failWithMessage("Expected response to have errors but had none");
        }
        return this;
    }

    /**
     * Verifies that the response has the expected number of errors.
     *
     * @param count the expected error count
     * @return this assertion for chaining
     */
    public GraphiteResponseAssert<D> hasErrorCount(int count) {
        isNotNull();
        int actualCount = actual.errors().size();
        if (actualCount != count) {
            failWithMessage("Expected response to have %d errors but had %d", count, actualCount);
        }
        return this;
    }

    /**
     * Verifies that the response has data.
     *
     * @return this assertion for chaining
     */
    public GraphiteResponseAssert<D> hasData() {
        isNotNull();
        if (!actual.hasData()) {
            failWithMessage("Expected response to have data but was null");
        }
        return this;
    }

    /**
     * Verifies that the response has no data.
     *
     * @return this assertion for chaining
     */
    public GraphiteResponseAssert<D> hasNoData() {
        isNotNull();
        if (actual.hasData()) {
            failWithMessage("Expected response to have no data but had: %s", actual.data());
        }
        return this;
    }

    /**
     * Verifies that the response data equals the expected value.
     *
     * @param expected the expected data
     * @return this assertion for chaining
     */
    public GraphiteResponseAssert<D> hasDataEqualTo(D expected) {
        isNotNull();
        hasData();
        if (!Objects.equals(actual.data(), expected)) {
            failWithMessage("Expected response data to be <%s> but was <%s>", expected, actual.data());
        }
        return this;
    }

    /**
     * Verifies that the response data satisfies the given requirements.
     *
     * @param requirements the requirements for the data
     * @return this assertion for chaining
     */
    public GraphiteResponseAssert<D> satisfiesData(Consumer<D> requirements) {
        isNotNull();
        hasData();
        requirements.accept(actual.data());
        return this;
    }

    /**
     * Returns an assertion on the errors list.
     *
     * @return a list assertion for the errors
     */
    public ListAssert<GraphiteError> errors() {
        isNotNull();
        return new ListAssert<>(actual.errors());
    }

    /**
     * Verifies that the response contains an error with the given message.
     *
     * @param message the expected error message
     * @return this assertion for chaining
     */
    public GraphiteResponseAssert<D> hasErrorWithMessage(String message) {
        isNotNull();
        Objects.requireNonNull(message, "message must not be null");
        boolean found = actual.errors().stream()
                .anyMatch(e -> message.equals(e.message()));
        if (!found) {
            failWithMessage("Expected response to have error with message <%s> but errors were: %s",
                    message, actual.errors());
        }
        return this;
    }

    /**
     * Verifies that the response contains an error with a message containing the given text.
     *
     * @param text the text to search for
     * @return this assertion for chaining
     */
    public GraphiteResponseAssert<D> hasErrorMessageContaining(String text) {
        isNotNull();
        Objects.requireNonNull(text, "text must not be null");
        boolean found = actual.errors().stream()
                .anyMatch(e -> e.message().contains(text));
        if (!found) {
            failWithMessage("Expected response to have error containing <%s> but errors were: %s",
                    text, actual.errors());
        }
        return this;
    }

    /**
     * Verifies that the response contains an error with the given code.
     *
     * @param code the expected error code
     * @return this assertion for chaining
     */
    public GraphiteResponseAssert<D> hasErrorWithCode(String code) {
        isNotNull();
        Objects.requireNonNull(code, "code must not be null");
        boolean found = actual.errors().stream()
                .anyMatch(e -> e.extensions() != null && code.equals(e.extensions().get("code")));
        if (!found) {
            failWithMessage("Expected response to have error with code <%s> but errors were: %s",
                    code, actual.errors());
        }
        return this;
    }

    /**
     * Verifies that the response has extensions.
     *
     * @return this assertion for chaining
     */
    public GraphiteResponseAssert<D> hasExtensions() {
        isNotNull();
        if (actual.extensions().isEmpty()) {
            failWithMessage("Expected response to have extensions but had none");
        }
        return this;
    }

    /**
     * Verifies that the response has the specified extension.
     *
     * @param key the extension key
     * @return this assertion for chaining
     */
    public GraphiteResponseAssert<D> hasExtension(String key) {
        isNotNull();
        Objects.requireNonNull(key, "key must not be null");
        if (!actual.extensions().containsKey(key)) {
            failWithMessage("Expected response to have extension <%s> but extensions were: %s",
                    key, actual.extensions());
        }
        return this;
    }

    /**
     * Verifies that the response has the specified extension with the given value.
     *
     * @param key the extension key
     * @param value the expected value
     * @return this assertion for chaining
     */
    public GraphiteResponseAssert<D> hasExtension(String key, Object value) {
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
     * Verifies that the response is successful (has data and no errors).
     *
     * @return this assertion for chaining
     */
    public GraphiteResponseAssert<D> isSuccessful() {
        return hasNoErrors().hasData();
    }

    /**
     * Verifies that the first error satisfies the given requirements.
     *
     * @param requirements the requirements for the first error
     * @return this assertion for chaining
     */
    public GraphiteResponseAssert<D> firstErrorSatisfies(Consumer<GraphiteError> requirements) {
        isNotNull();
        hasErrors();
        requirements.accept(actual.errors().get(0));
        return this;
    }
}
