package Server;

import java.io.DataInputStream;
import java.util.Random;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;


public class UDPServer extends Thread {											//internal server class
	   
	private int BUFF_SIZE = 1066;	//input buffer size
	
	public boolean isServing = false;		//boolean to toggle whether server is serving, leave false until sync'd
	public boolean dualServerSync = false;	//boolean to indicate whether server has been sync'd with other server
	
	public InetAddress otherServerIP;
	public int otherServerPort;
	
	DatagramSocket serverSocket;				//server listens on this socket
	byte[] inputBuffer, outputBuffer;			//network io buffers
	DatagramPacket dpReceive, dpSend;			//datagram packets
	
	Timer serverSwitchTimer = new Timer();
	TimerExec serverSwitchExec = new TimerExec();
	Random rand = new Random();
	
	ArrayList<User> registeredUsers;
	FileManager fm;
	
	
    public UDPServer(int localPort, int restore, String fileName) {
    	inputBuffer = new byte[BUFF_SIZE];
    	
    	fm = new FileManager(fileName + ".json");
    	
    	registeredUsers = new ArrayList<User>();
    	//if we start a new server... should we attempt to reload the file, ask for it, etc?
    	
    	if (restore == 1) {
    		registeredUsers = fm.loadUserList();
    	}
    	
    	try { serverSocket = new DatagramSocket(localPort); }	//create datagram socket and bind to port
		catch (SocketException e) { 
			e.printStackTrace(); 
			System.out.println("socketException while creating DatagramSocket");
		}
    	
    }
    
    public void run() {
    	
    	System.out.println("starting UDP server");
    	
    	
		
    	while(true) {	//loop for receiving/parsing/handling incoming data
	     	
    		dpReceive = new DatagramPacket(inputBuffer,inputBuffer.length);
    		
    	    try { serverSocket.receive(dpReceive); }			//wait till data is received
    	    catch (IOException e) { e.printStackTrace(); System.out.println("socketException while recieving data");}
    	    
    	    
    	    
    	    //----------------------------
    	    // server input parser/handler
    	    //----------------------------
    	    
    	    /*	0: test case
    	     * 	1: registration request
    	     * 	100: server sync
    	     * 	101: server sync confirmation
    	     * 	102: server switch
    	     */
    	    
    	    //note: we cast the op codes to bytes when we send them, meaning we can only use op codes in the range of [-128, 127]
    	    
    	    
    	    if(isServing)				//socket input handlers which run while serving
    	    switch(inputBuffer[0]) {
    	    	
    	    case 0:	// a test case, print message to console, and respond with 'message received'
    	    	server_app.display("data recieved from client: "+parseString(inputBuffer,1));	//convert data to string, then send to main for displaying
    	    	sendString("message recieved", 0, dpReceive.getAddress(), dpReceive.getPort());	//send response
    	    	break;
    	    	
    	    case 1: //registration request
    	    	//how will the registration request be handled?
    	    	
    	    	
    	    	//1 is reg req
    	    	//2 is un register
    	    	//3 is server to server reg
    	    	//4 is register accepted
    	    	//5 is reg denied
    	    	
    	    	User newUser = unpackRegisterRequest(inputBuffer);
    	    	int reqNum = inputBuffer[0];
    	    	int regStatus = checkUser(newUser);
    	    	
    	    	//okay register them
    	    	if (regStatus == 0) {
    	    		
    	    		registeredUsers.add(newUser);
    	    		//and save them to the file
    	    		
    	    		fm.updateUserList(registeredUsers);
    	    		
    	    		
    	    		//if youre the serving server send other server message and then they should do the save as well. 
    	    		//also they need to record denials... this is a bigger topic I think
    	    		byte[] copiedRegister = Arrays.copyOf(inputBuffer, 56);
    	    		copiedRegister[0] = 3;
    	    		sendServer(copiedRegister);
    	    		
    	    		//then respond to user about what has happened
    	    		sendString("RQ#: " + reqNum, 4, dpReceive.getAddress(), dpReceive.getPort());
    	    	}
    	    	else if (regStatus == 1) {
    	    		//username in use
    	    		sendString("RQ#: " + reqNum + ": Username taken", 5, dpReceive.getAddress(), dpReceive.getPort());
    	    	} else if (regStatus == 2) {
    	    		//ip and port in use
    	    		sendString("RQ#: " + reqNum + ": ip/port combo taken", 5, dpReceive.getAddress(), dpReceive.getPort());
    	    	}
    	    	
    	    	break;
    	    	
    	    case 2: //unregister
    	    	
    	    }
    	    
    	    switch(inputBuffer[0]) {	//socket input handler that runs even when not serving
    	    
    	    case 3: //registration has occured on other server
    	    	
    	    	//registerUser(unpackRegisterRequest(inputBuffer));
    	    	registeredUsers.add(unpackRegisterRequest(inputBuffer));
    	    	fm.updateUserList(registeredUsers);
    	    	break;
    	    	
    	    
    	    case 100:	//server sync: 
    	    	
    	    	//save info for other server
    	    	otherServerIP = dpReceive.getAddress();
    	    	otherServerPort = dpReceive.getPort();
    	    	
    	    	if(Objects.equals(parseString(inputBuffer,1),"s1")) {	//we are server 1
        	    	dualServerSync = true;	//set server as sync'd
        	    	isServing = true;		//server 1 serves first
        	    	//System.out.println("this server is serving");
        	    	sendServer("s2",100);	//respond to sync other server
    	    	} else {	//we are server 2
    	    		dualServerSync = true;	//set server as sync'd but do not set isServing
    	    		sendServer("s",101);	//respond with sync confirmation to start server1's timer
    	    		//System.out.println("this server is not serving");
    	    		serverSwitchTimer = new Timer();		//initialize server 2's timer
        	    	serverSwitchExec = new TimerExec();		//initialize server2's timerTask
    	    	}
    	    	if(isServing) {
    	    		System.out.println("Server is serving");
    	    	}
    	    	else {
    	    		System.out.println("Server is not serving");
    	    	}
    	    	break;
    	    	
    	    case 101: //server sync confirmation
    	    	
    	    	//if we're here, then both servers are now sync'd and this is server 1
    	    	//start serverSwitchTimer
    	    	startTimer();
    	    	break;
    	    
    	    case 102: //server switch
    	    	//other server has notified us that of a server switch
    	    	isServing = true;	//start serving
    	    	startTimer();		//start timer for next server switch
    	    	break;
    	    }
    	    
    	    
    	    
    	    inputBuffer = new byte[BUFF_SIZE]; 	// Clear the buffer after every message. 

	    }
    	
	}
    
    String parseString(byte[] data, int start) { 	//function to convert byte array to string
    	
    	//chars are 2 bytes long,
    	//to be safe we should use the built in string constructor
//    	byte[] toConvert = Arrays.copyOfRange(data, start, data.length);
//    	return new String(toConvert);
    	
        if (data == null) return null; 
        String out = new String(); 
        for(int i=start; data[i]!=0; i++)
        	out += ((char) data[i]); 
        return out; 
    } 
    
//==================================================
// Data Send Functions  
//==================================================
    
    //sends a string with message code, to a given ip address and port
    public void sendString(String message, int op, InetAddress ip, int destPort) {
		
		//convert string to byte array
		byte[] tmpBuff = message.getBytes();		//get message as a byte array
		outputBuffer = new byte[tmpBuff.length+1];
		outputBuffer[0] = (byte) op;				//append a zero to beginning for server side command handler
		for(int i=0; i<tmpBuff.length; i++)			//copy message byte array to output buffer
		outputBuffer[i+1] = tmpBuff[i];
		
    	dpSend = new DatagramPacket(outputBuffer, outputBuffer.length, ip, destPort); 	//create datagram packet 

    	try { serverSocket.send(dpSend); }	//send data
    	catch(IOException e) { e.printStackTrace(); server_app.display("message could not be sent"); }
    	
	}
    
    //sends a string with message code, to the other server
    public void sendServer(String message, int op) {
		
		//convert string to byte array
		byte[] tmpBuff = message.getBytes();		//get message as a byte array
		outputBuffer = new byte[tmpBuff.length+1];
		outputBuffer[0] = (byte) op;				//append a zero to beginning for server side command handler
		for(int i=0; i<tmpBuff.length; i++)			//copy message byte array to output buffer
		outputBuffer[i+1] = tmpBuff[i];
		
    	dpSend = new DatagramPacket(outputBuffer, outputBuffer.length, otherServerIP, otherServerPort); 	//create datagram packet 

    	try { serverSocket.send(dpSend); }	//send data
    	catch(IOException e) { e.printStackTrace(); server_app.display("message could not be sent"); }
    	
	}
    
    //send other server a byte array
    public void sendServer(byte[] toSend) {
    	dpSend = new DatagramPacket(toSend, toSend.length, otherServerIP, otherServerPort); 	//create datagram packet 

    	try { serverSocket.send(dpSend); }	//send data
    	catch(IOException e) { e.printStackTrace(); server_app.display("message could not be sent"); }
    }
    
    
 //==================================================
 // Timer Stuff
 //==================================================
    
   class TimerExec extends TimerTask {
	   public void run() { 
		   System.out.println("timer triggered, switching servers");
		   
		   //notify registered clients, this can only be done after there are registered clients(FRANK)
		   
		   sendServer("",102);	//notify other server first
		   isServing = false;	//stop serving
		   
	   }
   }
   
   public void startTimer() {
	   
	   //reset timer
	   serverSwitchTimer.cancel();
	   serverSwitchTimer.purge();
	   serverSwitchTimer = new Timer();
	   serverSwitchExec = new TimerExec();

	   //project description says to "pick a random value say 5m"
	   int val = 240000 + rand.nextInt(360000); //pick a value between 240,000 and 360,000 (4m-6m)
	   //int val = 10000 + rand.nextInt(2000);	//10-12 seconds, left here for debuggin purposes
	   System.out.println("server switch in: " + val);
   	
	   //schedule(TimerTask task, Date time)
	   // task: task to run
	   // time: time to wait before running task in milliseconds
	   serverSwitchTimer.schedule(serverSwitchExec, val);
   	
   }
    
 //==================================================
    
   private User unpackRegisterRequest(byte[] req) {
	   
	   //split the array into different arrays
	   //format each one as required
	   //create the user object
	   
	   byte[] bname = Arrays.copyOfRange(req, 2, 22);
	   byte[] bip = Arrays.copyOfRange(req, 22, 52);
	   byte[] bsocket = Arrays.copyOfRange(req, 52, 60);
	   
	   String name = new String(bname);
	   //get rid of white space
	   //if there is any... or we can ensure there is by not allowing max length, always padding with an end char
	   //we could have done this to let ppl have stuff of any length 
	   name = name.substring(0, name.indexOf('-'));
	   
	   String ip = new String(bip);
	   ip = ip.substring(0, ip.indexOf('-'));
	   
	   int socket = fromByteArray(bsocket);
	   
	   
	   User user = new User(name, ip, socket);
	   
	   
	   return user;
   }
   
   
   //gunna turn this into just a check availability
    private int checkUser(User user) {
    	
		//check if user is registered
		//we will maintain a list of users
		//now check for free username etc.
		
		for(User u : registeredUsers) {
			if(u.getName() == user.getName()) {
				return 1; //username already exists
			}
			else if (u.getIp() == user.getIp() && u.getSocket() == user.getSocket()) {
				return 2; //ip and socket already registered
			}
		}
		
		return 0; //no registered user
    }
    
    private int fromByteArray(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) | 
               ((bytes[1] & 0xFF) << 16) | 
               ((bytes[2] & 0xFF) << 8 ) | 
               ((bytes[3] & 0xFF) << 0 );
   }
	   
	      
}