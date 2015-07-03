// Nico Feld 1169233

package Topologie;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

public class MySP {

	static abstract class MySP_Message implements Serializable {
		private static final long serialVersionUID = 1L;

	}
	
	static class MySP_Msg_Message extends MySP_Message{
		int id;
		String msg;
		
		public MySP_Msg_Message(int i, String s) {
			id = i;
			msg = s;
		}
		
	}

	static class MySP_Msg_Disconnect extends MySP_Message {
		private static final long serialVersionUID = 1L;
	}
	
	static class MySP_Msg_GetIds extends MySP_Message{
		
	}
	
	static class MySP_Msg_Name extends MySP_Message{
		String s;
		public MySP_Msg_Name(String name) {
			s = name;
		}
	}

	ObjectOutputStream objOut;
	ObjectInputStream objIn;
	Socket socket;
	String serverIP;
	int serverPort;

	MySP(Socket s) {
		serverIP = s.getInetAddress().toString();
		serverPort = s.getPort();
		socket = s;
		try {
			objOut = new ObjectOutputStream(socket.getOutputStream());
			objIn = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.out.println("Could not open IO streams for server");
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}

	MySP_Message receive() {
		try {
			return (MySP_Message) objIn.readObject();
		} catch (ClassNotFoundException | IOException e) {
			return null;
		}
	}

	void send(MySP_Message km) {
		try {
			objOut.writeObject(km);
			objOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
