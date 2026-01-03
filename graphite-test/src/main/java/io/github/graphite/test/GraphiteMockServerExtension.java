package io.github.graphite.test;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * JUnit 5 extension for managing {@link GraphiteMockServer} lifecycle.
 *
 * <p>This extension automatically starts a mock server before each test
 * and stops it after each test. It also supports parameter injection.</p>
 *
 * <p>Example usage with {@code @ExtendWith}:</p>
 * <pre>{@code
 * @ExtendWith(GraphiteMockServerExtension.class)
 * class MyTest {
 *
 *     @Test
 *     void testWithServer(GraphiteMockServer server) {
 *         server.stubResponse("{ \"data\": { \"hello\": \"world\" } }");
 *         // ... test code
 *     }
 * }
 * }</pre>
 *
 * <p>Example usage with {@code @RegisterExtension}:</p>
 * <pre>{@code
 * class MyTest {
 *
 *     @RegisterExtension
 *     static GraphiteMockServerExtension serverExtension = new GraphiteMockServerExtension();
 *
 *     @Test
 *     void testWithServer() {
 *         GraphiteMockServer server = serverExtension.getServer();
 *         server.stubResponse("{ \"data\": { \"hello\": \"world\" } }");
 *         // ... test code
 *     }
 * }
 * }</pre>
 */
public class GraphiteMockServerExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(GraphiteMockServerExtension.class);
    private static final String SERVER_KEY = "server";

    private final int port;
    private GraphiteMockServer currentServer;

    /**
     * Creates a new extension that uses a random available port.
     */
    public GraphiteMockServerExtension() {
        this(0);
    }

    /**
     * Creates a new extension that uses the specified port.
     *
     * @param port the port to use, or 0 for a random port
     */
    public GraphiteMockServerExtension(int port) {
        this.port = port;
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        GraphiteMockServer server = port == 0
                ? GraphiteMockServer.create()
                : GraphiteMockServer.create(port);
        server.start();
        currentServer = server;
        getStore(context).put(SERVER_KEY, server);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        GraphiteMockServer server = getStore(context).remove(SERVER_KEY, GraphiteMockServer.class);
        if (server != null) {
            server.stop();
        }
        currentServer = null;
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == GraphiteMockServer.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return getStore(extensionContext).get(SERVER_KEY, GraphiteMockServer.class);
    }

    /**
     * Returns the current mock server instance.
     *
     * <p>This method is useful when using {@code @RegisterExtension}.</p>
     *
     * @return the current server, or null if not in a test
     */
    public GraphiteMockServer getServer() {
        return currentServer;
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(NAMESPACE);
    }
}
