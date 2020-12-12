package client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class ClientFileManager {
	//needs to have a file, and methods to add to that file
	
	File log;
	
	FileWriter fw;
	BufferedWriter bw;
	PrintWriter pw;
	
	public ClientFileManager() {
		
		this.log = new File("clientLog.txt");
		
		try {
			log.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void log(String message) {
		//simple log a message user wants
		
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(log, true));
			output.write(message + System.getProperty("line.separator"));
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void log(String message, byte[] buffer) {
		
		int realLen = buffer.length - 1;
		while(buffer[realLen] == 0) {
			realLen--;
		}
		
		String sMes = new String(Arrays.copyOfRange(buffer, 1, realLen + 1));
		
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(log, true));
			output.write(java.time.LocalTime.now() + " " + message + ": " + sMes + System.getProperty("line.separator"));
			output.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}	
}