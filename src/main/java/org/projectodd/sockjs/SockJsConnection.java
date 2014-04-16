/**
 * Copyright (C) 2014 Red Hat, Inc, and individual contributors.
 * Copyright (C) 2011-2012 VMware, Inc.
 */

package org.projectodd.sockjs;

public class SockJsConnection {

    public SockJsConnection(Session session) {
        this.session = session;
        this.id = Utils.uuid();
    }

    @Override
    public String toString() {
        return "<SockJSConnection " + id + ">";
    }

    public boolean write(String payload) {
        return session.send(payload);
    }

    public void end(String string) {
        if (string != null) {
            write(string);
        }
        close();
    }

    public boolean close() {
        return session.close();
    }
    public boolean close(int code, String reason) {
        return session.close(code, reason);
    }

    public void destroy() {
        end(null);
    }

    /**
     * Called for every message received from a client
     *
     * @param onDataHandler The handler to call when messages arrive
     */
    public void onData(OnDataHandler onDataHandler) {
        this.onDataHandler = onDataHandler;
    }
    public void emitData(String message) {
        if (onDataHandler != null) {
            onDataHandler.handle(message);
        }
    }

    /**
     * Called when a connection to a client is closed
     *
     * @param onCloseHandler The handler to call when a connection is closed
     */
    public void onClose(OnCloseHandler onCloseHandler) {
        this.onCloseHandler = onCloseHandler;
    }
    public void emitClose() {
        if (onCloseHandler != null) {
            onCloseHandler.handle();
        }
    }

    private Session session;
    private OnDataHandler onDataHandler;
    private OnCloseHandler onCloseHandler;

    public String id;
    public String protocol;

    public static interface OnDataHandler {
        public void handle(String message);
    }

    public static interface OnCloseHandler {
        public void handle();
    }
}
