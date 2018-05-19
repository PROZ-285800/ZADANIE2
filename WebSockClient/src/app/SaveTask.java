package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javafx.stage.FileChooser;

public class SaveTask implements Runnable {
	private String fileName; 
	private String hash;
	private File file; 
	
	SaveTask(String fileName,String userHash, File file) {
		this.fileName = fileName;
		this.file = file;
		hash = userHash;
	}
	
	@Override
	public void run () {
		if (file != null) {
			try {
				InputStream inputStream = new FileInputStream(new File(hash + File.separator + fileName));
				OutputStream outputStream = new FileOutputStream(file);
				byte [] bufferArray = new byte[1000];
				int numberOfBytesRead = 0;
				while ((numberOfBytesRead = inputStream.read(bufferArray) ) > 0) {
					outputStream.write(bufferArray, 0, numberOfBytesRead);
				}
				outputStream.close();
				inputStream.close();
				
			}catch (Exception e) {
				e.printStackTrace();		
			}
		}
	}
}
