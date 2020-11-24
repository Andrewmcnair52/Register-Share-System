package Server;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;


public class FileManager {
	//needs to have a file, and methods to add to that file
	
	File file;
	File log;
	
	BufferedWriter bw;
	
	ArrayList<String> logList = new ArrayList<>();
	
	public FileManager(String fileName) {
		this.file = new File(fileName);
		
		LocalDateTime dt = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss");
		//String logName = dt.format(format);
		String logName = "log";
		this.log = new File(logName + ".txt");
		
		try {
			file.createNewFile();
			log.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void printLastLogs(int num) {
		for (int i = 1; i <= num; i++) {
			try {
				System.out.println(logList.get(logList.size() - i));
			}catch (IndexOutOfBoundsException e) {
				System.out.println("End of List");
				return;
			}
		}
	}
	
	
	public void updateUserList(List<User> userList) {
		
		ObjectMapper om = new ObjectMapper();
		
		try {
			om.writeValue(file, userList);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//public void deleteUser(String name) {}


	public ArrayList<User> loadUserList() {
		
		ObjectMapper om = new ObjectMapper();
		
		ArrayList<User> list = new ArrayList<>();
		String sList = new String();
	
		
		try {
			sList = Files.readString(file.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			list = om.readValue(sList, new TypeReference<ArrayList<User>>() {});
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return list;
	}
	
	public void log(String message) {
		//simple log a message user wants
		
		logList.add(message);
		
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
		String sMes = new String(Arrays.copyOfRange(buffer, 1, buffer.length));
		
		logList.add(java.time.LocalTime.now() + " " + message + " : " + sMes);
		
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(log, true));
			output.write(java.time.LocalTime.now() + " " + message + " : " + sMes + System.getProperty("line.separator"));
			output.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void log(String message, String sMes) {
		
		logList.add(java.time.LocalTime.now() + " " + message + " : " + sMes);
		
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(log, true));
			output.write(java.time.LocalTime.now() + " " + message + " : " + sMes + System.getProperty("line.separator"));
			output.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
	
	
