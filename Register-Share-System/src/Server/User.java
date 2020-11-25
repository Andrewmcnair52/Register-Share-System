package Server;

public class User {
	
	private String name;
	private String ip;
	private int socket;
	
	public User(String name, String ip, int socket) {
		this.name = name;
		this.ip = ip;
		this.socket = socket;
	}
	
	public User() {
		
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getSocket() {
		return socket;
	}
	public void setSocket(int socket) {
		this.socket = socket;
	}
	
	

}