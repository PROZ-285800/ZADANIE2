package app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.WebSocketContainer;


import javax.websocket.Session;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

public class WebSocketChatStageController {

	@FXML
	TextField userTextField;
	@FXML
	TextArea chatTextArea;
	@FXML
	TextField messageTextField;
	@FXML
	Button btnSet;
	@FXML
	Button btnSend;
	@FXML
	Button btnUpload;

	// FileName , FileContents
	HashMap<String, FileOutputStream> map = new HashMap<>();
	
	private ReentrantLock lock = new ReentrantLock();

	@FXML
	private ListView<String> fileNames;

	private String user = "guest";
	private WebSocketClient webSocketClient;
	private String hash;

	@FXML
	private void initialize() {
		webSocketClient = new WebSocketClient();
		hash = Integer.toString(webSocketClient.getSession().hashCode());
		(new File(hash + File.separator)).mkdirs();
	}

	@FXML
	private void btnUpload_Click() {
		File selectedFile = (new FileChooser()).showOpenDialog(null);
		if (selectedFile != null) {
			Thread thread = new Thread(new UploadTask(webSocketClient, selectedFile, lock));
			thread.setDaemon(true);
			thread.start();
			
		}
	}

	@FXML
	private void btnSet_Click() {
		user = userTextField.getText();
	}

	@FXML
	private void btnSend_Click() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				webSocketClient.sendMessage(user + ": " + messageTextField.getText());
			}
		}).start();
	}

	@FXML
	private void downloadContent(MouseEvent e) {
		if(fileNames.getSelectionModel().getSelectedIndex() < 0)  {
			return;
		}
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialFileName(fileNames.getSelectionModel().getSelectedItem());
		File file = fileChooser.showSaveDialog(null);
		new Thread(new SaveTask
				(fileNames.getSelectionModel().getSelectedItem() , hash, file ))
					.start();
	}

	public void closeSession(CloseReason closeReason) {
		try {
			webSocketClient.session.close(closeReason);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getFileNameFromBinaryMessage(ByteBuffer byteBuffer) {
		String s = new String(Arrays.copyOfRange(byteBuffer.array(), 0, 200),StandardCharsets.UTF_8);
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == 0) {
				s = s.substring(0, i);
				break;
			}
		}
		return s;
	}

	private byte[] getFileContentsFromBinaryMessage(ByteBuffer byteBuffer) {
		return Arrays.copyOfRange(byteBuffer.array(), 200, byteBuffer.remaining());
	}

	@ClientEndpoint
	public class WebSocketClient {

		private Session session;

		public WebSocketClient() {
			connectToWebSocket();
		}

		@OnOpen
		public void onOpen(Session session) {
			System.out.println("Connection is opened.");
			this.session = session;
		}

		@OnClose
		public void onClose(CloseReason closeReason) {
			System.out.println("Connection is closed: " + closeReason.getReasonPhrase());
			
			for (String str : fileNames.getItems() ) {  // delete files
				new File(hash + File.separator + str).delete();
			}
			// delete directory 
			new File(hash + File.separator).delete();
		}

		@OnError
		public void onError(Throwable throwable) {
			System.out.println("Error occured");
			throwable.printStackTrace();
		}

		@OnMessage // text message
		public void onMessage(String message, Session session) {
			// Text message
			if (message.indexOf(':') != -1) {
				System.out.println("Message was received");
				chatTextArea.setText(chatTextArea.getText() + message + "\n");
				// File name
			} else {
				Platform.runLater(new Runnable() {
					
					@Override
					public void run() {
						fileNames.getItems().add(message);
						
					}
				});
			}
			
		}

		@OnMessage
		public void onMessage(ByteBuffer byteBuffer, boolean last, Session session) {
			String fileName = null;
   			if (byteBuffer.remaining() > 0 && byteBuffer.array().length >= 200) {
				fileName = getFileNameFromBinaryMessage(byteBuffer);
				try {
					if( !map.containsKey(fileName) ) {
						map.put(fileName, new FileOutputStream(hash + File.separator + fileName, true));
					}
						byte[] fileContents = getFileContentsFromBinaryMessage(byteBuffer);
						FileOutputStream out = map.get(fileName);
						out.write(fileContents);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				chatTextArea.setText(chatTextArea.getText() + new String(Arrays.copyOfRange(byteBuffer.array(), 0, byteBuffer.array().length),StandardCharsets.UTF_8) + "\n");
			}
		}
		

		private void connectToWebSocket() {
			WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
			try {
				URI uri = URI.create("ws://localhost:8080/WebSocketServer/websocketendpoint");
				webSocketContainer.connectToServer(this, uri);
			} catch (DeploymentException | IOException e) {
				e.printStackTrace();
			}
		}

		public void sendMessage(String message) {
			lock.lock();	
			try {
				session.getBasicRemote().sendText(message);
			} catch (IOException e) {
				e.printStackTrace();
			}		
			lock.unlock();
		}

		public Session getSession() {
			return session;
		}

		public void setSession(Session session) {
			this.session = session;
		}
	
	}
}
