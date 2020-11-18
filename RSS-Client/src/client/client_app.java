package client;

public class client_app {
	
	public static int localPort = 6055;				//client port number
	
	public static int server1Port = 6077;			//server1 port number
	public static int server2Port = 6066;			//server2 port number
	public static String server1IP = "localhost";	//server1 ip address
	public static String server2IP = "localhost";	//server2 ip address
	
	public static SocketListener socket;

	public static void main(String[] args) {

		//start socket listener thread
		socket = new SocketListener(server1IP, server2IP, server1Port, server2Port, localPort);
		socket.start();
		
		//socket.sendString("hello world", 0, 1);	//send message, with op 0
		
		
		
		//tests for reg and dereg
		
		String rr = socket.formatRegisterReq("test", "localhost", 6055);
		
		//send reg to both servers
		socket.sendString(rr, 1, 1);
		socket.sendString(rr, 1, 2);
		
		//test a deregistration
		
		//String dr = socket.formatDeregisterReq("test");
		//socket.sendString(dr, 2, 1);
		
		
		
		
		String mr = socket.formatPublishReq("test", "Books", "Books are so dope!");
		socket.sendString(mr, 11, 1);
		
	}
	
	public static void display(String in) {
		//hopefully print to a GUI one day, console for now
		System.out.println(in);
	}
	
}
