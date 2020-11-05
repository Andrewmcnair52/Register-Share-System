package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

public class server_app {

	public static int localPort1 = 6077;	//hard coded port that first server listens on
	public static int localPort2 = 6066;	//hard coded port that second server listens on
	public static UDPServer server;
	
	public static void main(String[] args) {
		
		//initialize console input
		BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));   
		
		//prompt user to designate server as first or second
		System.out.println("please designate this server as server 1 or server 2");
		System.out.print("enter 1 or 2 > ");
		
		try {
			
			int serverNum = Integer.parseInt(cin.readLine());
			if(serverNum==1) {
				
				//start server, which will await sync with server 2 before coming online
				server = new UDPServer(localPort1);
				server.start();
			
			} else if(serverNum==2) {
				
				//start server 2
				server = new UDPServer(localPort2);
				server.start();
				
				//solicit ip for server 1
				System.out.println("please enter the second servers IP address, or 'localhost' for local machine");
				System.out.print("> ");
				String secondServerIPstr = cin.readLine();
				
				InetAddress secondServerIP;
				if(secondServerIPstr.equals("localhost")) secondServerIP = InetAddress.getLocalHost();
				else secondServerIP = InetAddress.getByName(secondServerIPstr);
				
				//send sync message to server 1
				//using sendString() over sendServer() since second server info is not available yet
				server.sendString("s",100, secondServerIP, localPort1);
				
			}
			
		
		} catch (IOException e) {
			System.out.println("An IOException was thrown during console input");
			e.printStackTrace();
			System.out.println("exiting");
		} catch (NumberFormatException e) {
			System.out.println("\ninvalid value\n"+e.getMessage()+"\nvalue should be an integer\nexiting ...");
			
		}
		
		//start server
		//server = new UDPServer(localPort1, localPort2, destPort);
		//server.start();
		
		//input parser + help + rest of console interface goes here

	}
    
	
	public static void display(String in) {
		//server dont need no gui, but this function can be used for display formating
		System.out.println(in);
		
	}

}
