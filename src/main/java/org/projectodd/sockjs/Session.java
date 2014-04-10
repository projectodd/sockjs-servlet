/**
 * Copyright (C) 2014 Red Hat, Inc, and individual contributors.
 * Copyright (C) 2011-2012 VMware, Inc.
 */

package org.projectodd.sockjs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public class Session {

    public Session(String sessionId, final Server server) {
        this.sessionId = sessionId;
        this.server = server;
        heartbeatDelay = server.options.heartbeatDelay;
        disconnectDelay = server.options.disconnectDelay;
        sendBuffer = new ArrayList<>();
        readyState = Transport.CONNECTING;
        if (sessionId != null && sessionId.length() > 0) {
            System.err.println("!!! Adding session " + sessionId);
            sessions.put(sessionId, this);
        }
        timeoutCb = new Runnable() {
            @Override
            public void run() {
                didTimeout();
            }
        };
        toTref = server.setTimeout(timeoutCb, disconnectDelay);
        connection = new SockJsConnection(this);
        emitOpen = new Runnable() {
            @Override
            public void run() {
                emitOpen = null;
                server.emitConnection(connection);
            }
        };
    }

    public void register(SockJsRequest req, GenericReceiver recv) {
        if (this.recv != null) {
            recv.doSendFrame(Transport.closeFrame(2010, "Another connection still open"));
            recv.didClose();
            return;
        }
        if (toTref != null) {
            server.clearTimeout(toTref);
            toTref = null;
        }
        if (readyState == Transport.CLOSING) {
            flushToRecv(recv);
            recv.doSendFrame(closeFrame);
            recv.didClose();
            toTref = server.setTimeout(timeoutCb, disconnectDelay);
            return;
        }
        this.recv = recv;
        recv.session = this;

        decorateConnection(req);

        if (readyState == Transport.CONNECTING) {
            recv.doSendFrame("o");
            readyState = Transport.OPEN;
            // TODO: sockjs-node does this on process.nextTick
            emitOpen.run();
        }

        if (recv == null) {
            return;
        }
        tryFlush();
    }

    private void decorateConnection(SockJsRequest req) {
        // TODO: actually decorate this sucker
        connection.protocol = recv.protocol;
    }

    public void unregister() {
        recv.session = null;
        recv = null;
        if (toTref != null) {
            server.clearTimeout(toTref);
        }
        toTref = server.setTimeout(timeoutCb, disconnectDelay);
    }

    private boolean flushToRecv(GenericReceiver receiver) {
        if (sendBuffer.size() > 0) {
            List<String> sb = new ArrayList<>(sendBuffer);
            sendBuffer = new ArrayList<>();
            recv.doSendBulk(sb);
            return true;
        }
        return false;
    }

    private void tryFlush() {
        if (!flushToRecv(recv)) {
            if (toTref != null) {
                server.clearTimeout(toTref);
            }
            Runnable x = new Runnable() {
                @Override
                public void run() {
                    if (recv != null) {
                        toTref = server.setTimeout(this, heartbeatDelay);
                        recv.doSendFrame("h");
                    }
                }
            };
            toTref = server.setTimeout(x, heartbeatDelay);
        }
    }

    private void didTimeout() {
        if (toTref != null) {
            server.clearTimeout(toTref);
            toTref = null;
        }
        if (readyState != Transport.CONNECTING &&
                readyState != Transport.OPEN &&
                readyState != Transport.CLOSING) {
            // TODO: Use some other exception class
            throw new RuntimeException("INVALID_STATE_ERR");
        }
        if (recv != null) {
            // TODO: Use some other exception class
            throw new RuntimeException("RECV_STILL_THERE");
        }
        readyState = Transport.CLOSED;
        connection.emitEnd();
        connection.emitClose();
        connection = null;
        if (sessionId != null) {
            System.err.println("!!! Removing session " + sessionId);
            sessions.remove(sessionId);
            sessionId = null;
        }
    }

    public void didMessage(String payload) {
        System.err.println("!!! didMessage");
        if (readyState == Transport.OPEN) {
            connection.emitData(payload);
        }
    }

    public boolean send(String payload) {
        if (readyState != Transport.OPEN) {
            return false;
        }
        sendBuffer.add(payload);
        if (recv != null) {
            tryFlush();
        }
        return true;
    }

    public boolean close() {
        return close(1000, "Normal closure");
    }

    public boolean close(int status, String reason) {
        if (readyState != Transport.OPEN) {
            return false;
        }
        readyState = Transport.CLOSING;
        closeFrame = Transport.closeFrame(status, reason);
        if (recv != null) {
            recv.doSendFrame(closeFrame);
            if (recv != null) {
                recv.didClose();
            }
            if (recv != null) {
                unregister();
            }
        }
        return true;
    }

    public static Session bySessionId(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        return sessions.get(sessionId);
    }

    private String sessionId;
    private Server server;
    private int disconnectDelay;
    private int heartbeatDelay;
    private List<String> sendBuffer;
    private int readyState;
    private Runnable timeoutCb;
    private ScheduledFuture toTref;
    private SockJsConnection connection;
    private Runnable emitOpen;
    private GenericReceiver recv;
    private String closeFrame;

    // TODO: Should this  be scoped to Server instances instead of across all apps?
    private static Map<String, Session> sessions = new ConcurrentHashMap<>();
}
