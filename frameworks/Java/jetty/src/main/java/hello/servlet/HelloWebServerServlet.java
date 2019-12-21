
package hello.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.AbstractHandlerContainer;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jetty9.InstrumentedConnectionFactory;
import com.codahale.metrics.jetty9.InstrumentedHandler;
import com.codahale.metrics.jetty9.InstrumentedQueuedThreadPool;


/**
 * An implementation of the TechEmpower benchmark tests using the Jetty web
 * server.
 */
public final class HelloWebServerServlet
{
    private static final MetricRegistry REGISTRY = new MetricRegistry();

    public static void main(String[] args) throws Exception
    {
        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog");
        System.setProperty("org.eclipse.jetty.LEVEL", "OFF");

        final InstrumentedQueuedThreadPool threadPool = new InstrumentedQueuedThreadPool(REGISTRY);
        final Server server = new Server(threadPool);

        ServerConnector connector = new ServerConnector(
            server,
            new InstrumentedConnectionFactory(
                new HttpConnectionFactory(),
                REGISTRY.timer("http.connection")
            )
        );

        connector.setPort(8080);
        server.addConnector(connector);

        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SECURITY|ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");

        context.addServlet(org.eclipse.jetty.servlet.DefaultServlet.class, "/");
        context.addServlet(JsonServlet.class, "/json");
        context.addServlet(PlaintextServlet.class, "/plaintext");

        final InstrumentedHandler handler = new InstrumentedHandler(REGISTRY);
        handler.setHandler(context);
        server.setHandler(handler);

        final JmxReporter reporter = JmxReporter.forRegistry(REGISTRY).build();

        reporter.start();
        server.start();
        server.join();
    }
}
