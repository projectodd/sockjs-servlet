package org.projectodd.sockjs;

public class XhrStreamingReceiver extends ResponseReceiver {

    public XhrStreamingReceiver(SockJsRequest req, SockJsResponse res, Server.Options options) {
        super(req, res, options);
        protocol = "xhr-streaming";
    }

    @Override
    public boolean doSendFrame(String payload) {
        System.err.println("!!! XhrStreamingReceiver doSendFrame " + payload);
        return super.doSendFrame(payload + "\n");
    }
}
