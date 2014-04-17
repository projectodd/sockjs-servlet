package org.projectodd.sockjs;

import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentManager;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SockJsConnectionTest extends AbstractSockJsTest {

    @Before
    public void startServer() throws Exception {
        server = new SockJsServer();
        pathHandler = new PathHandler();
        undertow = createUndertow(pathHandler, "localhost", 8081);
        baseUrl = "http://localhost:8081";
        undertow.start();
    }

    @After
    public void stopServer() {
        undertow.stop();
    }

    @Test
    public void testDecoratedFromHttpRootContextRootMapping() throws Exception {
        verifyHttp("/", "/*");
    }

    @Test
    public void testDecoratedFromHttpNonRootContextRootMapping() throws Exception {
        verifyHttp("/foo", "/*");
    }

    @Test
    public void testDecoratedFromHttpNonRootContextNonRootMapping() throws Exception {
        verifyHttp("/foo", "/bar/*");
    }

    @Test
    public void testDecoratedFromWebsocketRootContextRootMapping() throws Exception {
        verifyWs("/", "/*");
    }

    @Test
    public void testDecoratedFromWebsocketNonRootContextRootMapping() throws Exception {
        verifyWs("/foo", "/*");
    }

    @Test
    public void testDecoratedFromWebsocketNonRootContextNonRootMapping() throws Exception {
        verifyWs("/foo", "/bar/*");
    }

    private void verifyHttp(final String context, final String mapping) throws Exception {
        verify(context, mapping, false, new VerifyBlock() {
            CloseableHttpResponse response = null;
            @Override
            public void connect(String prefix, String sessionId) throws Exception {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                HttpPost httpPost = new HttpPost(baseUrl + (context.equals("/") ? "" : context) + prefix + "/000/" + sessionId + "/xhr?foo=bar");
                response = httpClient.execute(httpPost);
                assertEquals(200, response.getStatusLine().getStatusCode());
                String body = EntityUtils.toString(response.getEntity());
                assertEquals("o\n", body);
            }
            @Override
            public void disconnect() throws Exception {
                response.close();
            }
        });
    }

    private void verifyWs(final String context, final String mapping) throws Exception {
        verify(context, mapping, true, new VerifyBlock() {
            WebSocketClient client = null;
            @Override
            public void connect(String prefix, String sessionId) throws Exception {
                client = new WebSocketClient();
                client.start();
                String baseWsUrl = baseUrl.replace("http", "ws");
                URI uri = new URI(baseWsUrl + (context.equals("/") ? "" : context) + prefix + "/000/" + sessionId + "/websocket?foo=bar");
                ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
                client.connect(new WebSocketListener() {
                    @Override
                    public void onWebSocketBinary(byte[] payload, int offset, int len) {
                    }
                    @Override
                    public void onWebSocketClose(int statusCode, String reason) {
                    }
                    @Override
                    public void onWebSocketConnect(Session session) {
                    }
                    @Override
                    public void onWebSocketError(Throwable cause) {
                    }
                    @Override
                    public void onWebSocketText(String message) {
                        assertEquals("o", message);
                    }
                }, uri, upgradeRequest);
            }
            @Override
            public void disconnect() throws Exception {
                client.stop();
                // sleep prevents some xnio stack on undertow shutdown
                Thread.sleep(25);
            }
        });
    }

    private void verify(final String context, final String mapping, final boolean websocket, VerifyBlock verifyBlock) throws Exception {
        final String sessionId = UUID.randomUUID().toString();
        final String prefix = extractPrefixFromMapping(mapping);
        final CountDownLatch connected = new CountDownLatch(1);
        server.onConnection(new SockJsServer.OnConnectionHandler() {
            @Override
            public void handle(SockJsConnection connection) {
                assertNotNull(connection.id);
                String suffix = "";
                if (websocket) {
                    suffix = "/websocket";
                    // TODO: Figure out if there's any way to get the remote IP and port
                    assertEquals(null, connection.remoteAddress);
                    assertEquals(0, connection.remotePort);
                    // TODO: and headers
                    assertEquals(null, connection.headers.get("host"));
                } else {
                    suffix = "/xhr";
                    assertEquals("127.0.0.1", connection.remoteAddress);
                    assertTrue(connection.remotePort > 0);
                    assertEquals("localhost:8081", connection.headers.get("host"));
                    assertTrue(connection.headers.get("user-agent").contains("Apache-HttpClient"));
                }
                String baseUrl = (context.equals("/") ? "" : context) + prefix + "/000/" + sessionId + suffix;
                assertEquals(baseUrl + "?foo=bar", connection.url);
                assertEquals("/000/" + sessionId + suffix, connection.pathname);
                assertEquals(context + prefix, connection.prefix);
                assertEquals(Transport.READY_STATE.OPEN, connection.getReadyState());
                connected.countDown();
            }
        });

        DeploymentManager manager = createDeploymentManager(server, context, mapping);
        manager.deploy();
        pathHandler.addPrefixPath(context, manager.start());

        verifyBlock.connect(prefix, sessionId);

        assertTrue(connected.await(5, TimeUnit.SECONDS));
        verifyBlock.disconnect();
        manager.stop();
    }

    private String extractPrefixFromMapping(String mapping) {
        if (mapping.endsWith("*")) {
            mapping = mapping.substring(0, mapping.length() - 1);
        }
        if (mapping.endsWith("/")) {
            mapping = mapping.substring(0, mapping.length() - 1);
        }
        return mapping;
    }

    private SockJsServer server;
    private PathHandler pathHandler;
    private Undertow undertow;
    private String baseUrl;

    private static interface VerifyBlock {
        void connect(String prefix, String sessionId) throws Exception;
        void disconnect() throws Exception;
    }
}
