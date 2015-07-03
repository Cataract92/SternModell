// Nico Feld 1169233

package Topologie;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.AllPermission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import Topologie.MySP.*;

class SM_server {

	/*
	 * 
	 * CLASSES!
	 */
	
	static boolean running = true;

	static class Client {

		static HashMap<Integer,Client> allclients = new HashMap<Integer,Client>();
		static ArrayList<Integer> allids = new ArrayList<Integer>();
		static Iterator<Integer> iter;

		RecieveClientInputThread input;
		MySP mysp;
		int id;
		String name;

		public Client(String ip, int port, Socket s) {
			mysp = new MySP(s);
			input = new RecieveClientInputThread(this);
			input.start();
			int i = 0;
			while (allclients.containsKey(i)){
				i++;
			}
			id = i;
			allids.add(id);
			allclients.put(id, this);
		}

		public void disconnect() {
			ta.setText(ta.getText()+"\nDisconnected: "
					+ this.mysp.socket.getInetAddress()+" aka \""+name+"\"");
			input.interrupt();
			try {
				mysp.objIn.close();
				mysp.objOut.close();
				mysp.socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			allclients.remove(id);
			allids.remove((Integer) id);
		}
	}

	static class WaitForNewClientsThread extends Thread {

		public void run() {
			while (running) {
				Socket cs = null;
				try {
					cs = ss.accept();
				} catch (IOException e) {
					e.printStackTrace();
				}	
				Client newClient = new Client(cs.getInetAddress().toString(),
						cs.getPort(), cs);
				newClient.mysp.send(new MySP_Msg_Message(-1,"Server: Connected! Your Id is: "+newClient.id));
			}
		}
	}

	static class RecieveClientInputThread extends Thread {

		Client c;

		public RecieveClientInputThread(Client client) {
			c = client;
		}

		public void run() {
			while (running) {
				if (c.mysp.socket.isClosed())
					break;
				MySP_Message msg = c.mysp.receive();
				
				if (msg instanceof MySP_Msg_Disconnect) {
					c.disconnect();
				}

				if (msg instanceof MySP_Msg_Name) {
					c.name = ((MySP_Msg_Name)msg).s;
					ta.setText(ta.getText()+"\nConnected: " + c.mysp.serverIP+" aka \""+c.name+"\"");
				}
				
				if (msg instanceof MySP_Msg_GetIds) {
					Client.iter = Client.allids.iterator();
					String s = "\n";
					while (Client.iter.hasNext()){
						int tc = Client.iter.next();
						s = s+"Id: "+tc+" aka \""+Client.allclients.get((Integer)tc).name+"\"\n";
					}
					c.mysp.send(new MySP_Msg_Message(-1,s));
				}
				
				if(msg instanceof MySP_Msg_Message){
					int id = ((MySP_Msg_Message) msg).id;
					String s =  ((MySP_Msg_Message) msg).msg;
					Client cl = Client.allclients.get(id);
					cl.mysp.send(new MySP_Msg_Message(-1,"Id "+c.id +" aka \""+c.name +"\" sent: "+s));
					ta.setText(ta.getText()+"\nId "+c.id+" sent to Id "+id+": "+s);
				}
			}
		}
	}

	static ServerSocket ss;
	static JFrame wnd;
	static JTextArea ta;

	public static void main(String[] argv) throws IOException {
		wnd = new JFrame("SternServer");
        wnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        wnd.setSize(200, 300);

        Container cp = wnd.getContentPane();
        //cp.setLayout(new BorderLayout());

        ta = new JTextArea(20,30); 
        ta.setText("Server started!");
        cp.add(ta);
        
        wnd.pack();
        wnd.setLocationRelativeTo(null);
        wnd.setVisible(true);
		ss = new ServerSocket(3000);
		WaitForNewClientsThread wt = new WaitForNewClientsThread();
		wt.start();
	}

}