# SockJS-servlet

This is an in-progress SockJS server implementation designed to run in
any Java Servlet 3.1 container. Things are still a work-in-progress,
and for now it's only tested with [Undertow][].


## Testing

Testing is completely manual for now and depends on the
[sockjs-protocol][] project. I'm lazy and hacked `mvn test` to start
up a server on http://localhost:8081 that you can then run the
protocol tests against.

TODO is figuring out how to run these and the [sockjs-client][] QUnit
tests in an automated fashion.


[undertow]: http://undertow.io/
[sockjs-protocol]: https://github.com/sockjs/sockjs-protocol
[sockjs-client]: https://github.com/sockjs/sockjs-client
