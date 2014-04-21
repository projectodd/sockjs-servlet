# SockJS Servlet

[![Build Status](https://travis-ci.org/projectodd/sockjs-servlet.svg?branch=master)](https://travis-ci.org/projectodd/sockjs-servlet)

This is an in-progress SockJS server implementation designed to run in
any Java Servlet 3.1 container. Things are still a work-in-progress
but very usable as-is.

All the protocols have been implemented and I've verified that a basic
browser SockJS client works with the echo example on [Undertow],
[WildFly 8][wildfly], and [Tomcat 8].

## Releases

All releases are published to Maven Central at
http://central.maven.org/maven2/org/projectodd/sockjs/sockjs-servlet/

Example Maven &lt;dependency&gt; entry to pull in the latest version:

    <dependency>
      <groupId>org.projectodd.sockjs</groupId>
      <artifactId>sockjs-servlet</artifactId>
      <version>[0.1.0,)</version>
    </dependency>

## Echo Example

See our [echo example](examples/echo) for an example of building a
.war that uses SockJS Servlet. The meat of the code is in the
[EchoServlet](examples/echo/src/main/java/org/projectodd/sockjs/examples/echo/EchoServlet.java)
class. To build the example from a fresh clone of this repo:

    cd examples/echo
    mvn clean package

Deploy the resulting `target/echo.war` to your favorite Servlet 3.1 or
Java EE 7 container and open http://localhost:8080/echo (or whatever
host/port your container runs on) to play with the example.

## Building SockJS Servlet

    mvn install

## Running SockJS Protocol tests

The only tests right now depend on the [sockjs-protocol][]
project. Make sure you have Python 2.x and virtualenv installed - see
the [sockjs-protocol README][sockjs-protocol-tests] for how to do
this. Once you get that done:

    git submodule init
    git submodule update
    mvn verify -Pintegration-tests

There are a couple of tests that are expected to fail just due to
differences in HTTP connection handling between Node and Servlet
containers. The build won't fail if any of these expected failures
occur, but will if anything unexpected fails. If there are any
unexpected failures, `target/sockjs-protocol-output.log` and
`target/server-output.log` are helpful in figuring out what went
wrong.

## Known Issues

* The client's remote IP and port are not available on
  SockJsConnection when using websockets. This is due to a limitation
  of the servlet 3.1 and websocket spec in Java that gives us no way
  to access this information.

* The client's headers are not available on SockJsConnection when
  using raw (non-browser) websockets. This is due to the same
  limitation as remote IP / port above, but when using browser-based
  websockets we can hack around it by storing some information keyed
  off the internal sockjs session id. This isn't an option when using
  raw websockets.

* We don't immediately respond to closing of connections at the socket
  level, and instead wait for the next heartbeat interval to realize
  the client closed the connection. We'll likely need some
  server-specific hacks to be able to handle this kind of connection
  closure from the TCP level.


[undertow]: http://undertow.io/
[wildfly]: http://wildfly.org/
[tomcat 8]: http://tomcat.apache.org/download-80.cgi
[sockjs-protocol]: https://github.com/sockjs/sockjs-protocol
[sockjs-protocol-tests]: https://github.com/sockjs/sockjs-protocol#running-tests
[sockjs-client]: https://github.com/sockjs/sockjs-client
