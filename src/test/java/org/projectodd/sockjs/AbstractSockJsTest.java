package org.projectodd.sockjs;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import org.projectodd.sockjs.servlet.SockJsServlet;

import javax.servlet.Servlet;

public class AbstractSockJsTest {

    protected DeploymentManager createDeploymentManager(SockJsServer server, String context) throws Exception {
        Servlet servlet = new SockJsServlet(server);
        Class<? extends Servlet> servletClass = servlet.getClass();
        final ServletInfo servletInfo = Servlets.servlet(servletClass.getSimpleName(),
                servletClass,
                new ImmediateInstanceFactory<>(servlet));
        servletInfo.addMapping("/*");
        // LoadOnStartup is required for our websocket Endpoint to work
        servletInfo.setLoadOnStartup(0);
        // AsyncSupported is required
        servletInfo.setAsyncSupported(true);
        final DeploymentInfo servletBuilder = Servlets.deployment()
                .setClassLoader(AbstractSockJsTest.class.getClassLoader())
                .setContextPath(context)
                .setDeploymentName(context)
                        // Because Undertow tries to be too smart and ignore our flushes
                .setIgnoreFlush(false)
                .addServlet(servletInfo);
        // Required for any websocket support in undertow
        final WebSocketDeploymentInfo wsInfo = new WebSocketDeploymentInfo();
        servletBuilder.addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, wsInfo);
        return Servlets.defaultContainer().addDeployment(servletBuilder);
    }

    protected void installHandler(PathHandler pathHandler, SockJsServer server, String context) throws Exception {
        final DeploymentManager manager = createDeploymentManager(server, context);
        manager.deploy();
        final HttpHandler servletHandler = manager.start();
        pathHandler.addPrefixPath(context, servletHandler);
    }

    protected Undertow createUndertow(HttpHandler handler, String host, int port) throws Exception {
        return Undertow.builder()
                .addHttpListener(port, host)
                .setHandler(handler)
                .build();
    }
}
