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
    <input id="text" type="text" /><button onclick="send()">Send</button>    <button onclick="closeWebSocket()">Close</button>
    <div id="message">
    </div>
  </body>
   
  <script type="text/javascript">
      var websocket = null;
       
      //�жϵ�ǰ������Ƿ�֧��WebSocket
      if('WebSocket' in window){
          websocket = new WebSocket("ws://localhost:8080/MyWebSocket/websocket");
      }
      else{
          alert('Not support websocket')
      }
       
      //���ӷ�������Ļص�����
      websocket.onerror = function(event){
          setMessageInnerHTML("error");
      };
       
      //���ӳɹ������Ļص�����
      websocket.onopen = function(event){
          setMessageInnerHTML("open");
      }
       
      //���յ���Ϣ�Ļص�����
      websocket.onmessage = function(event){
          setMessageInnerHTML(event.data);
      }
       
      //���ӹرյĻص�����
      websocket.onclose = function(event){
          setMessageInnerHTML("close");
      }
       
      //�������ڹر��¼��������ڹر�ʱ������ȥ�ر�websocket���ӣ���ֹ���ӻ�û�Ͽ��͹رմ��ڣ�server�˻����쳣��
      window.onbeforeunload = function(event){
          websocket.close();
      }
       
      //����Ϣ��ʾ����ҳ��
      function setMessageInnerHTML(innerHTML){
          document.getElementById('message').innerHTML += innerHTML + '<br/>';
      }
       
      //�ر�����
      function closeWebSocket(event){
          websocket.close();
      }
       
      //������Ϣ
      function send(){
          var message = document.getElementById('text').value;
          websocket.send(message);
      }
  </script>
</html>