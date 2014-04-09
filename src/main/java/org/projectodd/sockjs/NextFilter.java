package org.projectodd.sockjs;

public interface NextFilter {
    public void handle(Object data) throws SockJsException;
}
