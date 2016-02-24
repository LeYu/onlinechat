<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
 
<!DOCTYPE HTML>
<html>
  <head>
    <base href="<%=basePath%>">
    <title>My WebSocket</title>
  </head>
   
  <body>
    Welcome<br/>
    <input id="text" type="text" /><button onclick="send()">Send</button>
    <button onclick="closeWebSocket()">Close</button>
    <div id="message">
    </div>
  </body>
   
  <script type="text/javascript">
      var websocket = null;
       
      //check browser support WebSocket
      if('WebSocket' in window){
          websocket = new WebSocket("ws://172.19.35.29:8080/onlinechat/websocket");
      }
      else{
          alert('Not support websocket')
      }
       
      //connection error
      websocket.onerror = function(){
          setMessageInnerHTML("error");
      };
       
      //connection ok
      websocket.onopen = function(){
          setMessageInnerHTML("open");
      }
       
      //receive message
      websocket.onmessage = function(event){
          setMessageInnerHTML(event.data);
      }
       
      //close
      websocket.onclose = function(){
          setMessageInnerHTML("close");
      }
       
      //listen windown, when force close window, close websocket connection
      window.onbeforeunload = function(){
          websocket.close();
      }
       
      //show message on page
      function setMessageInnerHTML(innerHTML){
          document.getElementById('message').innerHTML += innerHTML + '<br/>';
      }
       
      //close connection
      function closeWebSocket(){
          websocket.close();
      }
       
      //send message
      function send(){
          var message = document.getElementById('text').value;
          websocket.send(message);
      }
  </script>
</html>