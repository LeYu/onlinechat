<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path;
	//测试用,后续应该从LOGIN_SERVLET存取,在这个地方读取
	request.getSession().setAttribute("LOGIN_USER",request.getParameter("from"));	//test 上线请删除
	String msgFrom = (String)request.getSession().getAttribute("LOGIN_USER");
	//从URL获取消息接收人ID
	String msgTo = request.getParameter("to");
	Set<String> tos = null;
	//判断Session是否存在历史会话,可以从DB中读取历史记录
	if(request.getSession().getAttribute("LOGIN_USER_CONVERSATION") != null){
		//加入当前用户到
		tos = (java.util.HashSet<String>)request.getSession().getAttribute("LOGIN_USER_CONVERSATION");
	} else {
		tos = new java.util.HashSet<String>();
	}
	tos.add(msgTo);
	request.getSession().setAttribute("LOGIN_USER_CONVERSATION",tos);
%>
<!DOCTYPE HTML>
<html>
<head>
<base href="<%=basePath%>">
<meta http-equiv="Content-Type" content="text/html;" charset="UTF-8">
<title>Online Chat @WebSocket</title>
</head>
<body>
<div class="container">
	<input type="text" id="text" class="form-control" placeholder="Please input message here">
	<button type="button" class="btn btn-sm btn-primary" onclick="sendMessage()">Send</button>
	<button type="button" class="btn btn-sm btn-danger" onclick="closeWebSocket()">Close</button>
	<button type="button" class="btn btn-sm btn-warning" onclick="reConnect()">Re-connect</button>
	<!-- <h4 id="message"></h4> -->
</div>

</div>
	
<div data-role="content" class="container" role="main">
	<ul class="content-reply-box mg10" id="message">
	
	</ul>
</div>

</body>

<script type="text/javascript">
	var msgTo = '<%=msgTo%>';
	var wsUri = 'ws://localhost:8080/onlinechat/websocket/' + msgTo;
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
		window.WebSocket = window.WebSocket || window.MozWebSocket;
		//if ('WebSocket' in window || 'MozWebSocket' in window)
		if (window.WebSocket) {
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
			return;
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
		writeToScreen(JSON.parse(evt.data));
	}
	function onError(evt) {
		//called on error
		writeToScreen("ERROR:" + evt.data);
	}

	function sendMessage() {
		var msg = {
				"content"	:	document.getElementById('text').value
		};
		websocket.send(JSON.stringify(msg));
	}
	
	function writeToScreen(message) {
		//<span class="label label-primary">Primary</span>
		var pre = document.createElement("li");
		//pre.style.wordWrap = "break-word";
		//pre.className = "label label-info";
		var img = document.createElement("img");
		var span = document.createElement("span");
		span.className = "user-name";
		if(message.from != '<%=msgFrom%>'){
			pre.className = "odd";
			img.className = "img-responsive avatar_";
			img.alt = "";
			img.src = "<%=basePath%>/resources/images/"+message.from+".png";
			span.innerHTML = message.from;
		}else{
			pre.className = "even";
			img.className = "img-responsive avatar_";
			img.alt = "";
			img.src = "<%=basePath%>/resources/images/"+message.to+".png";
			span.innerHTML = message.to;
		}
		var a = document.createElement("a");
		a.className = "user";
		a.href = "javascript:void(0);";
		a.appendChild(img);
		a.appendChild(span);
		pre.appendChild(a);
		var div = document.createElement("div");
		div.className = "reply-content-box";
		var spanS = document.createElement("span");
		spanS.className = "reply-time";
		spanS.innerHTML = "";
		div.appendChild(spanS);
		var divx = document.createElement("div");
		divx.className = "reply-content pr";
		
		var spanx = document.createElement("span");
		spanx.className = "arrow";
		spanx.innerHTML = "&nbsp";
		divx.appendChild(spanx);
		
		var content = document.createElement("span");
		if(message instanceof Object){
			//pre.innerHTML = JSON.stringify(message);
			content.innerHTML += timestampformat(message.sendDate) +" "; 
			content.innerHTML += message.from + " " ;
			content.innerHTML += "@" + message.to + " :" ;
			content.innerHTML += message.content;
		}else {
			content.innerHTML = message;
		}
		divx.appendChild(content);
		div.appendChild(divx);
		pre.appendChild(div);
		
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
<style>
.container {
    width: 600px;
}
</style>
<link rel="stylesheet" href="<%=basePath%>/resources/css/bootstrap.min.css" type="text/css">

<link rel="stylesheet" href="<%=basePath%>/resources/css/style.css" type="text/css">
<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
<script src="<%=basePath%>/resources/js/jquery-1.12.1.js" ></script>
<!-- Include all compiled plugins (below), or include individual files as needed -->
<script src="<%=basePath%>/resources/js/bootstrap.min.js" ></script>
</html>