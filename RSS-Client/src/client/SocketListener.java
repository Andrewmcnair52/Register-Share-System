package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class SocketListener extends Thread {

	private int BUFF_SIZE = 1066;	//input buffer size
	
	private byte[] inputBuffer, outputBuffer;
	private DatagramPacket dpSend, dpReceive;
	private DatagramSocket socket;
	private InetAddress server1IP, server2IP;
	private int localPort, server1Port, server2Port;
	
	public int serverSelect = 0;	//0 means uninitialized
	public boolean awaitServerSelect = false;
	public boolean alreadyChanged = false;
	
	public int reqNum = 0;
	
	Timer serverSwitchTimer = new Timer();
	TimerExec serverSwitchExec = new TimerExec();
	
	public SocketListener(String inServer1IP, String inServer2IP, int inServer1Port, int inServer2Port, int inLocalPort) {
		
		//initialize members
		inputBuffer = new byte[BUFF_SIZE];
		localPort = inLocalPort;
		server1Port = inServer1Port;
		server2Port = inServer2Port;
		
		//declare socket
		try { socket = new DatagramSocket(localPort); }
		catch (SocketException e) { e.printStackTrace(); System.out.println("SocketException while declaring datagram socket"); }
		
		//set server IP's
		try {
			if(inServer1IP.equals("localhost")) server1IP = InetAddress.getLocalHost();
			else server1IP = InetAddress.getByName(inServer1IP);
			if(inServer2IP.equals("localhost")) server2IP = InetAddress.getLocalHost();
			else server2IP = InetAddress.getByName(inServer2IP);
		} catch (IOException e) { e.printStackTrace(); System.out.println("error while resolving InerAddress");}
	
	}
	
	public void run() {
		
		//initialize serverSelect(servers must be started before client)
		//send a ping message to both servers to see which responds
		outputBuffer = new byte[1];
		outputBuffer[0] = (byte) 20;	//ping message
		
		dpSend = new DatagramPacket(outputBuffer, outputBuffer.length, server1IP, server1Port);
		try { socket.send(dpSend); }	//send data
	    catch(IOException e) { e.printStackTrace(); client_app.display("message could not be sent"); }
		
		dpSend = new DatagramPacket(outputBuffer, outputBuffer.length, server2IP, server2Port);
		try { socket.send(dpSend); startTimer(5000); }	//send data
	    catch(IOException e) { e.printStackTrace(); client_app.display("message could not be sent"); }
		
		client_app.display("awaiting server connection...");
		resetTimer();
		startTimer(10000);	//set a 10s timeout
		
		dpReceive = new DatagramPacket(inputBuffer,inputBuffer.length);
		
		try { 
			
			socket.receive(dpReceive);			//await for response
			resetTimer();						//stop timer on server response
			
			//if we're here server has responded, set serverSelect
			if(dpReceive.getPort()==client_app.server1Port) {
	    		serverSelect = 1;
	    		client_app.display("server 1 is serving");
			} else if(dpReceive.getPort()==client_app.server2Port) {
	    		serverSelect = 2;
	    		client_app.display("server 2 is serving");
			}
			
			client_app.blockSending = false;	//allow sending now that we know servers are up and serverSelect is initialized
		
		} catch (IOException e) { 
			System.out.println("socketException while recieving data");
			return;
		}
		
    	
		//loop for receiving/parsing/handling incoming data
    	while(true) {
    		
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
	
	public void sendString(String message, int op) {
		
		//convert string to byte array
		byte[] tmpBuff = message.getBytes();		//get message as a byte array
		outputBuffer = new byte[tmpBuff.length+1];
		outputBuffer[0] = (byte) op;				//append a zero to beginning for server side command handler
		for(int i=0; i<tmpBuff.length; i++)			//copy message byte array to output buffer
			outputBuffer[i+1] = tmpBuff[i]; 
    	
		if(serverSelect==1) {
    		
    		dpSend = new DatagramPacket(outputBuffer, outputBuffer.length, server1IP, server1Port); 	//create datagram packet
    		try { 
    			socket.send(dpSend); 
    			resetTimer(); startTimer(5000);
    			client_app.blockSending=true;	//send data, start timeout timer
    		}catch(IOException e) { e.printStackTrace(); client_app.display("message could not be sent"); }
    	
    	} else if(serverSelect==2) {
    		
    		dpSend = new DatagramPacket(outputBuffer, outputBuffer.length, server2IP, server2Port); 	//create datagram packet 
    		try { 
    			socket.send(dpSend);
    			startTimer(5000);
    			client_app.blockSending=true;	//send data, start timeout timer
    		} catch(IOException e) { e.printStackTrace(); client_app.display("message could not be sent"); }
    		
		} else { System.out.println("message could not be sent, invalid serverSelect value: "+serverSelect); return; }
		
	}
	
	
	public void formatRegisterReq() {
		int regId = 0; // we will have to give all requests codes
		int rqNum = 1; //whats this?
		String name = "frank";
		String ip = "192.168.1.1"; // dummy address, will all be local host no?
		int socket = localPort; //socket = port?
		
		String registerReq = regId + rqNum + name + ip + socket;
		
		
		//sendString(registerReq, 0, 1);
		
		
		
		
	}
	
	 
	String parseString(byte[] data, int start) { 	//function to convert byte array to string
    	
        if (data == null) return null; 
        String out = new String(); 
        for(int i=start; data[i]!=0; i++)
        	out += ((char) data[i]); 
        return out; 
    }
	
	//==================================================
	 // Timer Stuff
	 //==================================================
	    
	   class TimerExec extends TimerTask {
		   public void run() { 
			   System.out.println("timer triggered, switching servers");
			   
			   //if timer times out assume servers have switched and change serverSelect
			   if(serverSelect==0) {
				   //both servers were sent a message and neither responded to change serverSelect
				   //therefore either servers are down or neither are serving
				   socket.close();
				  client_app.display("timeout expired, no server is serving");
			   } else if(serverSelect==1) {
				   //server 1 has timed out assume server switch
			   }
			   
			   
		   }
	   }
	   
	   public void resetTimer() {
		   
		   serverSwitchTimer.cancel();
		   serverSwitchTimer.purge();
		   serverSwitchTimer = new Timer();
		   serverSwitchExec = new TimerExec();
	   }
	   
	   public void startTimer(int time) {
		   
		   //schedule(TimerTask task, Date time)
		   // task: task to run
		   // time: time to wait before running task in milliseconds
		   serverSwitchTimer.schedule(serverSwitchExec, time);
	   	
	   }
	    
	 //==================================================
	
	
}
