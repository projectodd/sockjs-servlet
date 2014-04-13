# SockJS-servlet

[![Build Status](https://travis-ci.org/projectodd/sockjs-servlet.svg?branch=master)](https://travis-ci.org/projectodd/sockjs-servlet)

This is an in-progress SockJS server implementation designed to run in
any Java Servlet 3.1 container. Things are still a work-in-progress,
and for now it's only tested with [Undertow][].

All the protocols have been implemented and I've verified that a basic
SockJS client can connect.

## Building

    mvn install

## Running SockJS Protocol tests

The only tests right now depend on the [sockjs-protocol][]
project. Make sure you have Python 2.x and virtualenv installed - see
the [sockjs-protocol README][sockjs-protocol-tests] for how to do
this. Once you get that done:

    git submodule init
    git submodule update
    mvn verify


[undertow]: http://undertow.io/
[sockjs-protocol]: https://github.com/sockjs/sockjs-protocol
[sockjspprotocol-tests]: https://github.com/sockjs/sockjs-protocol#running-tests
[sockjs-client]: https://github.com/sockjs/sockjs-client
