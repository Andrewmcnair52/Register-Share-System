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
	
	File userListFile;
	File log;
	
	BufferedWriter bw;
	
	ArrayList<String> logList = new ArrayList<>();
	
	public FileManager(int serverNum) {
		userListFile = new File("userlist_server"+serverNum+".json");
		log = new File("log_server"+serverNum+".txt");
		
		try {
			if(!userListFile.createNewFile()) loadUserList();
			else updateUserList(new ArrayList<>());
			log.createNewFile();
		} catch (IOException e) { e.printStackTrace(); }
		
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
			om.writeValue(userListFile, userList);
			
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
			sList = Files.readString(userListFile.toPath());
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
		
		logList.add("[" + java.time.LocalTime.now() + "] " + message + " : " + sMes);
		
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