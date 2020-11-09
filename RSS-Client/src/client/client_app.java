package client;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Scanner;

public class client_app {
	
	public static int localPort = 6055;				//client port number
	
	public static int server1Port = 6077;			//server1 port number
	public static int server2Port = 6066;			//server2 port number
	public static String server1IP = "localhost";	//server1 ip address
	public static String server2IP = "localhost";	//server2 ip address
	
	public static SocketListener socket;

	public static void main(String[] args) {
		//start socket listener thread
		Scanner in = new Scanner(System.in);
		Scanner stg = new Scanner(System.in);
		boolean stop=false;
		socket = new SocketListener(server1IP, server2IP, server1Port, server2Port, localPort);
		socket.start();
		boolean checked = false;
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
	        // Assuming the request numbers correspond to the option the user chooses
	        
			while (!in.hasNextInt()) in.next();
			int select = in.nextInt();
			switch(select){
			case 0: String test="";
					System.out.println("\t  Enter a word to be sent");
					Scanner scan = new Scanner(System.in);
					test+=scan.nextLine();
			        scan.close();
					socket.sendString1(test, 0);
					System.out.println(select);
				break;
			case 1: String registration = "Request 1, ";
					System.out.println("\t  Enter the name"); 
					String nameInput = in.next();
					registration=  registration + nameInput+", ";
					System.out.println("\t  Enter the Ip Address"); 
					String IpAddressInput = in.next();
					registration=registration+IpAddressInput+", ";
					System.out.println("\t  Enter the socket number"); 
					String socketInput = in.next();
					registration=registration+socketInput;
					socket.sendString1(registration, 1);
			   break;
			case 2: System.out.println("Deregistration is not done yet"); 
			   break;

			case 3: String update = "Request 3, ";
			System.out.println("\t  Enter the user name you want to update"); 
			String userNameUpdate = in.next();
			update=  update + userNameUpdate+", ";
			System.out.println("\t  Update name"); 
			String nameUpdate = in.next();
			update=  update + nameUpdate+", ";
			System.out.println("\t  Update Ip Address"); 
			String IpAddressUpdate = in.next();
			update=  update+IpAddressUpdate+", ";
			System.out.println("\t  Update socket number"); 
			String socketUpdate = in.next();
			update=  update+socketUpdate;
			socket.sendString1(update, 3);		
			   break;
			case 4: System.out.println("\t  What is the user's name?"); 
				String userName = in.next();
				System.out.println("\t  Add a subject of interest"); 
				String subjectInput = in.next();
				socket.sendString1(subjectInput, 4);	
				break; 
			case 5: System.out.println("Publication on a subject of interest is not done yet"); 
			   break;	
			case 6: System.out.println("Stopping the app");
				stop = true;
			   break;
			}
			
			
		}
		
		
	}
	public static void sendingUserData(client_User user){
		
		socket.sendString1(user.getName(), 0);
		socket.sendString1(user.getIp_address(), 0);
		socket.sendString1(String.valueOf(user.getSocketNumber()), 0);
		Enumeration enu = user.getSubjectOfInterestVector().elements();
		while(enu.hasMoreElements()) {
			socket.sendString1(enu.nextElement().toString()+" ", 0);
		}
	}
	public static void display(String in) {
		//hopefully print to a GUI one day, console for now
		System.out.println(in);
	}
	
}
