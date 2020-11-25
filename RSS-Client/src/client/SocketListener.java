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

import java.util.Random;

public class SocketListener extends Thread {

	private int BUFF_SIZE = 1066;	//input buffer size
	
	private byte[] inputBuffer, outputBuffer;
	private DatagramPacket dpSend, dpReceive;
	private DatagramSocket socket;
	private InetAddress server1IP, server2IP;
	private int localPort, server1Port, server2Port;
	
	ClientFileManager fm = new ClientFileManager();
	
	private Random RNG = new Random(System.currentTimeMillis());
	
	public int serverSelect = 0;	//0 means uninitialized
	public boolean awaitServerSelect = false;
	public boolean alreadyChanged = false;
	
	public int reqNum = 0;
	public boolean send_lock = true;		//block sending until initialized
	boolean awaitingResponse = false;		//only for sendTimeout
	
	Timer sendTimeout, initTimeout;
	initTimeoutTask initTask;
	sendTimeoutTask sendTask;
	
	
	public SocketListener(String inServer1IP, String inServer2IP, int inServer1Port, int inServer2Port, int inLocalPort) {
		
		//initialize members
		inputBuffer = new byte[BUFF_SIZE];
		localPort = inLocalPort;
		server1Port = inServer1Port;
		server2Port = inServer2Port;
		
		//set server IP's
		try {
			if(inServer1IP.equals("localhost")) server1IP = InetAddress.getLocalHost();
			else server1IP = InetAddress.getByName(inServer1IP);
			if(inServer2IP.equals("localhost")) server2IP = InetAddress.getLocalHost();
			else server2IP = InetAddress.getByName(inServer2IP);
		} catch (IOException e) { e.printStackTrace(); fm.log("error while resolving InerAddress");}
	
	}
	
	public void run() {
		
		//declare socket
		try { socket = new DatagramSocket(localPort); }
		catch (SocketException e) {
			e.printStackTrace();
			fm.log("SocketException while declaring datagram socket, closing socketListener"); 
			return;
		}
		
		runInit();		//server init moved to its own function so it can be reused
    	
		//loop for receiving/parsing/handling incoming data
    	while(true) {
    		
    		dpReceive = new DatagramPacket(inputBuffer,inputBuffer.length);
    			
    	    try { socket.receive(dpReceive); }			//wait till data is received
    	    catch (IOException e) { fm.log("stopping SocketListener"); return; }
    	    
    	    if(awaitingResponse) {
    	    	stopSendTimeout();	//stop timer, also resets awaitingResponse
    	    	send_lock = false;		//allow the user to send messages again
    	    }
    	    
    	    
    	    //----------------------------
    	    // client input parser/handler
    	    //----------------------------
    	    
    	    /*
    	     * 0: Test Case
    	     * 4: Registation Accepted
    	     * 5: Registration Denied
    	     * 
    	     * 50: server init
    	     * 51: server switch notification
    	     * 52: server update notification
    	     */

    	    switch(inputBuffer[0]) {
	    	
    	    case 0:	// a test case, print message to console
    	    	client_app.display("data recieved, from server" + parseString(inputBuffer, 1));
    	    	fm.log("Test case received", inputBuffer);
    	    	break;
    	    	
			case 4:
    	    	client_app.display(parseString(inputBuffer, 1));
    	    	fm.log("Registration accepted recieved", inputBuffer);
    	    	break;
    	    	
    	    case 5:
    	    	client_app.display(parseString(inputBuffer, 1));
    	    	fm.log("Registration denied recieved", inputBuffer);
    	    	break;
              
    	    case 6: //receive message
    	    	
    	    	client_app.display(parseString(inputBuffer, 1));
    	    	fm.log("Publish Message Received", inputBuffer);
    	    	break;
    	    	

    	    case 50: //server init
    	    	client_app.display("message received");
    	    	stopInitTimeout();						//stop timer on server response
				
    			//if we're here server has responded, set serverSelect
    			if(dpReceive.getPort()==server1Port) {
    			    serverSelect = 1;
    			    client_app.display("server 1 is serving");
    			} else if(dpReceive.getPort()==server2Port) {
    				serverSelect = 2;
    			    client_app.display("server 2 is serving");
    			}
    					
    			send_lock = false;	//allow sending now that we know servers are up and serverSelect is initialized
    			client_app.display("initialization complete");
    	    	break;
    	    	
    	    case 51: //server switch notification
    	    	client_app.display("received notification of server switch\n");
    	    	if(serverSelect==1) serverSelect = 2;
    	    	else serverSelect = 1;
    	    	break;
    	    	
    	    case 52: //server update notification
    	    	
    	    	//parse ip and port
    	    	String data = parseString(inputBuffer, 1);
    	    	int newServerPort = Integer.parseInt( data.substring(data.indexOf(':')+1) );
				String strIP = data.substring(0,data.indexOf(':'));
				InetAddress newServerIP;
				try {
					if(Objects.equals(strIP, "localhost")) newServerIP = InetAddress.getLocalHost();
					else newServerIP = InetAddress.getByName(strIP);
				} catch(UnknownHostException e) { fm.log("cant resolve new host ip"); break;}
				
    	    	//set ip and port
    	    	if(dpReceive.getPort()==server1Port) {
    			    server1IP = newServerIP;
    			    server1Port = newServerPort;
    			} else if(dpReceive.getPort()==server2Port) {
    				server2IP = newServerIP;
    			    server2Port = newServerPort;
    			}
    	    	break;

    	    default:
    	    	client_app.display("invalid operation recieved, initial byte out of range");
    	    	client_app.display("data recieved, from server: " + parseString(inputBuffer, 1));
    	    }
    	    
    	    
    	    
    	    inputBuffer = new byte[BUFF_SIZE]; 	// Clear the buffer after every message. 
    	}
		
	}
	
	public boolean sendString(String message, int op) {
		
		if(send_lock) {
			client_app.display("cant send message right now");
			return false;
		}
		
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
    			startSendTimeout();
    			send_lock = true;	//send data, start timeout timer
    		}catch(IOException e) { e.printStackTrace(); client_app.display("message could not be sent"); return false;}
    		
    		return true;
    	
    	} else if(serverSelect==2) {
    		
    		dpSend = new DatagramPacket(outputBuffer, outputBuffer.length, server2IP, server2Port); 	//create datagram packet 
    		try { 
    			socket.send(dpSend);
    			startSendTimeout();
    			send_lock = true;	//send data, start timeout timer
    		} catch(IOException e) { e.printStackTrace(); client_app.display("message could not be sent"); return false;}
    		
    		return true;
    		
		} else { fm.log("message could not be sent, invalid serverSelect value: "+serverSelect); return false; }
		
	}
	
	public void runInit() {
		
		client_app.display("\nrunning initialization with server");
		
		//initialize serverSelect(servers must be started before client)
		//send a ping message to both servers to see which responds
		outputBuffer = new byte[1];
		outputBuffer[0] = (byte) 50;	//ping message
		
		startInitTimeout();	//start first, otherwise we might hear back before timer is started
		
		dpSend = new DatagramPacket(outputBuffer, outputBuffer.length, server1IP, server1Port);
		try { socket.send(dpSend); }	//send data
		catch(IOException e) { e.printStackTrace(); client_app.display("message could not be sent"); }
				
		dpSend = new DatagramPacket(outputBuffer, outputBuffer.length, server2IP, server2Port);
		try { socket.send(dpSend); }	//send data
		catch(IOException e) { e.printStackTrace(); client_app.display("message could not be sent"); }
				
		client_app.display("awaiting server connection...\n");
		
		
	}
	
	
	public String formatDeregisterReq(String name) {
		String formatted = genRqNum() + "-" + name;
		return formatted;
	}
	
	public String formatRegisterReq(String name, String ip, int port) {
		
		String formatted = genRqNum() + "-" + name + "-" + ip + "-" + port;
		return formatted;
		
		
		//sendString(registerReq, 0, 1);
		
		
		
		
	}
	
	
	//the name would be replaced if some kind of login was implemented.. 
	public String formatPublishReq(String name, String subject, String text) {
		
		String formatted = genRqNum() + "-" + name + "-" + subject + "-" + text;
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
	
	//==================================================
	 // Timer Stuff
	 //==================================================
	    
	   class initTimeoutTask extends TimerTask {
		   public void run() { 
			   socket.close();
			   client_app.display("timeout expired, no server is serving");
		   }
	   }
	   
	   class sendTimeoutTask extends TimerTask {
		   public void run() {
			   client_app.display("timeout expired, no response from server about last sent message");
			   awaitingResponse = false;
			   runInit();
		   }
	   }
	   
	   
	   public void startInitTimeout() {
		   
		   if(initTimeout!=null) { initTimeout.cancel(); initTimeout.purge(); }
		   initTimeout = new Timer();
		   initTask = new initTimeoutTask();
		   initTimeout.schedule(initTask, 5000);	//5s timeout
	   	
	   }
	   
	  public void startSendTimeout() {
		  if(sendTimeout!=null) { sendTimeout.cancel(); sendTimeout.purge(); }
		  sendTimeout = new Timer();
		  sendTask = new sendTimeoutTask();
		  sendTimeout.schedule(sendTask, 5000);	//5s timeout
		  awaitingResponse = true;
	  }
	  
	  public void stopInitTimeout() {
		  if(initTimeout==null) client_app.display("cant stop initTimeout because its null");
		  else {
			  initTimeout.cancel();
			  initTimeout.purge();
		  }
	  }
	  
	  public void stopSendTimeout() {
		  if(sendTimeout==null) client_app.display("cant stop sendTimeout because its null");
		  else {
			  sendTimeout.cancel();
			  sendTimeout.purge();
		  }
		  awaitingResponse = false;
	  }
	    
	 //==================================================
	
	
}
