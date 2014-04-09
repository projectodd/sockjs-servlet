package org.projectodd.sockjs;

public class Transport {

    public static int CONNECTING = 0;
    public static int OPEN = 1;
    public static int CLOSING = 2;
    public static int CLOSED = 3;

    public static String closeFrame(int status, String reason) {
        return "c" + "[" + status + ",\"" + reason + "\"]";
    }

    public static Session register(SockJsRequest req, Server server, ResponseReceiver receiver) {
        return register(req, server, req.session(), receiver);
    }

    public static Session register(SockJsRequest req, Server server, String sessionId, ResponseReceiver receiver) {
        Session session = Session.bySessionId(sessionId);
        if (session == null) {
            session = new Session(sessionId, server);
        }
        session.register(req, receiver);
        return session;
    }
}
