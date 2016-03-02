package com.online;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * @author du.chen
 *
 */
public class GetHttpSessionConfigurator extends
		ServerEndpointConfig.Configurator {

	/**
	 * Set HttpSession information from open websocket
	 *
	 * @param ServerEndpointConfig config
	 * @param HandshakeRequest request
	 * @param HandshakeResponse response
	 */
	@Override
	public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
		HttpSession httpSession = (HttpSession) request.getHttpSession();
		config.getUserProperties().put(HttpSession.class.getName(), httpSession);
		//super.modifyHandshake(config, request, response);
	}
}
