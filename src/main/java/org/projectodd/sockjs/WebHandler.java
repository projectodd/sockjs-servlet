package org.projectodd.sockjs;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Handlers from sockjs-node's webjs.coffee
 */
public class WebHandler {

    public DispatchFunction handle405 = new DispatchFunction() {
        @Override
        @SuppressWarnings("unchecked")
        public Object handle(SockJsRequest req, SockJsResponse res, Object data) throws SockJsException {
            List<String> methods = (List<String>) data;
            res.setHeader("Allow", Utils.join(methods, ", "));
            res.writeHead(405);
            res.end();
            return true;
        }
    };

    public DispatchFunction handleError = new DispatchFunction() {
        @Override
        public Object handle(SockJsRequest req, SockJsResponse res, Object data) throws SockJsException {
            Exception x = (Exception) data;
            if (res.finished()) {
                return x;
            }
            x.printStackTrace();
            if (x instanceof DispatchException) {
                DispatchException dx = (DispatchException) x;
                System.err.println(dx.message);
                res.writeHead(dx.status);
                String message = dx.message;
                if (message == null) {
                    message = "";
                }
                res.end(message);
            } else {
                try {
                    res.writeHead(500);
                    res.end("500 - Internal Server Error");
                } catch (Exception e) {
                    // ignore
                }
            }
            return true;
        }
    };

    public DispatchFunction expose = new DispatchFunction() {
        @Override
        public Object handle(SockJsRequest req, SockJsResponse res, Object content) throws SockJsException {
            if (res.finished()) {
                return content;
            }
            boolean hasContent = content != null && content.toString().length() > 0;
            if (hasContent && res.getHeader("Content-Type") == null) {
                res.setHeader("Content-Type", "text/plain");
            }
            if (hasContent) {
                try {
                    res.setHeader("Content-Length", "" + content.toString().getBytes("UTF-8").length);
                } catch (IOException ex) {
                    throw new SockJsException("Error writing Content-Length header:", ex);
                }
            }
            res.writeHead(res.statusCode());
            res.end(content.toString());
            return true;
        }
    };

    public DispatchFunction cacheFor = new DispatchFunction() {
        @Override
        public Object handle(SockJsRequest req, SockJsResponse res, Object content) throws SockJsException {
            if (res.cacheFor() == null) {
                res.cacheFor(365 * 24 * 60 * 60); // one year
            }
            res.setHeader("Cache-Control", "public, max-age=" + res.cacheFor());
            res.setHeader("Expires", Utils.generateExpires(new Date(System.currentTimeMillis() + res.cacheFor() * 1000)));
            return content;
        }
    };

    public DispatchFunction hNoCache = new DispatchFunction() {
        @Override
        public Object handle(SockJsRequest req, SockJsResponse res, Object content) throws SockJsException {
            res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            return content;
        }
    };

    public DispatchFunction expectXhr = new DispatchFunction() {
        @Override
        public Object handle(final SockJsRequest req, final SockJsResponse res, Object _data) throws SockJsException {
            System.err.println("!!! EXPECTING XHR");
            final Buffer data = new Buffer(0);
            req.onData(new SockJsRequest.OnDataHandler() {
                @Override
                public void handle(byte[] d) {
                    System.err.println("!!! ON DATA " + new String(d));
                    data.concat(new Buffer(d));
                }
            });
            req.onEnd(new SockJsRequest.OnEndHandler() {
                @Override
                public void handle() {
                    System.err.println("!!! ON END");
                    String contentType = req.getContentType();
                    if (contentType == null) {
                        contentType = "";
                    }
                    String q;
                    switch(contentType.split(";")[0]) {
                        case "text/plain":
                        case "T":
                        case "application/json":
                        case "application/xml":
                        case "":
                        case "text/xml":
                            q = data.toString("UTF-8");
                            break;
                        default:
                            q = null;
                            break;
                    }
                    try {
                        System.err.println("!!! Q IS " + q);
                        req.nextFilter.handle(q);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            throw new DispatchException(0);
        }
    };

    public WebHandler(Server server) {
        this.server = server;
    }

    private Server server;
}
