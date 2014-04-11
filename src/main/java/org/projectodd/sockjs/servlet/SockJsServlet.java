/**
 * Copyright (C) 2014 Red Hat, Inc, and individual contributors.
 */

package org.projectodd.sockjs.servlet;

import org.projectodd.sockjs.Server;
import org.projectodd.sockjs.SockJsException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;
import java.io.IOException;

public class SockJsServlet extends HttpServlet {

    public SockJsServlet() {

    }

    public SockJsServlet(Server server) {
        this.server = server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Server getServer() {
        return server;
    }

    @Override
    public void init() throws ServletException {
        if (server == null) {
            server = new Server();
        }
        server.init();

        if (server.options.websocket) {
            String websocketPath = server.options.prefix + "/{server}/{session}/websocket";
            ServerEndpointConfig config = ServerEndpointConfig.Builder
                    .create(SockJsEndpoint.class, websocketPath)
                    .configurator(new ServerEndpointConfig.Configurator() {
                        @Override
                        public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
                            try {
                                return endpointClass.getConstructor(Server.class).newInstance(server);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    })
                    .build();
            ServerContainer serverContainer = (ServerContainer) getServletContext().getAttribute("javax.websocket.server.ServerContainer");
            try {
                serverContainer.addEndpoint(config);
            } catch (DeploymentException ex) {
                throw new ServletException("Error deploying websocket endpoint:", ex);
            }
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        AsyncContext asyncContext = req.startAsync();
        SockJsServletRequest sockJsReq = new SockJsServletRequest(req);
        SockJsServletResponse sockJsRes = new SockJsServletResponse(res, asyncContext);
        try {
            server.dispatch(sockJsReq, sockJsRes);
        } catch (SockJsException ex) {
            throw new ServletException("Error during SockJS request:", ex);
        }
        if ("application/x-www-form-urlencoded".equals(req.getHeader("Content-Type"))) {
            // Let the servlet parse data and just pretend like we did
            sockJsReq.onAllDataRead();
        } else {
            req.getInputStream().setReadListener(sockJsReq);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        server.destroy();
    }

    private Server server;
}
