package client;

public class app {
	
	//test push
	
	public static int localPort = 6055;				//client port number
	public static int destPort = 6066;				//server port number
	public static String serverIP = "localhost";	//server ip address
	
	public static SocketListener socket;

	public static void main(String[] args) {

		//start socket listener thread
		socket = new SocketListener(serverIP, localPort, destPort);
		socket.start();
		
		socket.sendString("hello world", 0);	//send message, with op 0
		
	}
	
	public static void display(String in) {
		//hopefully print to a GUI one day, console for now
		System.out.println(in);
	}
	
}
