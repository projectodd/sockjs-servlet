package org.projectodd.sockjs;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

import java.util.Random;

public class InfoHandler implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.setResponseCode(200);
        HeaderMap responseHeaders = exchange.getResponseHeaders();
        responseHeaders.add(Headers.CONTENT_TYPE, "application/json;charset=UTF-8");
        responseHeaders.add(Headers.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0");
        cors(exchange);
        Random random = new Random();
        long entropy = random.nextInt(Integer.MAX_VALUE) + random.nextInt(Integer.MAX_VALUE);
        exchange.getResponseSender().send("{" +
                "\"websocket\": " + websocketsEnabled + ", " +
                "\"cookie_needed\": false, " +
                "\"origins\": [\"*:*\"], " +
                "\"entropy\": " + entropy +
                "}");
    }

    private void cors(HttpServerExchange exchange) {
        String origin = exchange.getRequestHeaders().get(Headers.ORIGIN, 0);
        if (origin == null || origin.equals("null")) {
            origin = "*";
        }
        HeaderMap responseHeaders = exchange.getResponseHeaders();
        responseHeaders.add(new HttpString("Access-Control-Allow-Origin"), origin);
        responseHeaders.add(new HttpString("Access-Control-Allow-Credentials"), "true");
    }

    public boolean websocketsEnabled = true;

}
