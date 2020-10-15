package client;

import java.util.Vector;

public class app {
	
	public static int localPort = 6055;				//client port number
	public static int destPort = 6066;				//server port number
	public static String serverIP = "localhost";	//server ip address
	
	public static SocketListener socket;

	public static void main(String[] args) {

		//start socket listener thread
		socket = new SocketListener(serverIP, localPort, destPort);
		socket.start();
		
		socket.sendString("hello world", 0);	//send message, with op 0
		
		Vector<User> listOfUsers = new Vector<User>();
		User user1 = new User("Snoop Dog", "122,122,133,12", 3333);
		User user2 = new User("Dr Dre", "111,222,333,444", 5555);
		user1.register("Soccer");
		user1.register("Football");
		user2.register("Parties");
		listOfUsers.add(user1);
		listOfUsers.add(user2);
		user1.displayUserInformation();
		user2.displayUserInformation();
		
	}
	
	public static void display(String in) {
		//hopefully print to a GUI one day, console for now
		System.out.println(in);
	}
	
}
