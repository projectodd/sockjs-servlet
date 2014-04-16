# SockJS Servlet

[![Build Status](https://travis-ci.org/projectodd/sockjs-servlet.svg?branch=master)](https://travis-ci.org/projectodd/sockjs-servlet)

This is an in-progress SockJS server implementation designed to run in
any Java Servlet 3.1 container. Things are still a work-in-progress,
and for now it's only tested with [Undertow][].

All the protocols have been implemented and I've verified that a basic
browser SockJS client can connect.

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
occur, but will if anything unexpected fails.


[undertow]: http://undertow.io/
[sockjs-protocol]: https://github.com/sockjs/sockjs-protocol
[sockjs-protocol-tests]: https://github.com/sockjs/sockjs-protocol#running-tests
[sockjs-client]: https://github.com/sockjs/sockjs-client
