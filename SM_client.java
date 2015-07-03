package Topologie;

// Nico Feld 1169233

import javax.swing.*;

import Topologie.MySP.*;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.StringTokenizer;

class SM_client {

	static boolean running = true;
	static boolean connected = false;
	static String name;

	static MySP mysp;

	static Socket socket = null;

	static JFrame wnd;
	static JTextArea ta;
	static JTextField tf;

	public static void main(String[] argv) {
		System.out.println("Client started");
		wnd = new JFrame("SternClient");
		wnd.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		wnd.setSize(200, 300);

		Container cp = wnd.getContentPane();
		cp.setLayout(new BorderLayout());

		tf = new JTextField();
		tf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StringTokenizer t = new StringTokenizer(tf.getText(), " ");
				switch (t.nextToken()) {
				case "name": {
					if (connected) {
						ta.setText(ta.getText() + "\nDisconnect first!");
					} else {
						name = t.nextToken();
						ta.setText(ta.getText()+"\nYour name is set to \""+name+"\"");
					}
					tf.setText("");
					return;
				}
				case "send": {
					if (connected) {
						String ip = t.nextToken();
						String msg = t.nextToken();
						while (t.hasMoreTokens()) {
							msg = msg + " " + t.nextToken();
						}
						ta.setText(ta.getText() + "\nSent \"" + msg + "\" to "
								+ ip);
						mysp.send(new MySP_Msg_Message(Integer.parseInt(ip),
								msg));
					} else {
						ta.setText(ta.getText()
								+ "\nConnect to a Server first!");
					}
					tf.setText("");
					return;
				}
				case "connect": {
					if (name == null) {
						ta.setText(ta.getText()
								+ "\nNo Name set yet. Set name with \"name {Name}\"");
						tf.setText("");
						return;
					}

					if (connected)
						disconnect();
					try {
						socket = new Socket(t.nextToken(), 3000);
					} catch (IOException ec) {
						ec.printStackTrace();
						ta.setText(ta.getText() + "\nCould not connect!");
					}
					mysp = new MySP(socket);
					mysp.send(new MySP_Msg_Name(name));
					connected = true;
					tf.setText("");
					return;
				}
				case "disconnect": {
					disconnect();
					tf.setText("");
					return;
				}
				case "quit": {
					if (connected)
						disconnect();
					System.exit(0);
				}
				case "getAllIds": {
					mysp.send(new MySP_Msg_GetIds());
					tf.setText("");
					return;
				}
				}
				tf.setText("");
			}
		});
		cp.add(tf, BorderLayout.NORTH);

		ta = new JTextArea(20, 30);
		ta.setText("Client started!");
		cp.add(ta, BorderLayout.SOUTH);

		wnd.pack();
		wnd.setLocationRelativeTo(null);
		wnd.setVisible(true);

		while (running) {
			System.out.print("");
			if (connected) {
				MySP_Message msg = mysp.receive();
				if (msg instanceof MySP_Msg_Message) {
					ta.setText(ta.getText() + "\n"
							+ ((MySP_Msg_Message) msg).msg);
				}
			}
		}

	}

	static void disconnect() {
		ta.setText(ta.getText() + "\nDisconnected!");
		connected = false;
		mysp.send(new MySP_Msg_Disconnect());
	}
}