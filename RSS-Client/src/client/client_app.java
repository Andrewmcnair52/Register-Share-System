package client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import java.util.Scanner;

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
		
		Scanner in = new Scanner(System.in);
		Scanner stg = new Scanner(System.in);
		boolean stop=false;
		boolean checked = false;
		ArrayList<String> subjects;
		System.out.println("Welcome to client console");
		while(!stop) {
			System.out.println("\nPlease select an option from the menu");
			System.out.println("0\t Testing the server");
			System.out.println("1\t Register");
	        System.out.println("2\t Deregister");
	        System.out.println("3\t Update a user information");
	        System.out.println("4\t Update subject of interest");
	        System.out.println("5\t Publish on a subject of interest");
	        System.out.println("6\t Stop the client app");
	        System.out.println("7\t Restart the socket Listener");
	        System.out.print("> ");
	        while (!in.hasNextInt()) in.next();
			int select = in.nextInt();
			switch(select){
			case 0: String test="";
					System.out.println("\t  Enter a word to be sent");
					System.out.print("> ");
					Scanner scan = new Scanner(System.in);
					test+=scan.nextLine();
			        //scan.close();
					socket.sendString(test, 0);
				break;
			case 1: 
			System.out.println("\t  Enter the name");
			System.out.print("> ");
			String nameInput = in.next();
			System.out.println("\t  Enter the Ip Address");
			System.out.print("> ");
			String IpAddressInput = in.next();
			System.out.println("\t  Enter the socket number");
			System.out.print("> ");
			int socketInput = in.nextInt();
			String rr = socket.formatRegisterReq(nameInput, IpAddressInput, socketInput);
			socket.sendString(rr, 1);
				break;
			case 2: 
				System.out.println("\t  Enter the name"); 
				System.out.print("> ");
				nameInput = in.next();
				String dr = socket.formatDeregisterReq(nameInput);
				socket.sendString(dr, 2);
				break;
			case 3: 
			System.out.println("\t  Enter the user's name you want to update");
			String nameUpdate = in.next();
			System.out.println("\t  Update Ip Address"); 
			String IpAddressUpdate = in.next();
			System.out.println("\t  Update socket number"); 
			int socketUpdate = in.nextInt();
			String ur = socket.formatUpdateReq(nameUpdate, IpAddressUpdate, socketUpdate);
			socket.sendString(ur, 3);		
			   break;
			case 4: 
				System.out.println("\t  What is the user's name?"); 
				String userName = in.next();
				String yesNo ="n";
				String subjectInput = "";
				subjects = new ArrayList<String>();
				do {
					System.out.println("\t  Add a subject of interest"); 
					String sInput = in.next();
					subjects.add(subjectInput);
					subjectInput = subjectInput + sInput +"-";
					
					System.out.println("\t  Do you want to add more subject? y/n"); 
					yesNo = in.next();
				}while(!yesNo.equals("n"));
				String sr = socket.formatSubjectReq(userName, subjectInput);
				socket.sendString(sr, 4);
				System.out.println(subjectInput);
				break;
				
			case 5: 
				System.out.println("\t  Enter the user's name");
				String namePublish = in.next();
				System.out.println("\t  Enter the subject name"); 
				String subjectPublish = in.next();
				System.out.println("\t  Enter the text to publish"); 
				String textPublsih = in.next();
				String publishInput = in.next();
				String pr = socket.formatPublishReq(namePublish, subjectPublish, publishInput);
				socket.sendString(pr, 11);
				break;
			case 6: System.out.println("Stopping the app");
				stop = true;
			   break;
			case 7: 
			if(!socket.isAlive()) {	
				System.out.println("Restarting the socket Listener... ");
				socket.start();
			}else {System.out.println("The socket is already listening ");}
			   break;
			}}
		
	}
	
	public static void display(String in) {
		//hopefully print to a GUI one day, console for now
		System.out.println(in);
	}
	
}