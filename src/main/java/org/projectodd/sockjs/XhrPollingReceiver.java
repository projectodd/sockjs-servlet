package org.projectodd.sockjs;

public class XhrPollingReceiver extends XhrStreamingReceiver {

    public XhrPollingReceiver(SockJsRequest req, SockJsResponse res, Server.Options options) {
        super(req, res, options);
        protocol = "xhr-polling";
        maxResponseSize = 1;
    }
}
