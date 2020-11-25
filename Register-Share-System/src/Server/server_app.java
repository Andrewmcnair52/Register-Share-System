package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class server_app {
	
	public static UDPServer server;
	
	public static String newServerIP;
	public static int newServerPort;
	
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
			
			Thread.sleep(100);
			
			String out;
			int input;
			while(true) {
				
				out = "";
				out = "\nServer status: ";
				if(server.isServing) out += "serving, ";
				else out += "not serving, ";
				if(server.dualServerSync) out += "sync'd\n";
				else out += "not sync'd\n";
				out += "0: exit\n";
				out += "1: print logs\n";
				out += "2: update server\n";
				out += "3: print other server info\n";
				out += "4: print registered users\n";
				out += "> ";
				
				System.out.print(out);
				input = Integer.parseInt( cin.readLine() );
				
				switch(input) {
				
				case 0:
					System.exit(0);
					
				case 1:
					System.out.println("LOGS:");
					server.fm.printLastLogs(20);
					break;
					
				case 2:
					
					//get new server ip and port
					System.out.print("enter new ip:port for server: ");
					String strInput = cin.readLine();
					newServerPort = Integer.parseInt( strInput.substring(strInput.indexOf(':')+1) );
					newServerIP = strInput.substring(0,strInput.indexOf(':'));
					
					//validate ip address
					try {
						if(Objects.equals(newServerIP, "localhost")) InetAddress.getLocalHost();
						else InetAddress.getByName(newServerIP);
					} catch(UnknownHostException e) { System.out.println("cannot update server, invalid host ip"); break;}
						
					if(!server.dualServerSync) { System.out.println("cannot update server, servers not yet sync'd"); break; } //check if sync'd
					if(server.isServing) { System.out.println("cannot update server, this server is serving"); break; }	//check that server is not serving
					
					//begin update request
					server.sendServer("", 51); //request server update
					synchronized(server) {		//wait for server request to finnish
						try { 
							System.out.println("\nrequesting server update"); 
							server.wait();
			            } catch(InterruptedException e){  e.printStackTrace(); }
					}
					break;
					
				case 3:
					if(server.dualServerSync) System.out.println("\nother server: "+server.otherServerIP+":"+server.otherServerPort);
					else System.out.println("\ncant display info, not sync'd");
					break;
					
				case 4:
					if(server.dualServerSync) {
						System.out.println("\nUsers:");
						Iterator<User> it = server.registeredUsers.iterator();
						while(it.hasNext()) { 
							User tmpUser = it.next();
							System.out.println(tmpUser.getName()+", "+tmpUser.getIp()+", "+tmpUser.getSocket());
						}
					} else System.out.println("\ncant display info, not sync'd");
					break;
					
				default:
					break;
				}
			}
			
			
		
		} catch (IOException e) {
			System.out.println("An IOException was thrown during console input");
			e.printStackTrace();
			System.out.println("exiting");
			return;
		} catch (NumberFormatException e) {
			System.out.println("\ninvalid value\n"+e.getMessage()+"\nvalue should be an integer\nexiting ...");
			return;
			
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
	
}
