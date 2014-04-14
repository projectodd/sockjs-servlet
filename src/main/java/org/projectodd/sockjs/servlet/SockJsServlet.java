/**
 * Copyright (C) 2014 Red Hat, Inc, and individual contributors.
 */

package org.projectodd.sockjs.servlet;

import org.projectodd.sockjs.SockJsException;
import org.projectodd.sockjs.SockJsServer;

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

    public SockJsServlet(SockJsServer sockJsServer) {
        this.sockJsServer = sockJsServer;
    }

    public void setServer(SockJsServer sockJsServer) {
        this.sockJsServer = sockJsServer;
    }

    public SockJsServer getServer() {
        return sockJsServer;
    }

    @Override
    public void init() throws ServletException {
        if (sockJsServer == null) {
            sockJsServer = new SockJsServer();
        }
        sockJsServer.init();

        if (sockJsServer.options.websocket) {
            // Make sure we listen on all possible mappings of the servlet
            for (String mapping : getServletContext().getServletRegistration(getServletName()).getMappings()) {
                String websocketPath = extractPrefixFromMapping(mapping) + sockJsServer.options.prefix +
                        "/{server}/{session}/websocket";
                ServerEndpointConfig config = ServerEndpointConfig.Builder
                        .create(SockJsEndpoint.class, websocketPath)
                        .configurator(new ServerEndpointConfig.Configurator() {
                            @Override
                            public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
                                try {
                                    return endpointClass.getConstructor(SockJsServer.class).newInstance(sockJsServer);
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
    }

    private String extractPrefixFromMapping(String mapping) {
        if (mapping.endsWith("*")) {
            mapping = mapping.substring(0, mapping.length() - 1);
        }
        if (mapping.endsWith("/")) {
            mapping = mapping.substring(0, mapping.length() - 1);
        }
        return mapping;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        System.err.println("!!! SockJsServlet#service for " + req.getMethod() + " " + req.getPathInfo());
        AsyncContext asyncContext = req.startAsync();
        SockJsServletRequest sockJsReq = new SockJsServletRequest(req);
        SockJsServletResponse sockJsRes = new SockJsServletResponse(res, asyncContext);
        try {
            sockJsServer.dispatch(sockJsReq, sockJsRes);
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
        sockJsServer.destroy();
    }

    private SockJsServer sockJsServer;
}
