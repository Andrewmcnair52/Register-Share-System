package Server;

import java.io.DataInputStream;
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
import java.util.Objects;
import java.util.Vector;


public class UDPServer extends Thread {											//internal server class
	   
	private int BUFF_SIZE = 1066;	//input buffer size
	
	public boolean isServing = false;		//boolean to toggle whether server is serving, leave false until sync'd
	public boolean dualServerSync = false;	//boolean to indicate whether server has been sync'd with other server
	
	public InetAddress otherServerIP;
	public int otherServerPort;
	
	DatagramSocket serverSocket;				//server listens on this socket
	byte[] inputBuffer, outputBuffer;			//network io buffers
	DatagramPacket dpReceive, dpSend;	;		//datagram packets
	
	ArrayList<String> registeredUsers;
	
	
    public UDPServer(int localPort) {
    	inputBuffer = new byte[BUFF_SIZE];
    	registeredUsers = new ArrayList<String>();
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
    	     * 	101: server switch
    	     * 
    	     */
    	    
    	    
    	    if(isServing)				//socket input handlers which run while serving
    	    switch(inputBuffer[0]) {
    	    	
    	    case 0:	// a test case, print message to console, and respond with 'message received'
    	    	server_app.display("data recieved from client: "+parseString(inputBuffer,1));	//convert data to string, then send to main for displaying
    	    	sendString("message recieved", 0, dpReceive.getAddress(), dpReceive.getPort());	//send response
    	    	break;
    	    	
    	    case 1: //registration request
    	    	
    	    	
    	    }
    	    
    	    switch(inputBuffer[0]) {	//socket input handler that runs even when not serving
    	    
    	    case 100:	//server sync: 
    	    	
    	    	//save info for other server
    	    	otherServerIP = dpReceive.getAddress();
    	    	otherServerPort = dpReceive.getPort();
    	    	
    	    	//respond to sync other server
    	    	sendServer("s",101);
    	    	
    	    	//set servers as sync'd
    	    	dualServerSync = true;
    	    	isServing = true;		//server 1 starts
    	    	System.out.println("this server is serving");
    	    	
    	    	break;
    	    	
    	    case 101: //server sync confirmation
    	    	
    	    	//save info for other server
    	    	otherServerIP = dpReceive.getAddress();
    	    	otherServerPort = dpReceive.getPort();
    	    	
    	    	//set servers as sync'd, leave isServing false
    	    	dualServerSync = true;
    	    	System.out.println("this server is not serving");
    	    	
    	    	break;
    	    
    	    case 102: //server switch
    	    	
    	    	break;
    	    }
    	    
    	    
    	    
    	    inputBuffer = new byte[BUFF_SIZE]; 	// Clear the buffer after every message. 

	    }
    	
	}
    
    String parseString(byte[] data, int start) { 	//function to convert byte array to string
    	
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
    
//==================================================
    
    private int registerUser() {
    	
    	//if i am not the active server, do nothing 
    	//check if user is already registerd
    	//check if user name is available
    	// if its all good, register them
    	// then send the other server a message about what happened
    	// then send the user back a message about what happened
    	
    	return 0;
    }
	   
	      
}