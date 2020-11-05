package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class SocketListener extends Thread {

	private int BUFF_SIZE = 1066;	//input buffer size
	
	private byte[] inputBuffer, outputBuffer;
	private DatagramPacket dpSend, dpReceive;
	private DatagramSocket socket;
	private InetAddress server1IP, server2IP;
	private int localPort, server1Port, server2Port;
	
	public SocketListener(String inServer1IP, String inServer2IP, int inServer1Port, int inServer2Port, int inLocalPort) {
		inputBuffer = new byte[BUFF_SIZE];
		localPort = inLocalPort;
		server1Port = inServer1Port;
		server2Port = inServer2Port;
		try { socket = new DatagramSocket(localPort); }
		catch (SocketException e) { e.printStackTrace(); System.out.println("SocketException while declaring datagram socket"); }
		try {
			if(inServer1IP.equals("localhost")) server1IP = InetAddress.getLocalHost();
			else server1IP = InetAddress.getByName(inServer1IP);
			if(inServer2IP.equals("localhost")) server2IP = InetAddress.getLocalHost();
			else server2IP = InetAddress.getByName(inServer2IP);
		} catch (IOException e) { e.printStackTrace(); System.out.println("error while resolving InerAddress");}
	}
	
	public void run() {
    	
    	while(true) {	//loop for receiving/parsing/handling incoming data
    		
    		dpReceive = new DatagramPacket(inputBuffer,inputBuffer.length);
    			
    		System.out.println("\nwaiting to recieve data");
    	    try { socket.receive(dpReceive); }			//wait till data is received
    	    catch (IOException e) { e.printStackTrace(); System.out.println("socketException while recieving data");}
    	    
    	    //----------------------------
    	    // client input parser/handler
    	    //----------------------------
    	    
    	    switch(inputBuffer[0]) {
	    	
    	    case 0:	// a test case, print message to console

    	    	client_app.display("data recieved, from server: " + parseString(inputBuffer, 1));

    	    	break;
    	    	
    	    default:
    	    	client_app.display("invalid operation recieved, initial byte out of range");
    	    }
    	    
    	    
    	    
    	    inputBuffer = new byte[BUFF_SIZE]; 	// Clear the buffer after every message. 
    	}
		
	}
	
	public void sendString1(String message, int op) {
		
		//convert string to byte array
		byte[] tmpBuff = message.getBytes();		//get message as a byte array
		outputBuffer = new byte[tmpBuff.length+1];
		outputBuffer[0] = (byte) op;						//append a zero to beginning for server side command handler
		for(int i=0; i<tmpBuff.length; i++)			//copy message byte array to output buffer
			outputBuffer[i+1] = tmpBuff[i]; 
    	
		dpSend = new DatagramPacket(outputBuffer, outputBuffer.length, server1IP, server1Port); 	//create datagram packet 

    	try { socket.send(dpSend); }	//send data
    	catch(IOException e) { e.printStackTrace(); client_app.display("message could not be sent"); }
    	
	}
	
	public void sendString2(String message, int op) {
		
		//convert string to byte array
		byte[] tmpBuff = message.getBytes();		//get message as a byte array
		outputBuffer = new byte[tmpBuff.length+1];
		outputBuffer[0] = (byte) op;						//append a zero to beginning for server side command handler
		for(int i=0; i<tmpBuff.length; i++)			//copy message byte array to output buffer
			outputBuffer[i+1] = tmpBuff[i]; 
    	
		dpSend = new DatagramPacket(outputBuffer, outputBuffer.length, server2IP, server2Port); 	//create datagram packet 

    	try { socket.send(dpSend); }	//send data
    	catch(IOException e) { e.printStackTrace(); client_app.display("message could not be sent"); }
    	
	}
	
	
	public void formatRegisterReq() {
		int regId = 0; // we will have to give all requests codes
		int rqNum = 1; //whats this?
		String name = "frank";
		String ip = "192.168.1.1"; // dummy address, will all be local host no?
		int socket = localPort; //socket = port?
		
		String registerReq = regId + rqNum + name + ip + socket;
		
		
		sendString1(registerReq, 0);
		
		
		
		
	}
	
	 
	String parseString(byte[] data, int start) { 	//function to convert byte array to string
    	
        if (data == null) return null; 
        String out = new String(); 
        for(int i=start; data[i]!=0; i++)
        	out += ((char) data[i]); 
        return out; 
    }
	
	
}
