package com.SimpleJNet;
import java.io.InputStream;

import org.json.simple.JSONObject;

public class SimpleJNetClientWrapper implements SimpleJNetBaseWrapper {
	/*
	 * The wrapper for a client. It does not create it's own thread, so be careful when placing wait blocks in here.
	 * To connect to a server:
	 * SimpleJNetClientWrapper clientWrapper = new SimpleJNetClientWrapper();
	 * clientWrapper.setPort(8500);
	 * clientWrapper.setServerAddress("yourserveraddress.com");
	 * clientWrapper.connect();
	 * It's that simple to connect. Any incoming messages will be handled through these functions.
	 */
	private static final String TAG = "SimpleJNetClientWrapper";
	private SimpleJNetBaseServer baseServer;

	public SimpleJNetClientWrapper() {
		baseServer = new SimpleJNetBaseServer(this);
	}

	public void error(String errorString) {
		//Whenever an error happens it is returned here in string form
		System.out.println(TAG + " - error() - " + errorString);
	}
	
	public void onConnected() {
		//This method is called when the server is successfully connected
		System.out.println(TAG + " - onConnected() - Wrapper Connected");
	}

	public void onConnectionClosed() {
		//When the connection to the server is closed (it has to be connected first to call this)
		System.out.println(TAG + " - onConnectionClosed() - Connection to Server closed");
	}
	
	public void onConnectionFailed(String reason) {
		//After connect() is called, if the wrapper fails to connect to the server, it calls this
		System.out.println(TAG + " - onConnectionFailed() - Failed to Connect to Server, Reason: " + reason);
	}
	
	public void onMessageReceived(JSONObject message) {
		//This method returns messages from the server.
		System.out.println(TAG + " - onMessageReceived() - toString: " + message.toString());
	}

	public void connect() {
		System.out.println(TAG + " - connect() - Wrapper Attempting To Connect to Server");
		baseServer.connect();
	}
	
	public void disconnect() {
		System.out.println(TAG + " disconnect() - Wrapper Disconnecting");
		baseServer.disconnect();
	}
	
	public void sendMessage(JSONObject message) {
		//This method sends messages to the server.
		baseServer.sendMessage(message);
	}
	
	public boolean isConnected() {
		//Returns if the server has connection
		return baseServer.isConnected();
	}
	
	public void setServerAddress(String address) {
		//When using this in a client program, this method sets the server's address 
		baseServer.setServerAddress(address);
	}
	
	
	public void setPort(int port) {
		//Sets the port for the server
		baseServer.setPort(port);
	}

	public void setUseSSL(boolean useSSL) {
		//Use SSL?
		baseServer.setUseSSL(useSSL);
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