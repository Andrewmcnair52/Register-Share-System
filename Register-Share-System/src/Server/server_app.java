package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class server_app {

	public static int localPort1 = 6077;	//hard coded port that first server listens on
	public static int localPort2 = 6066;	//hard coded port that second server listens on
	public static UDPServer server;
	
	


	public static void main(String[] args) {
		
		//initialize console input
		BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));  
		
		String filename = "default";
		String filename2 = "default2";

		
		try {
			
			System.out.println("Would you liked to restore a userlist from a file?");
			System.out.print("enter 1 for yes or 0 for no > ");
			
			int restore = Integer.parseInt(cin.readLine());
			
			System.out.println("Please enter the restore file name you want to load/create (or nothing for default)");
			System.out.print("> ");
			char[] fnbuff = new char[64];
			cin.read(fnbuff);
			
			byte[] returnChar = System.getProperty("line.separator").getBytes();
			char firstRC = (char)returnChar[0];
			
			if (fnbuff[0] != firstRC) {
				filename = String.valueOf(fnbuff);
				filename = filename.substring(0,filename.indexOf(firstRC));
				filename2 = filename + "2";
			}
			
			//prompt user to designate server as first or second
			System.out.println("please designate this server as server 1 or server 2");
			System.out.print("enter 1 or 2 > ");
			
			
			int serverNum = Integer.parseInt(cin.readLine());
			if(serverNum==1) {
				
				//start server, which will await sync with server 2 before coming online
				server = new UDPServer(localPort1, restore, filename);
				server.start();
				
				//for testing purposes
				Subject e = new Subject("Books");
				e.addUser("test");
				server.subjects.add(e);
			
			} else if(serverNum==2) {
				
				//solicit ip for server 1
				System.out.println("please enter the first servers IP address, or 'localhost' for local machine");
				System.out.print("> ");
				String secondServerIPstr = cin.readLine();
				
				//start server 2
				server = new UDPServer(localPort2, restore, filename2);
				server.start();
				
				InetAddress secondServerIP;
				if(secondServerIPstr.equals("localhost")) secondServerIP = InetAddress.getLocalHost();
				else secondServerIP = InetAddress.getByName(secondServerIPstr);
				
				//send sync message to server 1
				//using sendString() over sendServer() since second server info is not available yet
				//send server s1 to tell it that it should be server 1, it will then respond triggering this servers sync
				server.sendString("s1",100, secondServerIP, localPort1);
				
			}
			
			
		
		} catch (IOException e) {
			System.out.println("An IOException was thrown during console input");
			e.printStackTrace();
			System.out.println("exiting");
		} catch (NumberFormatException e) {
			System.out.println("\ninvalid value\n"+e.getMessage()+"\nvalue should be an integer\nexiting ...");
			
		}
		
		for (;;) {
			System.out.println("Server Side Menu:");
			System.out.println("0: show last 20 lines of the log");
			
			//char[] menuBuf = new char[1];
			int menuO =-1;
			try {
				menuO = Integer.parseInt(cin.readLine());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			switch (menuO) {
			case 0:
				server.fm.printLastLogs(20);
				break;
			case -1:
				//best way to do this ?
				System.exit(0);
				return;
			}
			
		}
		
//		System.out.println("Please enter the restore file name you want to load/create (or nothing for default)");
//		System.out.print("> ");
//		char[] fnbuff = new char[64];
//		cin.read(fnbuff);

	}
	
	public static void display(String in) {
		//server dont need no gui, but this function can be used for display formating
		System.out.println(in);
		
	}
	
}
