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
	private InetAddress serverIP;
	private int localPort, destPort;
	
	public SocketListener(String inServerIP, int inLocalPort, int inDestPort) {
		inputBuffer =new byte[BUFF_SIZE];
		localPort = inLocalPort;
		destPort = inDestPort;
		try { socket = new DatagramSocket(localPort); }
		catch (SocketException e) { e.printStackTrace(); System.out.println("SocketException while declaring datagram socket"); }
    	
		try {
			if(inServerIP.equals("localhost")) serverIP = InetAddress.getLocalHost();
			else serverIP = InetAddress.getByName(inServerIP);
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
    	    	app_client.display("data recieved, from server: " + parseString(inputBuffer, 1));	//convert data to string, then send to app for displaying
    	    	break;
    	    	
    	    default:
    	    	app_client.display("invalid operation recieved, initial byte out of range");
    	    }
    	    
    	    
    	    
    	    inputBuffer = new byte[BUFF_SIZE]; 	// Clear the buffer after every message. 
    	}
		
	}
	
	public void sendString(String message, int op) {
		
		//convert string to byte array
		byte[] tmpBuff = message.getBytes();		//get message as a byte array
		outputBuffer = new byte[tmpBuff.length+1];
		outputBuffer[0] = (byte) op;						//append a zero to beginning for server side command handler
		for(int i=0; i<tmpBuff.length; i++)			//copy message byte array to output buffer
			outputBuffer[i+1] = tmpBuff[i]; 
    	
		dpSend = new DatagramPacket(outputBuffer, outputBuffer.length, serverIP, destPort); 	//create datagram packet 

    	try { socket.send(dpSend); }	//send data
    	catch(IOException e) { e.printStackTrace(); app_client.display("message could not be sent"); }
    	
	}
	
	 
    String parseString(byte[] data, int start) { 	//function to convert byte array to string
    	
        if (data == null) return null; 
        String out = new String(); 
        for(int i=start; data[i]!=0; i++)
        	out += ((char) data[i]); 
        return out; 
    }
    
    
	
	
}
