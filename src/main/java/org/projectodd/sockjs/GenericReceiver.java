/**
 * Copyright (C) 2014 Red Hat, Inc, and individual contributors.
 * Copyright (C) 2011-2012 VMware, Inc.
 */

package org.projectodd.sockjs;

import java.util.ArrayList;
import java.util.List;

public abstract class GenericReceiver {
    protected void didClose() {
        if (session != null) {
            session.unregister();
        }
    }

    public void doSendBulk(List<String> messages) {
        List<String> qMsgs = new ArrayList<>(messages.size());
        for (String m : messages) {
            qMsgs.add(Utils.quote(m));
        }
        doSendFrame("a[" + Utils.join(qMsgs, ",") + "]");
    }

    public abstract boolean doSendFrame(String payload);

    public Session session;
}
