package com.online;

import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.codehaus.jackson.map.ObjectMapper;

//http://www.myexception.cn/web/1997640.html
//该注解用来指定一个URI,客户端可以通过这个URI来连接到WebSocket,类似Servlet的注解mapping,无需在web.xml中配置.
//{name}用来传递当前加入者的名称,RESTful
@ServerEndpoint(value = "/websocket/{username}", configurator = GetHttpSessionConfigurator.class)
public class WebSocketEndpoint {

	private static final Set<WebSocketEndpoint> onlineUsers = new CopyOnWriteArraySet<WebSocketEndpoint>(); // 静态变量
																											// 存储在线用户
	private Session session;
	private HttpSession httpSession;
	private String username;
	
	@OnOpen
	public void onOpen(Session session, EndpointConfig config)
			throws IOException {
		this.httpSession = (HttpSession) config.getUserProperties().get(
				HttpSession.class.getName());
		this.username = (String) httpSession.getAttribute("name");
		// @PathParam("username") String username
		this.setSession(session);
		onlineUsers.add(this); // 将用户信息添加到在线用户序列中
		EventBean systemEvent  = new EventBean("SYSTEM", "SYSTEM_NOTICE");
		systemEvent.setMessage(this.username + " 上线了");
		String packageMessage = new ObjectMapper().writeValueAsString(systemEvent);
		// 将上线消息发送给所有在线用户
		sendMessageToAllOnline(packageMessage, true);
	}
	
	private void sendMessageToAllOnline(String message) throws IOException{
		sendMessageToAllOnline(message, false);
	}
	private void sendMessageToAllOnline(String message, boolean isFirstLogin)
			throws IOException {
		System.out.println("当前用户个数"+onlineUsers.size());
		for (WebSocketEndpoint member : onlineUsers) {
			Session sessionX = member.getSession();
			//isFirstLogin, no need send to self
			if((isFirstLogin && sessionX != this.session) || !isFirstLogin){
				sessionX.getBasicRemote().sendText(message);
			}
		}
	}

	@OnMessage
	public void onMessage(String message) throws IOException {
		// 将客户端传来的消息发送给所有在线用户
		//System.out.println("收到客户发送信息"+this.username + "|" + message);
		EventBean receiveEvent = new ObjectMapper().readValue(message, EventBean.class);
		System.out.println("packageMessage1");
		//set user from session
		receiveEvent.setFrom(this.username);
		System.out.println("packageMessage2");
		//set user from session
		receiveEvent.setReceiveTime(new Date());
		System.out.println("packageMessage3");
		String packageMessage = new ObjectMapper().writeValueAsString(receiveEvent);
		System.out.println("packageMessage4");
		sendMessageToAllOnline(packageMessage);
		// 在控制台打印当前在线用户
		/*for (WebSocketEndpoint webSocketServer : onlineUsers) {
			System.out.print(webSocketServer.username + "  ");
		}*/
	}

	@OnClose
	public void onClose(CloseReason reason) throws IOException {
		EventBean systemEvent  = new EventBean("SYSTEM", "SYSTEM_NOTICE");
		systemEvent.setMessage(this.username + " 下线了");
		onlineUsers.remove(this);
		String packageMessage = new ObjectMapper().writeValueAsString(systemEvent);
		// 将下线消息发送给所有在线用户
		sendMessageToAllOnline(packageMessage);
	}

	@OnError
	public void onError(Session session, Throwable t) {
		t.printStackTrace();
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}