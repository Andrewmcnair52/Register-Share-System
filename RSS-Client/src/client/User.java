package client;

import java.util.Vector;

public class User extends app {
	private String name = "Unknown User";
	private String ipAddress = "0.0.0.0";
	private int socketNumber = 0;
	private Vector<String> subjectsOfInterest = new Vector<String>();

	User(String _name, String _ip_address, int _socket_number) {
		setName(_name);
		setIp_address(_ip_address);
		setSocketNumber(_socket_number);
	}

	public String getIp_address() {
		return ipAddress;
	}

	public void setIp_address(String _ip_address) {
		if (this.ipAddress == "0.0.0.0") {
			this.ipAddress = _ip_address;
		} else if (this.ipAddress != _ip_address) {
			System.out.println(
					this.name + " has changed his/her IP Address from " + this.ipAddress + " to " + _ip_address);
		}

	}

	public String getName() {
		return name;
	}

	public void setName(String _name) {
		if (this.name == "Unknown User") {
			this.name = _name;
		} else if (this.name != _name) {
			System.out.println(this.name + " has changed his/her name to " + _name);
		}

	}

	public int getSocketNumber() {
		return socketNumber;
	}

	public void setSocketNumber(int _socket_number) {
		if (this.socketNumber == 0) {
			this.socketNumber = _socket_number;
		} else if (this.socketNumber != _socket_number) {
			System.out.println(
					this.name + " has changed his/her socket number from " + socketNumber + " to " + _socket_number);
		}

	}

	// This function needed to be clarified later on
	public void getSubjectOfInterest() {
		// Output the present vector
		System.out.println(this.name + " subjects of interest are: " + this.subjectsOfInterest);
		// return this.subjectsOfInterest;
	}

	// This function needed to be clarified later on
	public void register(String _subject) {
		subjectsOfInterest.add(_subject);
		// Output the present vector
		System.out.println(this.name + " has registered to the subject \"" + _subject + "\"");
	}

	// This function needed to be clarified later on
	public void deregister(String _subject) {
		if (subjectsOfInterest.contains(_subject)) {
			subjectsOfInterest.remove(_subject);
			System.out.println(this.name + " has deregistered from the subject\" " + _subject + "\"");
		} else
			System.out.println(
					"There is no such a subject as \" " + _subject + "\" in " + this.name + "'s list of interest");
	}

	public void displayUserInformation() {
		System.out.println("\nName: " + this.name);
		System.out.println("IP Address: " + this.ipAddress);
		System.out.println("Socket number: " + this.socketNumber);
		this.getSubjectOfInterest();
	}
}
