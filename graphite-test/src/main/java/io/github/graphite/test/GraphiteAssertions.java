package io.github.graphite.test;

import io.github.graphite.GraphiteError;
import io.github.graphite.GraphiteResponse;
import org.assertj.core.api.Assertions;

/**
 * Entry point for Graphite-specific AssertJ assertions.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * import static io.github.graphite.test.GraphiteAssertions.assertThat;
 *
 * GraphiteResponse<User> response = client.execute(query);
 *
 * assertThat(response)
 *     .hasNoErrors()
 *     .hasData()
 *     .satisfiesData(user -> assertThat(user.getName()).isEqualTo("John"));
 * }</pre>
 */
public final class GraphiteAssertions extends Assertions {

    private GraphiteAssertions() {
    }

    /**
     * Creates an assertion for a GraphiteResponse.
     *
     * @param actual the response to assert on
     * @param <D> the data type
     * @return the assertion
     */
    public static <D> GraphiteResponseAssert<D> assertThat(GraphiteResponse<D> actual) {
        return new GraphiteResponseAssert<>(actual);
    }

    /**
     * Creates an assertion for a GraphiteError.
     *
     * @param actual the error to assert on
     * @return the assertion
     */
    public static GraphiteErrorAssert assertThat(GraphiteError actual) {
        return new GraphiteErrorAssert(actual);
    }
}
