package server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	
	private Logger logger = Logger.getLogger("websocketEndPointLogger");

	@OnOpen
	public void onOpen(Session session) {
		logger.log(Level.INFO, "Session " + session.getId().toString() + " : is Open" );
	}

	@OnClose
	public void onClose(Session session) {
		logger.log(Level.INFO, "Session " + session.getId().toString() + " : is Cloased");
	}

	@OnError
	public void onError(Throwable error) {
	}

	@OnMessage
	public void onMessage(ByteBuffer buf, boolean last, Session session) {
		System.out.println("File. Buffer capacity: " + buf.capacity());
		try {
			for (Session oneSession : session.getOpenSessions()) {
				if (oneSession.isOpen()) {
					oneSession.getBasicRemote().sendBinary(buf, last);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
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

}
