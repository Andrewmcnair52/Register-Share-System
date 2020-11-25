package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class server_app {
	
	public static UDPServer server;
	
	public static void main(String[] args) {
		
		//initialize console input
		BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));  

		
		try {
			
			//prompt user for ip and port
			System.out.print("please enter this servers port: ");
			int thisServersPort = Integer.parseInt( cin.readLine() );
			
			//prompt user to designate server as first or second
			System.out.print("please enter server number 1 or 2: ");
			int serverNum = Integer.parseInt(cin.readLine());
			
			if(serverNum==1) {
				
				//start server, which will await sync with server 2 before coming online
				server = new UDPServer(thisServersPort, 1);
				server.start();
				
				//for testing purposes
				Subject e = new Subject("Books");
				e.addUser("test");
				server.subjects.add(e);
			
			} else if(serverNum==2) {
				
				//prompt user for ip and port
				System.out.print("please enter <ip>:<port> for server 1: ");
				String input = cin.readLine();
				int otherServersPort = Integer.parseInt( input.substring(input.indexOf(':')+1) );
				String strIP = input.substring(0,input.indexOf(':'));
				InetAddress otherServersIP;
				if(Objects.equals(strIP, "localhost")) otherServersIP = InetAddress.getLocalHost();
				else otherServersIP = InetAddress.getByName(strIP);
				
				//start server 2
				server = new UDPServer(thisServersPort, 2);
				server.start();
				
				//send sync message to server 1
				//using sendString() over sendServer() since second server info is not available yet
				//send server s1 to tell it that it should be server 1, it will then respond triggering this servers sync
				server.sendString("s1",100, otherServersIP, otherServersPort);
				
			}
			
			String out = "\nServer Menu:\n";
			out += "0: exit\n";
			out += "1: print logs\n";
			out += "> ";
			int input;
			
			while(true) {
				
				System.out.print(out);
				input = Integer.parseInt( cin.readLine() );
				System.out.println("\n");
				
				switch(input) {
				
				case 0:
					server.serverSocket.close();
					return;
					
				case 1:
					System.out.println("LOGS:");
					server.fm.printLastLogs(20);
					break;
					
				default:
					break;
				}
			}
			
			
		
		} catch (IOException e) {
			System.out.println("An IOException was thrown during console input");
			e.printStackTrace();
			System.out.println("exiting");
		} catch (NumberFormatException e) {
			System.out.println("\ninvalid value\n"+e.getMessage()+"\nvalue should be an integer\nexiting ...");
			
		}

	}
	
}