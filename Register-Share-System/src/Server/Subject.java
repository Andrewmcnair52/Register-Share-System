package Server;

import java.util.ArrayList;

public class Subject {
	
	String name;
	ArrayList<String> users;
	
	
	public Subject(String name, ArrayList<String> users) {
		this.name = name;
		this.users = users;
	}
	
	public Subject(String name) {
		this.name = name;
		this.users = new ArrayList<String>();
	}
	public void setName(String n) {
		this.name = n;
	}
	public String getName() {
		return name;
	}
	
	public ArrayList<String> getUsers() {
		return users;
	}
	
	public void addUser(String name) {
		this.users.add(name);
	}
	
	public void removeUser(String name) {
		for(int i = 0; i < users.size(); i++) {
			if(name.equals(users.get(i))) {
				users.remove(i);
				return;
			}
		}
		
	}
	

	

}