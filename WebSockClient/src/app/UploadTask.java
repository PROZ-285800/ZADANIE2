package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.locks.ReentrantLock;

import app.WebSocketChatStageController.WebSocketClient;

public class UploadTask implements Runnable {
	private WebSocketClient client;
	
	private File selectedFile;
	
	private ReentrantLock lock;
	
	UploadTask(WebSocketClient webSocketClient, File file, ReentrantLock reentrantLock) {
		this.client = webSocketClient;
		selectedFile = file;
		this.lock = reentrantLock;
	}

	@Override
	public void run() {
		int numberOfBytes = 0;
		byte[] finalMessage = new byte[1024];
		try {
			if (selectedFile != null) {
				String fileName = selectedFile.getName();
				System.arraycopy(fileName.getBytes(), 0,  finalMessage, 0, fileName.getBytes().length);
				FileInputStream inputStream = new FileInputStream(selectedFile);
				OutputStream outputStream = client.getSession().getBasicRemote().getSendStream();

				// messages start from
				byte[] sedningBuffer = new byte[824];
			
				while ((numberOfBytes = inputStream.read(sedningBuffer)) > 0) {
					System.arraycopy(sedningBuffer, 0, finalMessage, 200, numberOfBytes);
					lock.lock();
					outputStream.write(finalMessage, 0, 200 + numberOfBytes);
					lock.unlock();
					
				}
				System.out.println("Binary Data from file : " + fileName + " was sent");
				outputStream.close();
				inputStream.close();
				client.sendMessage(fileName);
			} else {

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
