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
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        AsyncContext asyncContext = req.startAsync();
        SockJsServletRequest sockJsReq = new SockJsServletRequest(req);
        SockJsServletResponse sockJsRes = new SockJsServletResponse(res, asyncContext);
        req.getInputStream().setReadListener(sockJsReq);
        try {
            server.dispatch(sockJsReq, sockJsRes);
        } catch (SockJsException ex) {
            throw new ServletException("Error during SockJS request:", ex);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        server.destroy();
    }

    private Server server;
}
