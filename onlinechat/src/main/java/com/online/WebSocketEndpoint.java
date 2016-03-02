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
//该注解用来指定一个URI,客户端可以通过这个URI来连接到WebSocket,类似Servlet的注解mapping,无需在web.xml中配置.
//{name}用来传递当前加入者的名称,RESTful
@ServerEndpoint(value = "/websocket/{msgTo}", configurator = GetHttpSessionConfigurator.class)
public class WebSocketEndpoint {

	//private static final Set<WebSocketEndpoint> onlineUsers = new CopyOnWriteArraySet<WebSocketEndpoint>(); // 静态变量
	//静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
	//存储 memberID 和有联系的对象ID List
	//private static final ConcurrentHashMap<String, CopyOnWriteArraySet<String>> conversationMap = new ConcurrentHashMap<String, CopyOnWriteArraySet<String>>();
	//存储 memberID 和对象
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
		//绑定session到当前对象
		this.session = session;
		//从HTTPSession获取参数
		this.httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
		this.from = (String) httpSession.getAttribute("LOGIN_USER");
		this.to = msgTo;
		//将用户信息添加到在线用户序列中
		memberSessionMap.put(this.from +"||" +this.to, this);
		//绑定关系
		//mappingConversation(this.from, this.to);
		//更新在线人数
		addOnlineCount();
		//创建上线广播消息
		MessageEvent event  = new MessageEvent("SYSTEM", this.from, "LOGIN_WELCOME");
		event.setContent("你已经成功进入聊天室!");
		String packagedMsg = new ObjectMapper().writeValueAsString(event);
		//开始广播消息
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
		//调试信息
		System.out.println("#################### 调试信息开始 #################");
		System.out.println("####当前所有session总数:" + WebSocketEndpoint.onlineCount);
		System.out.println("####当前所有session信息:");
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
		System.out.println("#################### 调试信息结束 #################");
		
		//不是登录信息
		if(!isFirstLogin){
			//to是ALL,系统消息.广播给所有人(可以在页面验证权限信息)
			if("ALL".equals(to)){
				for(ConcurrentHashMap.Entry<String, WebSocketEndpoint> member: memberSessionMap.entrySet()){
					Session sessionX = member.getValue().getSession();
					if((isFirstLogin && sessionX != this.session) || !isFirstLogin){
						sessionX.getBasicRemote().sendText(message);
					}
				}
			//to不时ALL,属于私聊
			}else{
				//to已经登录,广播消息给to和from
				if(memberSessionMap.get(to + "||" + from) != null){
					//广播给to
					WebSocketEndpoint wse = memberSessionMap.get(to + "||" + from);
					Session sessionX = wse.getSession();
					sessionX.getBasicRemote().sendText(message);
					//广播给from
					if(from != null){
						WebSocketEndpoint wseF = memberSessionMap.get(from + "||" + to);
						wseF.getSession().getBasicRemote().sendText(message);
					}
				//to没有登录,广播给from,告诉他to未登录的消息
				}else{
					//当前用户退出,to也已经掉线或者不可达
					if(from != null){
						//创建用户不在线通知
						MessageEvent event  = new MessageEvent("SYSTEM", this.from, "MEMBER_NOT_LOGIN");
						event.setContent("["+ new Date() +"]."+ this.to +" 当前不在线,会在上线后推送.");
						String packagedMsg = new ObjectMapper().writeValueAsString(event);
						//广播给自己
						WebSocketEndpoint wseF = memberSessionMap.get(from + "||" + to);
						wseF.getSession().getBasicRemote().sendText(packagedMsg);
					}
				}
			}
		//是登录信息
		}else{
			//用户登录, 只广播给自己的欢迎信息
			this.getSession().getBasicRemote().sendText(message);
		}
	}

	@OnMessage
	public void onMessage(String jsonMessage) throws IOException {
		System.out.println("收到客户发送信息:" + jsonMessage);
		//JSON消息转换为MessageEvent对象
		MessageEvent event = new ObjectMapper().readValue(jsonMessage, MessageEvent.class);
		//根据当前连接session,获取发送人和收件人
		event.setFrom(this.from);
		event.setTo(this.to);
		event.setSendDate(new Date());
		String packageMessage = new ObjectMapper().writeValueAsString(event);
		//广播消息
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
		//更新在线人数
		subOnlineCount();
		//创建用户离线通知
		MessageEvent event  = new MessageEvent("SYSTEM", this.to, "MEMBER_OFFLINE");
		event.setContent("["+ new Date() +"]."+ this.from +" 已经下线.");
		String packagedMsg = new ObjectMapper().writeValueAsString(event);
		//将下线消息发送给对话的人
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
    	System.out.println("当前用户个数" + WebSocketEndpoint.getOnlineCount());
    }
     
    public static synchronized void subOnlineCount() {
    	WebSocketEndpoint.onlineCount--;
    	System.out.println("当前用户个数" + WebSocketEndpoint.getOnlineCount());
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