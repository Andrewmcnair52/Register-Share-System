package Server;

import java.util.Random;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;


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


	public FileManager fm;

	ArrayList<User> registeredUsers = new ArrayList<User>();
	//ArrayList<String> interestedUsers = new ArrayList<String>();
	ArrayList<Subject> subjects = new ArrayList<Subject>();

	private Semaphore serverSwitchLock = new Semaphore(1);

	public UDPServer(int localPort, int serverNum) {

		inputBuffer = new byte[BUFF_SIZE];
		fm = new FileManager(serverNum, this);
		
		this.subjects = fm.loadSubjectList();
		this.registeredUsers = fm.loadUserList();

		try { serverSocket = new DatagramSocket(localPort); }	//create datagram socket and bind to port
		catch (SocketException e) { 
			e.printStackTrace(); 
			fm.log("socketException while creating DatagramSocket");
		}

	}

	@Override
	public void run() {


		while(true) {	//loop for receiving/parsing/handling incoming data

			dpReceive = new DatagramPacket(inputBuffer,inputBuffer.length);

			try { serverSocket.receive(dpReceive); }			//wait till data is received
			catch (IOException e) { System.out.println("socketException while recieving data, closing UDPServer"); return;}

			//----------------------------
			// server input parser/handler
			//----------------------------

			/*	0: test case
			 * 	1: registration request
			 *  2: deregister
			 *  3: update ip and socket
			 *  4: update subjects of interest
			 *  5: publish
			 *  50: client serverSelect ping
			 *  51: other server is requesting to update
			 *  52: server update request response
			 *  53: other server done updating
			 *  99: server update sync
			 * 	100: server sync
			 * 	101: server sync confirmation
			 * 	102: server switch
			 *  103: registration on other server
			 *  104: dereg on other server
			 *  105: update subjects on other server
			 *  106; failed register on other server
			 *  107; update user on other server
			 */

			//note: we cast the op codes to bytes when we send them, meaning we can only use op codes in the range of [-128, 127]


			if(isServing)				//socket input handlers which run while serving
				switch(inputBuffer[0]) {

				case 0:	// a test case, print message to console, and respond with 'message received'
					fm.log("Test Case Received", inputBuffer);
					fm.log("data recieved from client: "+parseString(inputBuffer,1));	//convert data to string, then send to main for displaying
					sendString("message recieved", 0, dpReceive.getAddress(), dpReceive.getPort());	//send response
					break;

				case 1: //registration request
					fm.log("Register Received", inputBuffer);
					String regReq = parseString(inputBuffer, 1);

					String[] reqSplit = regReq.split("-");

					User newUser = new User(reqSplit[1], reqSplit[2], Integer.parseInt(reqSplit[3]));

					int regStatus = checkUser(newUser);

					//okay register them
					if (regStatus == 0) {

						registeredUsers.add(newUser);

						//and save them to the file

						fm.updateUserList(registeredUsers);
						//subjectsFile.updateSubjects(subjects);

						//if youre the serving server send other server message and then they should do the save as well. 
						byte[] copiedRegister = Arrays.copyOf(inputBuffer, 56);
						copiedRegister[0] = 103;
						fm.log("Sending Registration Notice", copiedRegister);
						sendServer(copiedRegister);

						//then respond to user about what has happened
						sendString("You have been registered RQ#: " + reqSplit[0], 1, dpReceive.getAddress(), dpReceive.getPort());
					}

					//TODO: tell other server about failed registration
					else if (regStatus == 1) {
						//username in use
						sendString("RQ#: " + reqSplit[0] + ": Username taken", 2, dpReceive.getAddress(), dpReceive.getPort());
						
						String failMes = "Received invalid registration request: Username \"" + newUser.getName() + "\" already in use";
						byte[] failArr = failMes.getBytes(); 
						byte[] failwop = new byte[failArr.length+1];
						failwop[0] = 106;
						System.arraycopy(failArr, 0, failwop, 1, failArr.length);
						sendServer(failwop);
						fm.log(failMes);
					} else if (regStatus == 2) {
						//ip and port in use
						String failMes = "Received invalid registration request: IP/PORT combination: " + newUser.getIp() + ":" + newUser.getSocket() + " already in use";
						byte[] failArr = failMes.getBytes(); 
						byte[] failwop = new byte[failArr.length+1];
						failwop[0] = 106;
						System.arraycopy(failArr, 0, failwop, 1, failArr.length);
						sendServer(failwop);
						sendString("RQ#: " + reqSplit[0] + ": ip/port combo taken", 2, dpReceive.getAddress(), dpReceive.getPort());
						fm.log(failMes);
					}

					break;

				case 2: //unregister
					fm.log("Unregister Received", inputBuffer);
					//get rid of op code and the single - that follows
					String deregUserReq = parseString(inputBuffer, 1);

					//now split out name

					String[] splitReq = deregUserReq.split("-");

					for (int i = 0; i < registeredUsers.size(); i++) {
						if(registeredUsers.get(i).getName().equals(splitReq[1])) {
							registeredUsers.remove(i);

							//update file
							fm.updateUserList(registeredUsers);

							//tell other server about dereg
							fm.log("Sending deregistration notice");
							sendServer(splitReq[1], 104);
							
							//remove them from all subjects
							ArrayList<Integer> subjectsYouIn = new ArrayList<>();
							
							for(int k = 0; k < subjects.size(); k++) {
								for(int j = 0; j < subjects.get(k).getUsers().size(); j++) {
									if (subjects.get(k).getUsers().get(j).equals(splitReq[1])) {
										subjectsYouIn.add(k);
										break;
									}
								}
							}
							//TODO: also remove them from the other servers subject list
							for(Integer ind : subjectsYouIn ) {
								subjects.get(ind).removeUser(splitReq[1]);
							}
							fm.updateSubjects(subjects);

							sendString("You've been deregistered!", 3, dpReceive.getAddress(), dpReceive.getPort());
							break;
						}
					}
					break;
				case 3: //update
					boolean found = false;
					String updateUserReq = parseString(inputBuffer, 1);
					String[] splitUpdateReq = updateUserReq.split("-");
					for (int i = 0; i < registeredUsers.size(); i++) {
						if(registeredUsers.get(i).getName().equals(splitUpdateReq[1])) {
							registeredUsers.get(i).setIp(splitUpdateReq[2]);
							registeredUsers.get(i).setSocket(Integer.parseInt(splitUpdateReq[3]));
							sendString("RQ#: " + splitUpdateReq[0] + ": "+"Update confirmed ", 4, dpReceive.getAddress(), dpReceive.getPort());
							fm.updateUserList(registeredUsers); //update file
							found = true;
							
							//inform other server
							byte[] forOtherServer = Arrays.copyOf(inputBuffer, inputBuffer.length);
							forOtherServer[0] = 107;
							sendServer(forOtherServer);
							
							break;
						}
					}
					if (!found) {
						sendString("RQ#: " + splitUpdateReq[0] + ": "+"The user does not exist ", 5, dpReceive.getAddress(), dpReceive.getPort());
					}
					break;

				case 4:
					fm.log("Update Subjects Received ", inputBuffer);
					
					boolean success = updateSubjects(inputBuffer);
					byte[] userMessage = Arrays.copyOfRange(inputBuffer, 1, inputBuffer.length);
					String message = new String(userMessage);
					if(success) {
						fm.updateSubjects(subjects);
						//first send to other server what happened
						//subjects-updated - rq - name - list of subjects
						
						byte[] otherServerMessage = Arrays.copyOf(inputBuffer, inputBuffer.length);
						otherServerMessage[0] = 105;
						sendServer(otherServerMessage);

						//send a confirmation back to the user
						sendString(message, 7, dpReceive.getAddress(), dpReceive.getPort());
					}
					
					else {
						//return the problem message to the user
						fm.log("Either bad user or bad subject ", inputBuffer);
						sendString(message + "Bad User or Subject", 8, dpReceive.getAddress(), dpReceive.getPort());
					}



					break;

				case 5: //publish

					fm.log("Publish Request Received", inputBuffer);

					//first disect message
					String pReq = parseString(inputBuffer, 1);

					String[] pReqSplit = pReq.split("-");

					//check to see if interest exists
					//and save it

					ArrayList<String> users = null;

					boolean subjectExist = false;
					int subject = -1;

					for (int i = 0; i < subjects.size(); i++) {
						if (subjects.get(i).getName().equals(pReqSplit[2])) {
							subjectExist = true;
							subject = i;
							break;
						}
					}

					boolean userHasSub = false;

					if (subjectExist) {
						users = subjects.get(subject).getUsers();
						for (String user : users) {
							if (user.equals(pReqSplit[1])) {
								userHasSub = true;
								break;
							}
						}
					} else {
						fm.log("Publish Request Received for non-existant subject: Rejected");
						sendString(pReqSplit[0], 9, dpReceive.getAddress(), dpReceive.getPort());
					}

					//collect their infoooooo... this merits a little thought i guessss...
					// so in retro, it would have been better for subjects to store users as a whole
					// cause now i got to go through the user list to find their addresses... which is like an m * n problem... almost like O(n^2)..
					// would rather be a little space inefficient.. will be an easy fix

					ArrayList<User> usersToSendTo = new ArrayList<>();


					//also this sends back to the sending user but fuck it ill fix it later when i redo this shit
					if (subjectExist && userHasSub) {
						for(int i = 0; i < users.size(); i++) {
							String check = users.get(i);
							for (int j = 0; j < registeredUsers.size(); j++) {
								if(check.equals(registeredUsers.get(j).getName())){
									usersToSendTo.add(registeredUsers.get(j));
								}
							}
						}

						//now send the messages
						for(User u : usersToSendTo) {
							InetAddress uAd = null;
							
							try {
								uAd = InetAddress.getByName(u.getIp());
							} catch (UnknownHostException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							fm.log("Publishing message", pReqSplit[0] + "-" + pReqSplit[1] + "-" + pReqSplit[3]);
							sendString(pReqSplit[0] + "-" + pReqSplit[1] + "-" + pReqSplit[2] + "-" + pReqSplit[3], 6, uAd, u.getSocket() );
						}

					}

					break;

				case 50: //client serverSelect ping
					sendString("pong", 50, dpReceive.getAddress(), dpReceive.getPort());
					break;


				case 51: //other server is requesting to update
					try { serverSwitchLock.acquire(); } catch (InterruptedException e) { e.printStackTrace(); }	//aquire permit to lock server switch
					if(isServing) {
						sendServer("accepted",52);	//if permit obtained, and still serving, success, server will not switch until release
						fm.log("received update server request, accepted, server switch is locked");
					}
					else { 	//if not serving, server switch occured, other server is now serving, deny request
						sendServer("denied",52);
						serverSwitchLock.release();	
						fm.log("received update server request, denied, server switch happened first");
					}

					break;

				case 53: //other server done updating

					//parse ip and port
					String data = parseString(inputBuffer, 1);
					int newServerPort = Integer.parseInt( data.substring(data.indexOf(':')+1) );
					String strIP = data.substring(0,data.indexOf(':'));
					InetAddress newServerIP;
					try {
						if(Objects.equals(strIP, "localhost")) newServerIP = InetAddress.getLocalHost();
						else newServerIP = InetAddress.getByName(strIP);
					} catch(UnknownHostException e) { fm.log("cant resolve new host ip"); break;}

					//update this servers record of other server
					otherServerIP = newServerIP;
					otherServerPort = newServerPort;

					//other server needs a sync to get this servers info
					sendServer("", 99);

					fm.log("server update done, server switch unlocked");
					serverSwitchLock.release();	//release lock allowing server switch to happen
					break;


				default:
					break;


				}

			switch(inputBuffer[0]) {	//socket input handler that runs even when not serving


			case 52: //update request response

				if(Objects.equals(parseString(inputBuffer,1), "accepted")) {
					System.out.println("update server request: accepted");
					sendAllClients(server_app.newServerIP+":"+server_app.newServerPort, 52);	//send server update notification to clients
					sendServer(server_app.newServerIP+":"+server_app.newServerPort,53);			//send server new ip and port so it can resync
					System.out.println("other server is now communicating with: "+server_app.newServerIP+":"+server_app.newServerPort);
					dualServerSync = false;
				} else {
					System.out.println("update server request: denied");
					if(isServing) System.out.println("this server is now serving");
				}
				synchronized(this) { notify(); }
				break;

			case 99: 	//server update sync
				fm.log("Server Sync Received", inputBuffer);
				otherServerIP = dpReceive.getAddress();
				otherServerPort = dpReceive.getPort();
				dualServerSync = true;	//set sync to true, leave isServing off since other server is serving
				serverSwitchTimer = new Timer();		//initialize servers timer
				serverSwitchExec = new TimerExec();		//initialize servers timerTask
				break;

			case 100:	//server sync: 
				fm.log("Server Sync Received", inputBuffer);

				//save info for other server
				otherServerIP = dpReceive.getAddress();
				otherServerPort = dpReceive.getPort();


				if(Objects.equals(parseString(inputBuffer,1),"s1")) {	//we are server 1
					dualServerSync = true;	//set server as sync'd
					isServing = true;		//server 1 serves first
					fm.log("Sending sync to other server");
					sendServer("s2",100);	//respond to sync other server
				} else {	//we are server 2
					dualServerSync = true;	//set server as sync'd but do not set isServing
					fm.log("Sending sync confirmation");
					sendServer("",101);	//respond with sync confirmation to start server1's timer
					serverSwitchTimer = new Timer();		//initialize server 2's timer
					serverSwitchExec = new TimerExec();		//initialize server2's timerTask
				}

				if(isServing) fm.log("this server is serving");
				else fm.log("this server is not serving");
				break;

			case 101: //server sync confirmation
				fm.log("Server Sync Confirmation Received", inputBuffer);
				//if we're here, then both servers are now sync'd and this is server 1
				//start serverSwitchTimer
				startTimer();
				break;

			case 102: //server switch
				fm.log("Server Switch Received", inputBuffer);
				//other server has notified us that of a server switch
				isServing = true;	//start serving
				startTimer();		//start timer for next server switch
				break;

			case 103: //registration has occured on other server
				fm.log("Registration on other Server Received", inputBuffer);

				String regReq = parseString(inputBuffer, 1);

				String[] reqSplit = regReq.split("-");

				User newUser = new User(reqSplit[1], reqSplit[2], Integer.parseInt(reqSplit[3]));

				registeredUsers.add(newUser);
				fm.updateUserList(registeredUsers);
				break;

			case 104: //deregister on other server
				fm.log("Unregister on other server Received", inputBuffer);

				String deregUser = parseString(inputBuffer, 1);
				
				for (int i = 0; i < registeredUsers.size(); i++) {
					if(registeredUsers.get(i).getName().equals(deregUser)) {
						registeredUsers.remove(i);

						//update file
						fm.updateUserList(registeredUsers);

						//remove them from all subjects
						ArrayList<Integer> subjectsYouIn = new ArrayList<>();
						
						for(int k = 0; k < subjects.size(); k++) {
							for(int j = 0; j < subjects.get(k).getUsers().size(); j++) {
								if (subjects.get(k).getUsers().get(j).equals(deregUser)) {
									subjectsYouIn.add(k);
									break;
								}
							}
						}
						
						for(Integer ind : subjectsYouIn ) {
							subjects.get(ind).removeUser(deregUser);
						}
						fm.updateSubjects(subjects);
						break;
					}
				}
				
				break;

			case 105: //update subjects on other server
				fm.log("Update subjects on other server Received", inputBuffer);
				updateSubjects(inputBuffer);
				fm.updateSubjects(subjects);
				break;
				
			case 106: //reg fail on other server
				fm.log("Message from other server", inputBuffer);
				break;
			
			
			case 107: //update on other server
				String updateUserReq = parseString(inputBuffer, 1);
				String[] splitUpdateReq = updateUserReq.split("-");
				for (int i = 0; i < registeredUsers.size(); i++) {
					if(registeredUsers.get(i).getName().equals(splitUpdateReq[1])) {
						registeredUsers.get(i).setIp(splitUpdateReq[2]);
						registeredUsers.get(i).setSocket(Integer.parseInt(splitUpdateReq[3]));
						sendString("RQ#: " + splitUpdateReq[0] + ": "+"Update confirmed ", 0, dpReceive.getAddress(), dpReceive.getPort());
						fm.updateUserList(registeredUsers); //update file
						break;
					}
				}
				
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
		catch(IOException e) { e.printStackTrace(); fm.log("message could not be sent"); }

	}

	public void sendAllClients(String message, int op) {	//send to all registered clients
		Iterator<User> it = registeredUsers.iterator();
		while(it.hasNext()) { 
			User tmpUser = it.next();
			try {
				if(tmpUser.getIp().equals("localhost")) {
					sendString(message, op, InetAddress.getLocalHost(), tmpUser.getSocket());
				} else {
					sendString(message, op, InetAddress.getByName(tmpUser.getIp()), tmpUser.getSocket());
				}
			} catch(UnknownHostException e) { 
				fm.log("unknown host exception while sending to all clients, cannot parse ip: "+tmpUser.getIp());
			}
		}
	}

	//sends a string with message code, to the other server
	public void sendServer(String message, int op) {

		//convert string to byte array
		byte[] tmpBuff = message.getBytes();		//get message as a byte array
		outputBuffer = new byte[tmpBuff.length+1];
		outputBuffer[0] = (byte) op;				//append op to beginning for server side command handler
		for(int i=0; i<tmpBuff.length; i++)			//copy message byte array to output buffer
			outputBuffer[i+1] = tmpBuff[i];

		dpSend = new DatagramPacket(outputBuffer, outputBuffer.length, otherServerIP, otherServerPort); 	//create datagram packet 

		try { serverSocket.send(dpSend); }	//send data
		catch(IOException e) { e.printStackTrace(); fm.log("message could not be sent"); }

	}

	//send other server a byte array
	public void sendServer(byte[] toSend) {
		dpSend = new DatagramPacket(toSend, toSend.length, otherServerIP, otherServerPort); 	//create datagram packet 

		try { serverSocket.send(dpSend); }	//send data
		catch(IOException e) { e.printStackTrace(); fm.log("message could not be sent"); }
	}

	private boolean updateSubjects(byte[] req) {
		

		
		boolean userExists=false;
		ArrayList<String> listOfNewSubjects = new ArrayList<String>();
		String updateSubjectReq = parseString(inputBuffer, 1);
		String[] splitSubjectReq = updateSubjectReq.split("-");
		
		//does subject exist already? who fucking cares, i only care if hes already in it! 
		
		//checks if the user name exists in the list of registered users
		for (int i = 0; i < registeredUsers.size(); i++) {
			if(registeredUsers.get(i).getName().equals(splitSubjectReq[1])) {
				userExists = true;
				break;}
		}
		if (!userExists) {
			return false;
		}
		// adding the list of objects given from the user
		for (int i = 2; i<splitSubjectReq.length;i++) {
			listOfNewSubjects.add(splitSubjectReq[i]);
		}

		if (userExists) {
			boolean subExists = false;
			for (String newName : listOfNewSubjects) {	
				subExists = false;
				for (int i = 0; i < subjects.size(); i++) {
					if (newName.equals(subjects.get(i).getName())) {
						//this is the case if the subject already exists
						
						//does the user already have this subject?
						for(String un : subjects.get(i).users) {
							if (un.equals(splitSubjectReq[1])) {
								return false;
							}
						}
						
						subjects.get(i).addUser(splitSubjectReq[1]);
						subExists = true;
						break;
					}
				}
				if(!subExists) {
					Subject newSub = new Subject(newName);
					newSub.addUser(splitSubjectReq[1]);
					subjects.add(newSub);
				}
			}
		}
		return true;
	}


	//==================================================
	// Timer Stuff
	//==================================================

	class TimerExec extends TimerTask {	//runs when server switch timer triggers
		@Override
		public void run() { 

			try { serverSwitchLock.acquire(); } catch (InterruptedException e) { e.printStackTrace(); }	//if locked, wait until unlocked

			fm.log("timer triggered, this server is no longer serving serving");
			sendServer("",102);		//notify other server first
			sendAllClients("", 51);	//notify registered users of server switch
			isServing = false;		//stop serving

			serverSwitchLock.release();

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
		//int val = 20000 + rand.nextInt(2000);	//20-22 seconds, left here for debugging purposes
		fm.log("this server is now serving, next server switch in: " + val/1000 + "s");

		//schedule(TimerTask task, Date time)
		// task: task to run
		// time: time to wait before running task in milliseconds
		serverSwitchTimer.schedule(serverSwitchExec, val);

	}
	public String displayTime(int v) {
		int s = v/1000;
		int seconds = s%60;
		int h = s/60;
		int minutes = h%60;
		int hours = h/60;	

		return (hours+"h"+minutes+"m"+seconds+"s");
	}

	//==================================================




	private int checkUser(User user) {


		for(User u : registeredUsers) {
			if(u.getName().equals(user.getName())) {
				return 1; //username already exists
			}
			else if (u.getIp().equals(user.getIp()) && u.getSocket() == user.getSocket()) {
				return 2; //ip and socket already registered
			}
		}

		return 0; //no registered user
	}



}