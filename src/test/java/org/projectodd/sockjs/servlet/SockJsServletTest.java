/**
 * Copyright (C) 2014 Red Hat, Inc, and individual contributors.
 */

package org.projectodd.sockjs.servlet;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import org.projectodd.sockjs.Server;
import org.projectodd.sockjs.SockJsConnection;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import javax.servlet.Servlet;
import java.util.concurrent.CountDownLatch;

public class SockJsServletTest {

    public static void main(String[] args) throws Exception {
        Server echoServer = new Server();
        echoServer.options.responseLimit = 4096;
        echoServer.onConnection(new Server.OnConnectionHandler() {
            @Override
            public void handle(final SockJsConnection connection) {
                System.out.println("    [+] echo open    " + connection);
                connection.onClose(new SockJsConnection.OnCloseHandler() {
                    @Override
                    public void handle() {
                        System.out.println("    [-] echo close    " + connection);
                    }
                });
                connection.onData(new SockJsConnection.OnDataHandler() {
                    @Override
                    public void handle(String message) {
                        System.out.println("    [ ] echo message " + connection + " " + message);
                        connection.write(message);
                    }
                });
            }
        });

        Server echoNoWsServer = new Server();
        echoNoWsServer.options.responseLimit = 4096;
        echoNoWsServer.options.websocket = false;
        echoNoWsServer.onConnection(new Server.OnConnectionHandler() {
            @Override
            public void handle(final SockJsConnection connection) {
                System.out.println("    [+] echo open    " + connection);
                connection.onClose(new SockJsConnection.OnCloseHandler() {
                    @Override
                    public void handle() {
                        System.out.println("    [-] echo close    " + connection);
                    }
                });
                connection.onData(new SockJsConnection.OnDataHandler() {
                    @Override
                    public void handle(String message) {
                        System.out.println("    [ ] echo message " + connection + " " + message);
                        connection.write(message);
                    }
                });
            }
        });

        Server echoCookie = new Server();
        echoCookie.options.responseLimit = 4096;
        echoCookie.options.jsessionid = true;
        echoCookie.onConnection(new Server.OnConnectionHandler() {
            @Override
            public void handle(final SockJsConnection connection) {
                System.out.println("    [+] echo open    " + connection);
                connection.onClose(new SockJsConnection.OnCloseHandler() {
                    @Override
                    public void handle() {
                        System.out.println("    [-] echo close    " + connection);
                    }
                });
                connection.onData(new SockJsConnection.OnDataHandler() {
                    @Override
                    public void handle(String message) {
                        System.out.println("    [ ] echo message " + connection + " " + message);
                        connection.write(message);
                    }
                });
            }
        });

        Server closeServer = new Server();
        closeServer.onConnection(new Server.OnConnectionHandler() {
            @Override
            public void handle(final SockJsConnection connection) {
                System.out.println("    [+] clos open    " + connection);
                connection.close(3000, "Go away!");
                connection.onClose(new SockJsConnection.OnCloseHandler() {
                    @Override
                    public void handle() {
                        System.out.println("    [-] clos close    " + connection);
                    }
                });
            }
        });

        PathHandler pathHandler = new PathHandler();
        installHandler(pathHandler, echoServer, "/echo");
        installHandler(pathHandler, echoNoWsServer, "/disabled_websocket_echo");
        installHandler(pathHandler, echoCookie, "/cookie_needed_echo");
        installHandler(pathHandler, closeServer, "/close");

        runServer(pathHandler, "localhost", 8081);
    }

    private static void installHandler(PathHandler pathHandler, Server server, String context) throws Exception {
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
                .setClassLoader(SockJsServletTest.class.getClassLoader())
                .setContextPath(context)
                .setDeploymentName(context)
                // Because Undertow tries to be too smart and ignore our flushes
                .setIgnoreFlush(false)
                .addServlet(servletInfo);
        // Required for any websocket support in undertow
        final WebSocketDeploymentInfo wsInfo = new WebSocketDeploymentInfo();
        servletBuilder.addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, wsInfo);
        final DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
        manager.deploy();
        final HttpHandler servletHandler = manager.start();

        pathHandler.addPrefixPath(context, servletHandler);
    }

    private static void runServer(HttpHandler handler, String host, int port) throws Exception {
        Undertow undertow = Undertow.builder()
                .addHttpListener(port, host)
                .setHandler(handler)
                .build();

        final CountDownLatch latch = new CountDownLatch(1);
        SignalHandler signalHandler = new SignalHandler() {
            @Override
            public void handle(Signal sig) {
                latch.countDown();
            }
        };
        undertow.start();
        System.out.println("SockJS Servlet running on http://" + host + ":" + port);
        System.out.println("CTRL+C to terminate");
        Signal.handle(new Signal("INT"), signalHandler);
        Signal.handle(new Signal("TERM"), signalHandler);
        latch.await();
    }
}
