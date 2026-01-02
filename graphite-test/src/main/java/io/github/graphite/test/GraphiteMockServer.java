package io.github.graphite.test;

/**
 * Mock server for testing Graphite GraphQL clients.
 */
public final class GraphiteMockServer {

    private GraphiteMockServer() {
        // Factory methods will be added later
    }

    /**
     * Creates a new mock server instance.
     *
     * @return a new mock server
     */
    public static GraphiteMockServer create() {
        return new GraphiteMockServer();
    }
}
