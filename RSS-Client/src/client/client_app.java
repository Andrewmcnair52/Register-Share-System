package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class client_app {
	
	public static SocketListener socket;

	public static void main(String[] args) {
		
		BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));  
		
		try {
		
			System.out.print("local port: ");
			int localPort = Integer.parseInt( cin.readLine() );
			
			System.out.print("enter <IP>:<Port> of server1: ");
			String inServer1 = cin.readLine();
			int server1Port = Integer.parseInt( inServer1.substring(inServer1.indexOf(':')+1) );
			String server1IP = inServer1.substring(0,inServer1.indexOf(':'));
			
			System.out.print("enter <IP>:<Port> of server2: ");
			String inServer2 = cin.readLine();
			int server2Port = Integer.parseInt( inServer2.substring(inServer2.indexOf(':')+1) );
			String server2IP = inServer2.substring(0,inServer2.indexOf(':'));
			
			//start socket listener thread
			socket = new SocketListener(server1IP, server2IP, server1Port, server2Port, localPort);
			socket.start();


		} catch (IOException e) {
			System.out.println("An IOException was thrown during console input");
			e.printStackTrace();
			System.out.println("exiting");
			return;
		} catch (NumberFormatException e) {
			System.out.println("\ninvalid value\n"+e.getMessage()+"\nvalue should be an integer\nexiting ...");
			return;
		}
		
		display("press enter to send registration request");
		try { int read = System.in.read(new byte[2]); } catch (IOException e) { e.printStackTrace(); }
		
		//test register a user
		String rr = socket.formatRegisterReq("test", "localhost", 6055);
		socket.sendString(rr, 1);
		
		//test a deregistration
		//String dr = socket.formatDeregisterReq("test");
		//socket.sendString(dr, 2, 1);
		
		//test publish
		//String mr = socket.formatPublishReq("test", "Books", "Books are so dope!");
		//socket.sendString(mr, 11, 1);
		
	}
	
	public static void display(String in) {
		//hopefully print to a GUI one day, console for now
		System.out.println(in);
	}
	
}
