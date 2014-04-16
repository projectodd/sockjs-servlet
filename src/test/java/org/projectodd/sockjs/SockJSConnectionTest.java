package org.projectodd.sockjs;

import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentManager;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class SockJSConnectionTest extends AbstractSockJsTest {

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
    public void testDecoratedFromHttp() throws Exception {
        final AtomicBoolean connected = new AtomicBoolean(false);
        server.onConnection(new SockJsServer.OnConnectionHandler() {
            @Override
            public void handle(SockJsConnection connection) {
                assertNotNull(connection.id);
                assertEquals("127.0.0.1", connection.remoteAddress);
                assertTrue(connection.remotePort > 0);
                assertEquals("localhost:8081", connection.headers.get("host"));
                assertTrue(connection.headers.get("user-agent").contains("Apache-HttpClient"));
                assertEquals("/foo/000/001/xhr?foo=bar", connection.url);
                assertEquals("/000/001/xhr", connection.pathname);
                assertEquals("/foo", connection.prefix);
                assertEquals(Transport.READY_STATE.OPEN, connection.getReadyState());
                connected.set(true);
            }
        });

        String context = "/foo";
        DeploymentManager manager = createDeploymentManager(server, context);
        manager.deploy();
        pathHandler.addPrefixPath(context, manager.start());

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(baseUrl + "/foo/000/001/xhr?foo=bar");
        CloseableHttpResponse response = httpClient.execute(httpPost);
        assertEquals(200, response.getStatusLine().getStatusCode());
        String body = EntityUtils.toString(response.getEntity());
        assertEquals("o\n", body);
        response.close();

        assertTrue(connected.get());
        manager.stop();
    }

    @Test
    public void testDecoratedFromWs() {

    }

    private SockJsServer server;
    private PathHandler pathHandler;
    private Undertow undertow;
    private String baseUrl;
}
