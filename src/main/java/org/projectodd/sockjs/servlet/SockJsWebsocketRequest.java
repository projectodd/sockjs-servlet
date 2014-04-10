package org.projectodd.sockjs.servlet;

import org.projectodd.sockjs.SockJsRequest;

import javax.websocket.Session;

public class SockJsWebsocketRequest extends SockJsRequest {

    public SockJsWebsocketRequest(Session session) {
        this.session = session;
    }

    @Override
    public String getMethod() {
        // Let's just pretend they're all GETs
        return "GET";
    }

    @Override
    public String getPath() {
        return session.getRequestURI().getPath();
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public String getCookie(String name) {
        return null;
    }

    private Session session;
}
