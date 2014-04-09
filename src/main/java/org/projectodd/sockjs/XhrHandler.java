package org.projectodd.sockjs;

import java.util.ArrayList;
import java.util.List;

/**
 * Handlers from sockjs-node's trans-xhr.coffee
 */
public class XhrHandler {

    public DispatchFunction xhrOptions = new DispatchFunction() {
        @Override
        public Object handle(SockJsRequest req, SockJsResponse res, Object data) throws SockJsException {
            res.statusCode(204);
            res.setHeader("Access-Control-Allow-Methods", "OPTIONS, POST");
            res.setHeader("Access-Control-Max-Age", "" + res.cacheFor());
            return "";
        }
    };

    public DispatchFunction xhrSend = new DispatchFunction() {
        @Override
        @SuppressWarnings("unchecked")
        public Object handle(SockJsRequest req, SockJsResponse res, Object data) throws SockJsException {
            System.err.println("!!! XHR SEND");
            if (data == null || data.toString().length() == 0) {
                throw new DispatchException(500, "Payload expected.");
            }
            List<String> d;
            try {
                d = Utils.parseJson(data.toString(), List.class);
            } catch (Exception e) {
                throw new DispatchException(500, "Broken JSON encoding.");
            }
            Session jsonp = Session.bySessionId(req.session());
            if (jsonp == null) {
                throw new DispatchException(404);
            }
            for (String message : d) {
                jsonp.didMessage(message);
            }
            res.setHeader("Content-Type", "text/plain; charset=UTF-8");
            res.writeHead(204);
            res.end();
            return true;
        }
    };

    public DispatchFunction xhrCors = new DispatchFunction() {
        @Override
        public Object handle(SockJsRequest req, SockJsResponse res, Object content) throws SockJsException {
            String origin = req.getHeader("origin");
            if (origin == null || origin.equals("null")) {
                origin = "*";
            }
            res.setHeader("Access-Control-Allow-Origin", origin);
            String headers = req.getHeader("access-control-request-headers");
            if (headers != null) {
                res.setHeader("Access-Control-Allow-Headers", headers);
            }
            res.setHeader("Access-Control-Allow-Credentials", "true");
            return content;
        }
    };

    public DispatchFunction xhrPoll = new DispatchFunction() {
        @Override
        public Object handle(SockJsRequest req, SockJsResponse res, Object data) throws SockJsException {
            res.setHeader("Content-Type", "application/javascript; charset=UTF-8");
            res.writeHead(200);

            Transport.register(req, server, new XhrPollingReceiver(req, res, server.options));
            return true;
        }
    };

    public DispatchFunction xhrStreaming = new DispatchFunction() {
        @Override
        public Object handle(SockJsRequest req, SockJsResponse res, Object data) throws SockJsException {
            res.setHeader("Content-Type", "application/javascript; charset=UTF-8");
            res.writeHead(200);

            int ieByteCount = 2049;
            List<String> ieWorkaround = new ArrayList<>(ieByteCount);
            for (int i = 0; i < ieByteCount; i++) {
                ieWorkaround.add("");
            }
            res.write(Utils.join(ieWorkaround, "h") + "\n");

            Transport.register(req, server, new XhrStreamingReceiver(req, res, server.options));
            return true;
        }
    };

    public XhrHandler(Server server) {
        this.server = server;
    }

    private Server server;
}
