
import java.io.InputStream;
import java.util.ArrayList;

import org.json.simple.JSONObject;

public class SimpleJNetServerWrapper implements SimpleJNetBaseWrapper{
	/*
	 * The wrapper for a server. It does not create it's own thread, so be careful when placing wait blocks in here.
	 * To start a server:
	 * SimpleJNetServerWrapper serverWrapper = new SimpleJNetServerWrapper();
	 * serverWrapper.setPort(8500);
	 * serverWrapper.connect();
	 * It's that simple to start a server. Any incoming connections and messages will be handled through these functions.
	 */
	private static final String TAG = "SimpleJNetServerWrapper";
	private SimpleJNetBaseServer baseServer;

	public SimpleJNetServerWrapper() {
		baseServer = new SimpleJNetBaseServer(this);
	}

	public void error(String errorString) {
		//Whenever an error happens it is returned here in string form
		System.out.println(TAG + " - error() - " + errorString);		
	}

	public void onConnected() {
		//This method is called when the server is successfully connected
		System.out.println(TAG + " - onConnected() - Server Connected");
		SimpleJNet_Example.setServerConnectionChanged();
	}

	public void onConnectionClosed() {
		//When the server connection is closed (it has to be connected first to call this)
		System.out.println(TAG + " - onConnectionClosed() - Server Connection Closed");
		SimpleJNet_Example.setServerConnectionChanged();
	}
	
	public void onConnectionFailed(String reason) {
		//After connect() is called, if the server fails to start, it calls this
		System.out.println(TAG + " - onConnectionFailed() - Server Connection Failed, Reason: " + reason);
		if (!isConnected()) SimpleJNet_Example.setClientStatus("Connection Failed");
		else SimpleJNet_Example.setClientStatus("Connected");
	}

	public void onNewConnection(SimpleJNetClient client) {
		//When using this in a server program, this method returns new client connections
		System.out.println(TAG + " - onNewConnection() - New client connected");
		SimpleJNet_Example.setServerClientsConnected(getClientList().size());
	}
	
	public void onMessageReceived(SimpleJNetClient client, JSONObject message) {
		//This method returns messages from a client
		System.out.println(TAG + " - onMessageReceived() - toString: " + message.toString());
		SimpleJNet_Example.setServerMessageReceived((String)message.get("Title"));
	}
	
	public void onClientConnectionClosed(SimpleJNetClient client) {
		//This method returns when a client's connection closes
		System.out.println(TAG + " - onClientConnectionClosed()");
		SimpleJNet_Example.setServerClientsConnected(getClientList().size());
	}
	
	public void connect() {
		//Connect the server
		System.out.println(TAG + " - connect() - Wrapper Attempting To Connect");
		SimpleJNet_Example.setServerStatus("Connecting");
		baseServer.connect();
	}
	
	public void disconnect() {
		//Disconnect the server
		System.out.println(TAG + " - disconnect() - Wrapper Disconnecting");
		SimpleJNet_Example.setServerStatus("Disconnecting");
		baseServer.disconnect();
	}
	
	public ArrayList<SimpleJNetClient> getClientList() {
		return baseServer.getClientList();
	}
	
	public void sendMessage(JSONObject message, SimpleJNetClient client) {
		//This method sends a message to a specific client
		client.sendMessage(message);
	}
	
	public boolean isConnected() {
		//Returns if the server has connection
		return baseServer.isConnected();
	}
	
	public void setPort(int port) {
		//Sets the port for the server
		baseServer.setPort(port);
	}

	public void setUseSSL(boolean useSSL) {
		//Use SSL? And require client Auth?
		baseServer.setUseSSL(useSSL);
	}
	
	public void setRequireSSLClientAuth(boolean requireClientAuth) {
		baseServer.setRequireSSLClientAuth(requireClientAuth);
	}

	public void setKeyStore(InputStream keyStorePath, String keyStorePassword) {
		//Set key store details for SSL
		baseServer.setKeyStore(keyStorePath, keyStorePassword);
		
	}

	public void setTrustStore(InputStream trustStorePath, String trustStorePassword) {
		//Set trust store details for SSL & client auth
		baseServer.setTrustStore(trustStorePath, trustStorePassword);
	}

}