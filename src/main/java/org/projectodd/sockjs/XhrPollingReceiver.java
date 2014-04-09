/**
 * Copyright (C) 2014 Red Hat, Inc, and individual contributors.
 * Copyright (C) 2011-2012 VMware, Inc.
 */

package org.projectodd.sockjs;

public class XhrPollingReceiver extends XhrStreamingReceiver {

    public XhrPollingReceiver(SockJsRequest req, SockJsResponse res, Server.Options options) {
        super(req, res, options);
        protocol = "xhr-polling";
        maxResponseSize = 1;
    }
}
