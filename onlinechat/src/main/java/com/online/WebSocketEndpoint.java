package com.online;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.codehaus.jackson.map.ObjectMapper;

//http://www.myexception.cn/web/1997640.html
//��ע������ָ��һ��URI,�ͻ��˿���ͨ�����URI�����ӵ�WebSocket,����Servlet��ע��mapping,������web.xml������.
//{name}�������ݵ�ǰ�����ߵ�����,RESTful
@ServerEndpoint(value = "/websocket/{msgTo}", configurator = GetHttpSessionConfigurator.class)
public class WebSocketEndpoint {

	//private static final Set<WebSocketEndpoint> onlineUsers = new CopyOnWriteArraySet<WebSocketEndpoint>(); // ��̬����
	//��̬������������¼��ǰ������������Ӧ�ð�����Ƴ��̰߳�ȫ�ġ�
    private static int onlineCount = 0;
	//�洢 memberID ������ϵ�Ķ���ID List
	//private static final ConcurrentHashMap<String, CopyOnWriteArraySet<String>> conversationMap = new ConcurrentHashMap<String, CopyOnWriteArraySet<String>>();
	//�洢 memberID �Ͷ���
	private static final ConcurrentHashMap<String, WebSocketEndpoint> memberSessionMap = new ConcurrentHashMap<String, WebSocketEndpoint>();
	private Session session;
	private HttpSession httpSession;
	private String from;
	private String to;
	
	/**
	 * A connection is open
	 *
	 * @param session
	 * @param config
	 * @throws IOException
	 */
	@OnOpen
	public void onOpen(Session session, EndpointConfig config, @PathParam("msgTo") String msgTo) throws IOException {
		//��session����ǰ����
		this.session = session;
		//��HTTPSession��ȡ����
		this.httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
		this.from = (String) httpSession.getAttribute("LOGIN_USER");
		this.to = msgTo;
		//���û���Ϣ��ӵ������û�������
		memberSessionMap.put(this.from +"||" +this.to, this);
		//�󶨹�ϵ
		//mappingConversation(this.from, this.to);
		//������������
		addOnlineCount();
		//�������߹㲥��Ϣ
		MessageEvent event  = new MessageEvent("SYSTEM", this.from, "LOGIN_WELCOME");
		event.setContent("���Ѿ��ɹ�����������!");
		String packagedMsg = new ObjectMapper().writeValueAsString(event);
		//��ʼ�㲥��Ϣ
		broadcastMessage(this.from, this.to, packagedMsg, true);
	}
	/*
	private void mappingConversation(String from, String to){
		//from -> to
		CopyOnWriteArraySet<String> fromConversation = null;
		if(conversationMap.get(from) == null){
			conversationMap.put(from, new CopyOnWriteArraySet<String>());
		}
		fromConversation = conversationMap.get(from);
		fromConversation.add(to);
		//to -> from
		CopyOnWriteArraySet<String> toConversation = null;
		if(conversationMap.get(to) == null){
			conversationMap.put(to, new CopyOnWriteArraySet<String>());
		}
		toConversation = conversationMap.get(to);
		toConversation.add(from);
	}
	*/
	/*private ArrayList<WebSocketEndpoint> searchforSession(WebSocketEndpoint wse){
		ArrayList<WebSocketEndpoint> list = new ArrayList<WebSocketEndpoint>();
		list.add(this);
		WebSocketEndpoint toWse = memberSessinMap.get(wse.getTo());
		if(toWse != null){
			list.add(toWse);
			Session toSession = toWse.getSession();
			ArrayList<WebSocketEndpoint> subList = new ArrayList<WebSocketEndpoint>();
			subList.add(this);
			subList.add(toWse);
			storeMap.put(toSession.getId(), subList);
		}
		return list;
	}*/
	
	private void broadcastMessage(String from, String to, String message) throws IOException{
		broadcastMessage(from, to, message, false);
	}
	private void broadcastMessage(String from, String to, String message, boolean isFirstLogin)
			throws IOException {
		//������Ϣ
		System.out.println("#################### ������Ϣ��ʼ #################");
		System.out.println("####��ǰ����session����:" + WebSocketEndpoint.onlineCount);
		System.out.println("####��ǰ����session��Ϣ:");
		for(ConcurrentHashMap.Entry<String, WebSocketEndpoint> member: memberSessionMap.entrySet()){
			WebSocketEndpoint wse = member.getValue();
			Session sessionX = wse.getSession();
			System.out.print(member.getKey() +" | ");
			System.out.print(member.getValue() +" | ");
			System.out.print(sessionX.getId() +" | ");
			System.out.print(wse.getFrom() +" | ");
			System.out.println(wse.getTo());
		}
		System.out.println("isFirstLogin="+isFirstLogin);
		System.out.println("from="+from);
		System.out.println("to="+to);
		System.out.println("message="+message);
		System.out.println("memberSessionMap.get(to)=" + (memberSessionMap.get(to)==null));
		System.out.println("#################### ������Ϣ���� #################");
		
		//���ǵ�¼��Ϣ
		if(!isFirstLogin){
			//to��ALL,ϵͳ��Ϣ.�㲥��������(������ҳ����֤Ȩ����Ϣ)
			if("ALL".equals(to)){
				for(ConcurrentHashMap.Entry<String, WebSocketEndpoint> member: memberSessionMap.entrySet()){
					Session sessionX = member.getValue().getSession();
					if((isFirstLogin && sessionX != this.session) || !isFirstLogin){
						sessionX.getBasicRemote().sendText(message);
					}
				}
			//to��ʱALL,����˽��
			}else{
				//to�Ѿ���¼,�㲥��Ϣ��to��from
				if(memberSessionMap.get(to + "||" + from) != null){
					//�㲥��to
					WebSocketEndpoint wse = memberSessionMap.get(to + "||" + from);
					Session sessionX = wse.getSession();
					sessionX.getBasicRemote().sendText(message);
					//�㲥��from
					if(from != null){
						WebSocketEndpoint wseF = memberSessionMap.get(from + "||" + to);
						wseF.getSession().getBasicRemote().sendText(message);
					}
				//toû�е�¼,�㲥��from,������toδ��¼����Ϣ
				}else{
					//��ǰ�û��˳�,toҲ�Ѿ����߻��߲��ɴ�
					if(from != null){
						//�����û�������֪ͨ
						MessageEvent event  = new MessageEvent("SYSTEM", this.from, "MEMBER_NOT_LOGIN");
						event.setContent("["+ new Date() +"]."+ this.to +" ��ǰ������,�������ߺ�����.");
						String packagedMsg = new ObjectMapper().writeValueAsString(event);
						//�㲥���Լ�
						WebSocketEndpoint wseF = memberSessionMap.get(from + "||" + to);
						wseF.getSession().getBasicRemote().sendText(packagedMsg);
					}
				}
			}
		//�ǵ�¼��Ϣ
		}else{
			//�û���¼, ֻ�㲥���Լ��Ļ�ӭ��Ϣ
			this.getSession().getBasicRemote().sendText(message);
		}
	}

	@OnMessage
	public void onMessage(String jsonMessage) throws IOException {
		System.out.println("�յ��ͻ�������Ϣ:" + jsonMessage);
		//JSON��Ϣת��ΪMessageEvent����
		MessageEvent event = new ObjectMapper().readValue(jsonMessage, MessageEvent.class);
		//���ݵ�ǰ����session,��ȡ�����˺��ռ���
		event.setFrom(this.from);
		event.setTo(this.to);
		event.setSendDate(new Date());
		String packageMessage = new ObjectMapper().writeValueAsString(event);
		//�㲥��Ϣ
		broadcastMessage(this.from, this.to, packageMessage);
	}

	/**
	 * Close a session
	 *
	 * @param session
	 * @param closeReason
	 */
	@OnClose
	public void onClose(Session session, CloseReason closeReason) throws IOException {
		if(session.isOpen()){
			try{
				session.close();
			} catch(IllegalStateException | IOException ex){
				
			}
		}
		//������������
		subOnlineCount();
		//�����û�����֪ͨ
		MessageEvent event  = new MessageEvent("SYSTEM", this.to, "MEMBER_OFFLINE");
		event.setContent("["+ new Date() +"]."+ this.from +" �Ѿ�����.");
		String packagedMsg = new ObjectMapper().writeValueAsString(event);
		//��������Ϣ���͸��Ի�����
		broadcastMessage(null, this.to, packagedMsg);
	}

	@OnError
	public void onError(Session session, Throwable t) {
		System.out.println("*********************************************************");
		t.printStackTrace();
	}
	
	public static synchronized int getOnlineCount() {
        return onlineCount;
    }
 
    public static synchronized void addOnlineCount() {
    	WebSocketEndpoint.onlineCount++;
    	System.out.println("��ǰ�û�����" + WebSocketEndpoint.getOnlineCount());
    }
     
    public static synchronized void subOnlineCount() {
    	WebSocketEndpoint.onlineCount--;
    	System.out.println("��ǰ�û�����" + WebSocketEndpoint.getOnlineCount());
    }

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}
}