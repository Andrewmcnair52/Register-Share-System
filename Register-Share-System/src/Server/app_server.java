package Server;

public class app_server {

	public static int localPort = 6066;		//server port
	public static int destPort = 6055;		//client port
	public static UDPServer server;
	
	public static void main(String[] args) {
		
		//start server
		server = new UDPServer(localPort,destPort);
		server.start();
		
		//input parser + help + rest of console interface goes here
		
		//test

	}
    
	
	public static void display(String in) {
		//server dont need no gui, but this function can be used for display formating
		System.out.println(in);
		
	}

}
