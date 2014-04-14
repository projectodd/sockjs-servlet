package org.projectodd.sockjs.examples.echo;

import org.projectodd.sockjs.SockJsConnection;
import org.projectodd.sockjs.SockJsServer;
import org.projectodd.sockjs.servlet.SockJsServlet;

import javax.servlet.ServletException;

public class EchoServlet extends SockJsServlet {

    @Override
    public void init() throws ServletException {
        SockJsServer echoServer = new SockJsServer();
        // Various options can be set on the server, such as:
        echoServer.options.responseLimit = 4 * 1024;
        // onConnection is the main entry point for handling SockJS connections
        echoServer.onConnection(new SockJsServer.OnConnectionHandler() {
            @Override
            public void handle(final SockJsConnection connection) {
                getServletContext().log("SockJS client connected");

                // onData gets called when a client sends data to the server
                connection.onData(new SockJsConnection.OnDataHandler() {
                    @Override
                    public void handle(String message) {
                        connection.write(message);
                    }
                });

                // onClose gets called when a client disconnects
                connection.onClose(new SockJsConnection.OnCloseHandler() {
                    @Override
                    public void handle() {
                        getServletContext().log("SockJS client disconnected");
                    }
                });
            }
        });
        
        setServer(echoServer);
        // Don't forget to call super.init() to wire everything up
        super.init();
    }
}
