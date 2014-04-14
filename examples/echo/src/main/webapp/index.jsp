<%@ page language="java" contentType="text/html; charset=UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
  <head>
    <script src="http://cdn.sockjs.org/sockjs-0.3.min.js">
    </script>
    <script>
      var sock = new SockJS("http://<%= request.getServerName() %>:<%= request.getServerPort() %><%= request.getContextPath() %>/echo");
      sock.onopen = function() {
        console.log('open');
        sock.send('testing');
      };
      sock.onmessage = function(e) {
        console.log('message', e.data);
      }
      sock.onclose = function() {
        console.log('close');
      }
    </script>
  </head>
  <body>
    <h1>SockJS Test</h1>
  </body>
</html>
