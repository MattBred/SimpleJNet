import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.json.simple.JSONObject;

public class SimpleJNet_Example {

	private static SimpleJNetServerWrapper serverWrapper;
	private static SimpleJNetClientWrapper clientWrapper;
	private static JButton connectClientButton;
	private static JButton connectServerButton;
	private static JLabel statusClient;
	private static JLabel clientsConnected;
	private static JLabel statusServer;
	private static JLabel messageClientTitle;
	private static JLabel messageServerTitle;
	
	
	public static void main(String[] args) {
		new SimpleJNet_Example();	
	}


	
	public SimpleJNet_Example() {
		serverWrapper = new SimpleJNetServerWrapper();
		serverWrapper.setPort(8000);
		
		clientWrapper = new SimpleJNetClientWrapper();
		clientWrapper.setServerAddress("localhost");
		clientWrapper.setPort(8000);
		
		loadGUI();	
	}
	
	private void loadGUI() {
		/*****************************
		 * Main GUI Frame
		 *****************************/
		JFrame guiFrame = new JFrame(); //The main application
		guiFrame.setTitle("SimpleJNet Example");
		guiFrame.setSize(600, 400);
		guiFrame.setLocation(600, 400);
		guiFrame.addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        } );
		
		/*****************************
		 * Server GUI Stuff
		 *****************************/
		JPanel lPanel = new JPanel();
		lPanel.setLayout(new GridLayout(8,1));
		
		statusServer = new JLabel("Server Status: Disconnected");
		statusServer.setHorizontalAlignment(SwingConstants.CENTER);
		lPanel.add(statusServer);
		
		clientsConnected = new JLabel("Clients Connected: 0");
		clientsConnected.setHorizontalAlignment(SwingConstants.CENTER);
		lPanel.add(clientsConnected);
		
		connectServerButton = new JButton("Connect Server");
		connectServerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				serverConnectButton();
			}
		});
		lPanel.add(connectServerButton);
		
		JButton messageServerButton = new JButton("Send Message To Client");
		messageServerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				serverSendMessageButton();
			}
		});
		lPanel.add(messageServerButton);
		
		messageServerTitle = new JLabel("No Message Received");
		lPanel.add(messageServerTitle);
		
		
		/*****************************
		 * Client GUI Stuff
		 *****************************/
		JPanel rPanel = new JPanel();
		rPanel.setLayout(new GridLayout(8,1));
		
		statusClient = new JLabel("Client Status: Disconnected");
		statusClient.setHorizontalAlignment(SwingConstants.CENTER);
		rPanel.add(statusClient);
		
		rPanel.add(new JPanel()); //Empty panel to make everything line up
		
		connectClientButton = new JButton("Connect Client");
		connectClientButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				clientConnectButton();
			}
		});
		rPanel.add(connectClientButton);
		
		JButton messageClientButton = new JButton("Send Message To Server");
		messageClientButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				clientSendMessageButton();
			}
		});
		rPanel.add(messageClientButton);
		
		messageClientTitle = new JLabel("No Message Received");
		rPanel.add(messageClientTitle);
		
		guiFrame.add(lPanel,BorderLayout.WEST);
		guiFrame.add(rPanel,BorderLayout.EAST);

		guiFrame.setVisible(true);
	}
	
	private void serverConnectButton() {
		if (!serverWrapper.isConnected()) serverWrapper.connect();
		else serverWrapper.disconnect();
	}
	
	private void serverSendMessageButton() {
		if (serverWrapper.getClientList().size() > 0) {
			JSONObject message = new JSONObject();
			message.put("Title", "The server here, saying hello!");
			for (SimpleJNetClient client : serverWrapper.getClientList()) {
				serverWrapper.sendMessage(message,client);
			}
		}
	}
	private void clientConnectButton() {
		//Create a new thread here so the connect() doesn't hang. Ideally would use a dedicated thread for all network commands.
		new Thread(new Runnable() {
			public void run() {
				if (!clientWrapper.isConnected()) clientWrapper.connect();
				else clientWrapper.disconnect();
			}
		}).start();

	}
	
	private void clientSendMessageButton() {
		JSONObject message = new JSONObject();
		message.put("Title", "Hello from the client!");
		clientWrapper.sendMessage(message);
	}
	
	public static void setClientMessageReceived(String message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				messageClientTitle.setText(String.format("<html><align=center><div WIDTH=%d>%s</div></align><html>", messageClientTitle.getWidth(), "Message Received:<br> " + message));
			}
		});	
	}
	
	public static void setServerConnectionChanged() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (serverWrapper.isConnected()) {
					connectServerButton.setText("Disconnect Server");
					setServerStatus("Connected");
				}
				else {
					connectServerButton.setText("Connect Server");
					setServerStatus("Disconnected");
				}
			}
		});	
	}
	
	public static void setClientConnectionChanged() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (clientWrapper.isConnected()) {
					connectClientButton.setText("Disconnect Client");
					setClientStatus("Connected");
				}
				else {
					connectClientButton.setText("Connect Client");
					setClientStatus("Disconnected");
				}
			}
		});	
	}
	
	public static void setServerStatus(String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				statusServer.setText(String.format("<html><align=center><div WIDTH=%d>%s</div></align><html>", statusServer.getWidth(), "Current Status:<br>" + text));
			}
		});	
	}
	public static void setClientStatus(String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				statusClient.setText(String.format("<html><align=center><div WIDTH=%d>%s</div></align><html>", statusClient.getWidth(), "Current Status:<br>" + text));
			}
		});	
	}
	public static void setServerClientsConnected(int number) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				clientsConnected.setText(String.format("<html><align=center><div WIDTH=%d>%s</div></align><html>", clientsConnected.getWidth(), "Clients Connected: " + number));
			}
		});	
	}
	
	public static void setServerMessageReceived(String message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				messageServerTitle.setText(String.format("<html><align=center><div WIDTH=%d>%s</div></align><html>", messageServerTitle.getWidth(), "Message Received:<br> " + message));
			}
		});	
	}

}
