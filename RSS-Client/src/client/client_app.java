package client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.Scanner;

public class client_app {
	

	public static SocketListener socket;
	public static String loggedUser;

	public static void main(String[] args) {
		
		BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));
		
		
		//not sure if im gunna use this actually..
		//lets get things functional then ill play with how it works
		
		
		String ezIp = "";
		int localPort = 0;
		boolean registered = false;
		
		
		try {
			
			System.out.print("Are you already registered? (y/n):");
			String inRegQ = cin.readLine();
			if (inRegQ.equals("y"))
				registered = true;
			
			if (!registered) {
				System.out.print("enter your ip: ");
				ezIp = cin.readLine();
			}
			
			System.out.print("Enter a username: ");
			loggedUser = cin.readLine();
		
			System.out.print("local port: ");
			localPort = Integer.parseInt( cin.readLine() );
			
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
		
		if (!registered) {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//auto register them
			String rr = socket.formatRegisterReq(loggedUser, ezIp, localPort);
			socket.sendString(rr, 1);
		}
		
		Scanner in = new Scanner(System.in);
		ArrayList<String> subjects;
		System.out.println("Welcome to client console");
		while(true) {
			System.out.println("\nPlease select an option from the menu");
			System.out.println("0\t Testing the server");
			System.out.println("1\t Register another user");
	        System.out.println("2\t Deregister");
	        System.out.println("3\t Update user information");
	        System.out.println("4\t Update subject of interest");
	        System.out.println("5\t Publish on a subject of interest");
	        System.out.println("6\t Stop the client app");
	        System.out.println("7\t Restart the socket Listener");
	        System.out.print("> ");
	        while (!in.hasNextInt()) in.next();
			int select = Integer.parseInt(in.nextLine());
			switch(select){
			case 0: String test="";
					System.out.println("\t  Enter a word to be sent");
					System.out.print("> ");
					Scanner scan = new Scanner(System.in);
					test=scan.nextLine();
					socket.sendString(test, 0);
				break;
			case 1: 
			System.out.println("\t  Enter the name");
			System.out.print("> ");
			String nameInput = in.nextLine();
			System.out.println("\t  Enter the Ip Address");
			System.out.print("> ");
			String IpAddressInput = in.nextLine();
			System.out.println("\t  Enter the socket number");
			System.out.print("> ");
			int socketInput = Integer.parseInt(in.nextLine());
			String rr = socket.formatRegisterReq(nameInput, IpAddressInput, socketInput);
			socket.sendString(rr, 1);
				break;
			case 2: 
				String dr = socket.formatDeregisterReq(loggedUser);
				socket.sendString(dr, 2);
				break;
			case 3: 
			System.out.println("\t  Update Ip Address"); 
			String IpAddressUpdate = in.nextLine();
			System.out.println("\t  Update socket number"); 
			int socketUpdate = Integer.parseInt(in.nextLine());
			String ur = socket.formatUpdateReq(loggedUser, IpAddressUpdate, socketUpdate);
			socket.sendString(ur, 3);		
			   break;
			case 4: 
				String yesNo ="n";
				String subjectInput = "";
				subjects = new ArrayList<String>();
				do {
					System.out.println("\t  Add a subject of interest"); 
					String sInput = in.nextLine();
					subjects.add(subjectInput);
					subjectInput = subjectInput + sInput +"-";
					
					System.out.println("\t  Do you want to add more subject? y/n"); 
					yesNo = in.nextLine();
					char yesNo_ = yesNo.charAt(0);
				}while(!yesNo.equals("n"));
				String sr = socket.formatSubjectReq(loggedUser, subjectInput);
				socket.sendString(sr, 4);
				break;
				
			case 5: 
				System.out.println("\t  Enter the subject name"); 
				String subjectPublish = in.nextLine();
				System.out.println("\t  Enter the text to publish"); 
				String textPublish = in.nextLine();
				String pr = socket.formatPublishReq(loggedUser, subjectPublish, textPublish);
				socket.sendString(pr, 5);
				break;
			case 6: System.out.println("Stopping the app");
			System.exit(0);
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