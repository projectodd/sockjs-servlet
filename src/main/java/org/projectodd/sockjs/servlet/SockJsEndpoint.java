/**
 * Copyright (C) 2014 Red Hat, Inc, and individual contributors.
 * Copyright (C) 2011-2012 VMware, Inc.
 */

package org.projectodd.sockjs.servlet;

import org.projectodd.sockjs.GenericReceiver;
import org.projectodd.sockjs.Server;
import org.projectodd.sockjs.SockJsRequest;
import org.projectodd.sockjs.Transport;
import org.projectodd.sockjs.Utils;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import java.io.IOException;
import java.util.List;


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

    /**
     * WebsocketReceiver logic from sockjs-node's trans-websocket.coffee
     */
    private static class WebsocketReceiver extends GenericReceiver {

        public WebsocketReceiver(Session ws) {
            protocol = "websocket";
            this.ws = ws;
            this.ws.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    didMessage(message);
                }
            });
        }

        @SuppressWarnings("unchecked")
        private void didMessage(String payload) {
            if (ws != null && session != null && payload.length() > 0) {
                if (payload.charAt(0) == '[') {
                    List<String> messages;
                    try {
                        messages = Utils.parseJson(payload, List.class);
                    } catch (Exception x) {
                        try {
                            ws.close(new CloseReason(CloseReason.CloseCodes.PROTOCOL_ERROR, "Broken framing"));
                        } catch (IOException e) {}
                        return;
                    }
                    for (String message : messages) {
                        session.didMessage(message);
                    }
                } else {
                    String message;
                    try {
                        message = Utils.parseJson(payload, String.class);
                    } catch (Exception x) {
                        try {
                            ws.close(new CloseReason(CloseReason.CloseCodes.PROTOCOL_ERROR, "Broken framing"));
                        } catch (IOException e) {}
                        return;
                    }
                    session.didMessage(message);
                }
            }
        }

        @Override
        public boolean doSendFrame(String payload) {
            if (ws != null) {
                try {
                    ws.getBasicRemote().sendText(payload);
                    return true;
                } catch (IOException x) {
                    didClose();
                }
            }
            return false;
        }

        @Override
        protected void didClose() {
            super.didClose();
            try {
                ws.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Normal closure"));
            } catch (IOException x) {
                x.printStackTrace();
            }
            ws = null;
        }

        private Session ws;
    }
}
