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
//��ע������ָ��һ��URI,�ͻ��˿���ͨ�����URI�����ӵ�WebSocket,����Servlet��ע��mapping,������web.xml������.
//{name}�������ݵ�ǰ�����ߵ�����,RESTful
@ServerEndpoint(value = "/websocket/{username}", configurator = GetHttpSessionConfigurator.class)
public class WebSocketEndpoint {

	private static final Set<WebSocketEndpoint> onlineUsers = new CopyOnWriteArraySet<WebSocketEndpoint>(); // ��̬����
																											// �洢�����û�
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
		onlineUsers.add(this); // ���û���Ϣ��ӵ������û�������
		EventBean systemEvent  = new EventBean("SYSTEM", "SYSTEM_NOTICE");
		systemEvent.setMessage(this.username + " ������");
		String packageMessage = new ObjectMapper().writeValueAsString(systemEvent);
		// ��������Ϣ���͸����������û�
		sendMessageToAllOnline(packageMessage, true);
	}
	
	private void sendMessageToAllOnline(String message) throws IOException{
		sendMessageToAllOnline(message, false);
	}
	private void sendMessageToAllOnline(String message, boolean isFirstLogin)
			throws IOException {
		System.out.println("��ǰ�û�����"+onlineUsers.size());
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
		// ���ͻ��˴�������Ϣ���͸����������û�
		//System.out.println("�յ��ͻ�������Ϣ"+this.username + "|" + message);
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
		// �ڿ���̨��ӡ��ǰ�����û�
		/*for (WebSocketEndpoint webSocketServer : onlineUsers) {
			System.out.print(webSocketServer.username + "  ");
		}*/
	}

	@OnClose
	public void onClose(CloseReason reason) throws IOException {
		EventBean systemEvent  = new EventBean("SYSTEM", "SYSTEM_NOTICE");
		systemEvent.setMessage(this.username + " ������");
		onlineUsers.remove(this);
		String packageMessage = new ObjectMapper().writeValueAsString(systemEvent);
		// ��������Ϣ���͸����������û�
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