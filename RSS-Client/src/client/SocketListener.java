package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;



public class SocketListener extends Thread {

	private int BUFF_SIZE = 1066;	//input buffer size
	
	private byte[] inputBuffer, outputBuffer;
	private DatagramPacket dpSend, dpReceive;
	private DatagramSocket socket;
	private InetAddress server1IP, server2IP;
	private int localPort, server1Port, server2Port;
	
	private Random RNG = new Random(System.currentTimeMillis());
	
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
    	    	
    	    case 4:
    	    	
    	    	client_app.display(parseString(inputBuffer, 1));
    	    	break;
    	    	
    	    case 5:
    	    	
    	    	client_app.display(parseString(inputBuffer, 1));
    	    	break;
    	    
    	    default:
    	    	client_app.display("invalid operation recieved, initial byte out of range");
    	    	client_app.display("data recieved, from server: " + parseString(inputBuffer, 1));
    	    }
    	    
    	    
    	    
    	    inputBuffer = new byte[BUFF_SIZE]; 	// Clear the buffer after every message. 
    	}
		
	}
	
	public void sendString(String message, int op, int serverNum) {
		
		//convert string to byte array
		byte[] tmpBuff = message.getBytes();		//get message as a byte array
		outputBuffer = new byte[tmpBuff.length+1];
		outputBuffer[0] = (byte) op;						//append a zero to beginning for server side command handler
		for(int i=0; i<tmpBuff.length; i++)			//copy message byte array to output buffer
			outputBuffer[i+1] = tmpBuff[i]; 
    	
		if(serverNum==1) {dpSend = new DatagramPacket(outputBuffer, outputBuffer.length, server1IP, server1Port); 	//create datagram packet 
		System.out.println("sending from server 1");}
		else if(serverNum==2) {dpSend = new DatagramPacket(outputBuffer, outputBuffer.length, server2IP, server2Port); 	//create datagram packet 
		System.out.println("sending from server 2");}
		else { System.out.println("message could not be sent, invalid server number: " + serverNum); return; }
		
    	try { socket.send(dpSend); }	//send data
    	catch(IOException e) { e.printStackTrace(); client_app.display("message could not be sent"); }
    	
	}
	
	
	public String formatDeregisterReq(String name) {
		String formatted = genRqNum() + "-" + name;
		return formatted;
	}
	
	public String formatRegisterReq(String name, String ip, int port) {
		
		String formatted = genRqNum() + "-" + name + "-" + ip + "-" + port;
		return formatted;	
	}
public String formatUpdateReq(String name, String ip, int port) {
		
		String formatted = genRqNum() + "-" + name + "-" + ip + "-" + port;
		return formatted;	
	}
public String formatSubjectReq(String name, String subject) {
	
	String formatted = genRqNum() + "-" + name + "-" + subject;
	return formatted;	
}
public String formatPublishReq(String name, String subject, String text) {
	
	String formatted = genRqNum() + "-" + name + "-" + subject +"-" + text;
	return formatted;	
}
	 
	private String parseString(byte[] data, int start) { 	//function to convert byte array to string
    	
        if (data == null) return null; 
        String out = new String(); 
        for(int i=start; data[i]!=0; i++)
        	out += ((char) data[i]); 
        return out;
    }
	
	//only returning 0 - 127 so that it can be downcast to a byte. 
	private int genRqNum() {
		return RNG.nextInt(128);
	}
	
	//used to pack big ints into bytes, was used before but could still come in handy
	private byte[] packIntInBytes(int i)
	{
	  byte[] result = new byte[4];

	  result[0] = (byte) (i >> 24);
	  result[1] = (byte) (i >> 16);
	  result[2] = (byte) (i >> 8);
	  result[3] = (byte) (i);

	  return result;
	}
	
	// get an int back from bytes
    private int fromByteArray(byte[] bytes) {
         return ((bytes[0] & 0xFF) << 24) | 
                ((bytes[1] & 0xFF) << 16) | 
                ((bytes[2] & 0xFF) << 8 ) | 
                ((bytes[3] & 0xFF) << 0 );
    }
	
	
}