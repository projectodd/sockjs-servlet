package org.projectodd.sockjs.servlet;

import org.projectodd.sockjs.SockJsRequest;

import javax.websocket.Session;
import java.util.List;

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

    @Override
    public String getQueryParameter(String name) {
        List<String> paramValues = session.getRequestParameterMap().get(name);
        if (paramValues != null && paramValues.size() > 0) {
            return paramValues.get(0);
        }
        return null;
    }

    private Session session;
}
