/**
 * Copyright (C) 2014 Red Hat, Inc, and individual contributors.
 * Copyright (C) 2011-2012 VMware, Inc.
 */

package org.projectodd.sockjs;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class Server {

    public Server() {
    }

    public void init() {
        // Set everything up here instead of the constructor so that we have a chance to set options
        appHandler = new AppHandler(this);
        webHandler = new WebHandler(this);
        iframeHandler = new IframeHandler(this);
        chunkingHandler = new ChunkingHandler(this);
        websocketHandler = new WebsocketHandler(this);
        xhrHandler = new XhrHandler(this);
        eventsourceHandler = new EventsourceHandler(this);

        dispatcher = new Dispatcher(appHandler.handle404, webHandler.handle405, webHandler.handleError);
        dispatcher.push("GET", p(""), appHandler.welcomeScreen);
        dispatcher.push("GET", p("/iframe[0-9-.a-z_]*.html"), iframeHandler.iframe,
                webHandler.cacheFor, webHandler.expose);
        dispatcher.push("OPTIONS", p("/info"), optsFilters(chunkingHandler.infoOptions));
        dispatcher.push("GET", p("/info"), xhrHandler.xhrCors, webHandler.hNoCache,
                chunkingHandler.info, webHandler.expose);
        dispatcher.push("GET", p("/websocket"), websocketHandler.rawWebsocket);
        dispatcher.push("POST", t("/xhr"), appHandler.hSid, webHandler.hNoCache, xhrHandler.xhrCors, xhrHandler.xhrPoll);
        dispatcher.push("OPTIONS", t("/xhr"), optsFilters());
        dispatcher.push("POST", t("/xhr_send"), appHandler.hSid, webHandler.hNoCache, xhrHandler.xhrCors, webHandler.expectXhr, xhrHandler.xhrSend);
        dispatcher.push("OPTIONS", t("/xhr_send"), optsFilters());
        dispatcher.push("POST", t("/xhr_streaming"), appHandler.hSid, webHandler.hNoCache, xhrHandler.xhrCors, xhrHandler.xhrStreaming);
        dispatcher.push("OPTIONS", t("/xhr_streaming"), optsFilters());
        dispatcher.push("GET", t("/eventsource"), appHandler.hSid, webHandler.hNoCache, eventsourceHandler.eventsource);

        if (options.websocket) {
            dispatcher.push("GET", t("/websocket"), websocketHandler.sockjsWebsocket);
        } else {
            dispatcher.push("GET", t("/websocket"), webHandler.cacheFor, appHandler.disabledTransport);
        }

        scheduledExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = Executors.defaultThreadFactory().newThread(r);
                // Mark as a daemon thread so we never prevent shutdown
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    public void destroy() {
        scheduledExecutor.shutdownNow();
    }

    public void dispatch(SockJsRequest req, SockJsResponse res) throws SockJsException {
        dispatcher.dispatch(req, res);
    }

    protected String p(String match) {
        return "^" + options.prefix + match + "[/]?$";
    }

    protected String[] t(String match) {
        String pattern = p("/([^/.]+)/([^/.]+)" + match);
        return new String[] { pattern, "server", "session" };
    }

    protected DispatchFunction[] optsFilters() {
        return optsFilters(xhrHandler.xhrOptions);
    }
    protected DispatchFunction[] optsFilters(DispatchFunction optionsFilter) {
        return new DispatchFunction[] { appHandler.hSid, xhrHandler.xhrCors, webHandler.cacheFor, optionsFilter, webHandler.expose };
    }

    public void onConnection(OnConnectionHandler handler) {
        onConnectionHandler = handler;
    }

    public void emitConnection(SockJsConnection connection) {
        if (onConnectionHandler != null) {
            onConnectionHandler.handle(connection);
        }
    }

    public ScheduledFuture setTimeout(Runnable callback, long delay) {
        return scheduledExecutor.schedule(callback, delay, TimeUnit.MILLISECONDS);
    }

    public void clearTimeout(ScheduledFuture future) {
        future.cancel(false);
    }

    private Dispatcher dispatcher;
    private AppHandler appHandler;
    private WebHandler webHandler;
    private IframeHandler iframeHandler;
    private ChunkingHandler chunkingHandler;
    private WebsocketHandler websocketHandler;
    private XhrHandler xhrHandler;
    private EventsourceHandler eventsourceHandler;
    private ScheduledExecutorService scheduledExecutor;
    private OnConnectionHandler onConnectionHandler;
    public Options options = new Options();

    public static class Options {
        public String prefix = "";
        public int responseLimit = 128 * 1024;
        public boolean websocket = true;
        public boolean jsessionid = false;
        public int heartbeatDelay = 25000;
        public int disconnectDelay = 5000;
        public String sockjsUrl = "http://cdn.sockjs.org/sockjs-0.3.min.js";
        public String baseUrl = null;
    }

    public static interface OnConnectionHandler {
        public void handle(SockJsConnection connection);
    }
}
