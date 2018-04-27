package server;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;


	 
@ApplicationScoped
@ServerEndpoint("/websocketendpoint")
public class WebSocketEndpoint {
	
	@OnOpen
	public void onOpen(Session session) { 
		System.out.println("Connection opened");
	}
	
	@OnClose
	public void onClose(Session session) {
		System.out.println("Connection closed");
	}
	
	@OnError
	public void onError(Throwable error) { }
	
	
	// text data
	@OnMessage
	public void onMessage(String message, Session session) {
		try {
			for (Session oneSession : session.getOpenSessions()) {
				if (oneSession.isOpen()) {
					oneSession.getBasicRemote().sendText(message);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// binary data 
	@OnMessage 
	public void onMessage(ByteBuffer data, Session session) {
		try {
			for (Session oneSession : session.getOpenSessions()) {
				if (oneSession.isOpen()) {
					oneSession.getBasicRemote().sendBinary(data);
				}
			} 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
