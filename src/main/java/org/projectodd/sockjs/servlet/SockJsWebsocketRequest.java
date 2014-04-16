package org.projectodd.sockjs.servlet;

import org.projectodd.sockjs.SockJsRequest;

import javax.websocket.Session;
import java.util.List;

public class SockJsWebsocketRequest extends SockJsRequest {

    public SockJsWebsocketRequest(Session session, String prefix) {
        this.session = session;
        this.prefix = prefix;
    }

    @Override
    public String getMethod() {
        // Let's just pretend they're all GETs
        return "GET";
    }

    @Override
    public String getUrl() {
        return session.getRequestURI().toString();
    }

    @Override
    public String getPath() {
        String path = session.getRequestURI().getPath();
        if (path != null && path.startsWith(prefix)) {
            path = path.substring(prefix.length());
        }
        return path;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String getRemoteAddr() {
        // TODO: grab this during the handshake process
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
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
    private String prefix;
}
