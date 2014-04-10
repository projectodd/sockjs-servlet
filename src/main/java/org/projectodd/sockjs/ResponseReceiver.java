/**
 * Copyright (C) 2014 Red Hat, Inc, and individual contributors.
 * Copyright (C) 2011-2012 VMware, Inc.
 */

package org.projectodd.sockjs;

public class ResponseReceiver extends GenericReceiver {
    public ResponseReceiver(SockJsRequest request, SockJsResponse response, Server.Options options) {
        this.request = request;
        this.response = response;
        this.options = options;
        currResponseSize = 0;
        maxResponseSize = options.responseLimit;
    }

    public boolean doSendFrame(String payload) {
        currResponseSize += payload.length();
        boolean r = false;
        try {
            response.write(payload);
            r = true;
        } catch (SockJsException x) {
            didClose();
            return r;
        }
        if (maxResponseSize >= 0 && currResponseSize >= maxResponseSize) {
            didClose();
        }
        return r;
    }

    @Override
    protected void didClose() {
        super.didClose();
        try {
            response.end();
        } catch (Exception x) {}
        response = null;
    }

    protected SockJsRequest request;
    protected SockJsResponse response;
    protected Server.Options options;
    protected int currResponseSize;
    protected int maxResponseSize = -1;
}
