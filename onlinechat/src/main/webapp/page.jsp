<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://"
			+ request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
	String name = request.getParameter("name");
	request.getSession().setAttribute("name","Session_"+name);
%>

<!DOCTYPE HTML>
<html>
<head>
<base href="<%=basePath%>">
<title>My WebSocket</title>

</head>
<body>
	Welcome
	<br />
	<input id="text" type="text" />
	<button onclick="sendMessage()">Send</button>
	<button onclick="closeWebSocket()">Close</button>
	<button onclick="reConnect()">re-connect</button>
	<div id="message"></div>
</body>

<script type="text/javascript">
	var username = '<%=name%>';
	var wsUri = 'ws://localhost:8080/onlinechat/websocket/' + username;
	var output;

	function init() {
		output = document.getElementById("message");
		initWebSocket();
	}
	
	function reConnect () {
		if(websocket.readyState == WebSocket.CLOSED){
			console.log("Re-connect to Server");
			init();
		}else{
			writeToScreen("Alrady connect, please not re-connect");
		}
	}

	function initWebSocket() {
		if ('WebSocket' in window) {
			websocket = new WebSocket(wsUri);
			
			writeToScreen("You have connectted to server, welcome");
			//attach event handlers
			websocket.onopen = onOpen;
			websocket.onclose = onClose;
			websocket.onmessage = onMessage;
			websocket.onerror = onError;
			
			window.onbeforeunload = function(event){
				websocket.close();
		    };
		} else {
			alert("WebSockets not supported on your browser.");
		}
	}

	function onOpen(evt) {
		//called as soon as a connection is opened
		if(websocket.readyState == WebSocket.OPEN){
			console.log("Connect to server");
		}
	}
	function onClose(evt) {
		//called when connection is closed
		writeToScreen("DISCONNECTED");
	}
	function onMessage(evt) {
		//called on receipt of message
		writeToScreen(evt.data);
	}
	function onError(evt) {
		//called on error
		writeToScreen("ERROR:" + evt.data);
	}

	function sendMessage() {
		var msg = document.getElementById('text').value;
		websocket.send("{\"message\": \"" + msg + "\",\"type\":\"USER\"}");
	}
	
	function writeToScreen(message) {
		var pre = document.createElement("p");
		pre.style.wordWrap = "break-word";
		pre.innerHTML = message;
		output.appendChild(pre);
	}
	
	function closeWebSocket(){
		websocket.close(1000,"normaly close");
	}
	
	window.addEventListener("load", init, false);
	
	Date.prototype.format = function(format) {
	    var o = {
	        "M+": this.getMonth() + 1,
	        // month
	        "d+": this.getDate(),
	        // day
	        "h+": this.getHours(),
	        // hour
	        "m+": this.getMinutes(),
	        // minute
	        "s+": this.getSeconds(),
	        // second
	        "q+": Math.floor((this.getMonth() + 3) / 3),
	        // quarter
	        "S": this.getMilliseconds()
	        // millisecond
	    };
	    if (/(y+)/.test(format) || /(Y+)/.test(format)) {
	        format = format.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
	    }
	    for (var k in o) {
	        if (new RegExp("(" + k + ")").test(format)) {
	            format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k] : ("00" + o[k]).substr(("" + o[k]).length));
	        }
	    }
	    return format;
	};
	
	function timestampformat(timestamp) {
	    return (new Date(timestamp)).format("yyyy-MM-dd hh:mm:ss");
	} 
</script>
<script type="text/javascript" src="<%=basePath %>jquery-1.8.0.js" />
</html>