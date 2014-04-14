/**
 * Copyright (C) 2014 Red Hat, Inc, and individual contributors.
 * Copyright (C) 2011-2012 VMware, Inc.
 */

package org.projectodd.sockjs.servlet;

import org.projectodd.sockjs.Server;
import org.projectodd.sockjs.SockJsRequest;
import org.projectodd.sockjs.Transport;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;


public class SockJsEndpoint extends Endpoint {

    public SockJsEndpoint(Server server) {
        this.server = server;
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        System.err.println("!!! onOpen");
        SockJsRequest req = new SockJsWebsocketRequest(session);
        Transport.registerNoSession(req, server, new WebsocketReceiver(session));
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        System.err.println("!!! onClose");
    }

    @Override
    public void onError(Session session, Throwable thr) {
        System.err.println("!!! onError");
        // TODO: something better
        thr.printStackTrace();
    }

    private Server server;
}
