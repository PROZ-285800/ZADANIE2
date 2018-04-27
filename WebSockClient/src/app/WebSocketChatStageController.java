package app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

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
import javafx.scene.text.Text;
import javafx.stage.FileChooser;



public class WebSocketChatStageController {
	
		@FXML TextField userTextField;
		@FXML TextArea chatTextArea;
		@FXML TextField messageTextField;
		@FXML Button btnSet;
		@FXML Button btnSend;
		@FXML Button btnUpload;
		@FXML Text attachmentInfo;
		@FXML Button btnRemove;
		@FXML private ListView<String> attachments;
		private List<byte[]> attachmentContents;
		private String user;
		private WebSocketClient webSocketClient;
		private byte[] fileContents;
		private String fileName;
		private int counter;
		

		@FXML private void initialize() {
			attachmentContents = new ArrayList<>();
			webSocketClient = new WebSocketClient();
			user = userTextField.getText();
			btnSend.setOnAction(value -> btnSend_Click());
			btnUpload.setOnAction(value -> btnUpload_Click());
			btnRemove.setVisible(false);
			attachmentInfo.setVisible(false);
			counter = 0;
		}
		
		@FXML private void btnUpload_Click() {
			FileChooser fileChooser = new FileChooser();
			File selectedFile = fileChooser.showOpenDialog(null);
			
			if (selectedFile != null) {
				fileName = selectedFile.getName();
				attachmentInfo.setText("Uploaded file: " + fileName);
				attachmentInfo.setVisible(true);
				btnRemove.setVisible(true);
				
				try {
					fileContents = Files.readAllBytes(selectedFile.toPath());
				} catch (Exception e) {
					e.printStackTrace();
				}
			
			} else {
				System.out.println("File was not selected");
			}
			
		}
		
		@FXML private void btnSet_Click() {
			user = userTextField.getText();
		}
		
		@FXML private void btnSend_Click() {
			webSocketClient.sendMessage(messageTextField.getText(), fileContents);
			attachmentInfo.setVisible(false);
			attachmentInfo.setText(null);
			btnRemove.setVisible(false);
			fileContents = null;
			fileName = null;
			
		}
		
		@FXML private void btnRemove_Click() {
			attachmentInfo.setText("");
			btnRemove.setVisible(false);
		}
		
		@FXML private void downloadContent(MouseEvent e) {
			byte [] content = attachmentContents.get(attachments.getSelectionModel().getSelectedIndex());
			FileChooser fileChooser = new FileChooser(); 
			File file = fileChooser.showSaveDialog(null);
			try {
				if(file != null) {
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(content);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
		}
		
		public void closeSession(CloseReason closeReason) {
		try {
			webSocketClient.session.close(closeReason);
		}
			catch (IOException e) { e.printStackTrace(); }
		}
		
		
		
		public byte[] getFileContents() {
			return fileContents;
		}

		public void setFileContents(byte[] fileContents) {
			this.fileContents = fileContents;
		}

		
		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}



		@ClientEndpoint
		public class WebSocketClient {
			
			private Session session;
			
			public WebSocketClient() { connectToWebSocket(); }
			
			
			@OnOpen public void onOpen(Session session) {
				System.out.println("Connection is opened.");
				this.session = session;
			}
			
			@OnClose public void onClose(CloseReason closeReason) {
				System.out.println("Connection is closed: " + closeReason.getReasonPhrase());
			}
			
			
			@OnError public void onError(Throwable throwable) {
				System.out.println("Error occured");
				throwable.printStackTrace();
			}
			
			@OnMessage public void onMessage(String message, Session session) {
				System.out.println("Message was received");
				chatTextArea.setText(chatTextArea.getText() + message + "\n");
			}
			
			@OnMessage public void onMessage(ByteBuffer data, Session session) {
				System.out.println("File was reveived");
				attachmentContents.add(data.array());
				counter++;
				Platform.runLater(new Runnable() {
				    @Override
				    public void run() {
				    		attachments.getItems().add("Attachment"+ Integer.toString(counter));	
				    }
				});
				
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
				
			public void sendMessage(String message, byte[] data) {
				String extraData = ""; 
				if (data!= null) {
					extraData = " (sent file) ";
				}
				try {
						System.out.println("Message was sent: " + message);
						if (user != null && !user.equals("")) {
							session.getBasicRemote().sendText(user + ": " + message + extraData);
						} else {
							session.getBasicRemote().sendText("annonymous: " + message + extraData);
						}
						
					} catch (IOException ex) {
						
						ex.printStackTrace();
					}	
				
				if(data != null) {
					ByteBuffer byteBuffer = ByteBuffer.wrap(data);
					try {
						session.getBasicRemote().sendBinary(byteBuffer);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
}
