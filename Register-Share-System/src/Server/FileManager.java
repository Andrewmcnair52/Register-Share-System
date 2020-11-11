package Server;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;


public class FileManager {
	//needs to have a file, and methods to add to that file
	
	File file;
	
	public FileManager(String fileName) {
		this.file = new File(fileName);
		
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	//need to implement appending to files
//	public void saveUser(User user) {
//		
//		ObjectMapper om = new ObjectMapper();
//		
//		try {
//			om.writeValue(file, user);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
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
		
}
	
	
