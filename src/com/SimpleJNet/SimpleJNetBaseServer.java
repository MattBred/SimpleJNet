package com.SimpleJNet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class SimpleJNetBaseServer {
	/*
	 * 
	 * Non user-interactable base class for the client/server connection.
	 * Doesn't use it's own thread outright, but each connection does.
	 * 
	 */
	private static final String TAG = "SimpleJNetBaseServer";
	private int port = -1;
	private String serverAddress;
	private boolean useSSL = false;
	private boolean requireClientAuth = false;
	private boolean connected = false;
	private boolean isProgramServer = false;
	
	private BufferedReader reader;
	private PrintWriter writer;

	private KeyManagerFactory keyManagerFactory;
	private TrustManagerFactory trustManagerFactory;
	
	private SimpleJNetBaseWrapper wrapper;

	private SSLServerSocketFactory sslServerFactory;
	private SSLServerSocket serverSocketSSL;
	private ServerSocket serverSocket;
	private SSLSocketFactory sslSocketFactory;
	private Socket socket;
	private SSLSocket sslSocket;
	private Object threadPause = new Object();
	
	
	
	/*
	 * 
	 * Called from the wrapper. Starts everything off, and tells it if it's a server or client based wrapper
	 * 
	 */
	public SimpleJNetBaseServer(SimpleJNetBaseWrapper wrapper) {
		System.out.println("base server");
		this.isProgramServer = wrapper instanceof SimpleJNetServerWrapper;
		this.wrapper = wrapper;
		if (isProgramServer) {
			ClientListener listener = new ClientListener(this);
			listener.start();
		} else {
			ServerMessageListener listener = new ServerMessageListener(this);
			listener.start();
		}
	}
	
	
	
	/*
	 * 
	 * In client program, returns true if client is connected to the server.
	 * In server program, returns true if server socket is connected.
	 * 
	 */
	public boolean isConnected() {
		return connected;
	}
	
	
	
	/*
	 * 
	 * Sets up all the sockets.
	 * In client program, connects to server.
	 * In server program, creates server socket.
	 * 
	 */
	public void connect() {
		if (isProgramServer) {
			//Running program as a server
			if (!isConnected()) {
				try {
					if (useSSL) {
						createServerSocketSSL();
					} else {
						createServerSocket();
					}
					onConnected();
				} catch (IOException e) {
					wrapper.onConnectionFailed();
				}
				catch (Exception e) {
					wrapper.onConnectionFailed();
					e.printStackTrace();
				}
			} else {
				wrapper.error("Server Already Running");
			}
		} else {
			//Running program as a client
			if (!isConnected()) {
				try {
					if (useSSL) {
						createSocketSSL();
					} else {
						createSocket();
					}
					if (writer != null) writer.close();
					if (reader != null) reader.close();
		    	    writer = new PrintWriter(new OutputStreamWriter(getSocket().getOutputStream(),StandardCharsets.UTF_8), true);
		    	    reader = new BufferedReader(new InputStreamReader(getSocket().getInputStream(),StandardCharsets.UTF_8));
		    	    onConnected();
				} catch (IOException e) {
					wrapper.onConnectionFailed();
				} catch (Exception e) {
					wrapper.onConnectionFailed();
					e.printStackTrace();
				}
			} else {
				wrapper.error("Wrapper Already Connected to Server");
			}
		} 
	}
	
	
	
	/*
	 * 
	 * Called when the socket connection is successful, and calls the wrapper's onConnected() function.
	 * 
	 */
	private void onConnected() {
		connected = true;
		synchronized(threadPause) {
			threadPause.notifyAll();
		}
		wrapper.onConnected();
	}
	
	
	
	/*
	 * 
	 * Closes the connection. The object can still reconnect at any time.
	 * This must be called first if the server is running and you want to make changes to the settings (port, address, SSL settings etc)
	 * 
	 */
	public void disconnect() {
		try {
			if (isConnected()) {
				if (isProgramServer) {
					getServerSocket().close();
				}
				else {
					getSocket().close();
				}
			}
		} catch (Exception e) {getWrapper().error("Disconnection Failed");}
	}
	
	
	
	/*
	 * 
	 * Called once per connect()
	 * In client program, this creates the socket (SSL type) to the server
	 * 
	 */
	private void createSocketSSL() throws Exception {
		SSLContext sslContext = SSLContext.getInstance("TLS");
		KeyManager[] keyManagers = null;
		if (keyManagerFactory != null) keyManagers = keyManagerFactory.getKeyManagers();
		TrustManager[] trustManagers = null;
		if (trustManagerFactory != null) trustManagers = trustManagerFactory.getTrustManagers();
		sslContext.init(keyManagers, trustManagers, null);
		sslSocketFactory= sslContext.getSocketFactory();
		sslSocket = (SSLSocket) sslSocketFactory.createSocket();
		sslSocket.connect(new InetSocketAddress(serverAddress,port),2000);
	}
	
	
	
	/*
	 * 
	 * Called once per connect()
	 * In client program, this creates the socket to the server
	 * 
	 */
	private void createSocket() throws IOException {
		socket = new Socket();
		socket.connect(new InetSocketAddress(serverAddress, port), 2000);
	}

	
	
	/*
	 * 
	 * Called once per connect()
	 * In server program, this creates the socket (SSL type) to receive incoming connections
	 * 
	 */	
	private void createServerSocketSSL() throws Exception {
		SSLContext sslContext = SSLContext.getInstance("TLS");
		KeyManager[] keyManagers = null;
		if (keyManagerFactory != null) keyManagers = keyManagerFactory.getKeyManagers();
		TrustManager[] trustManagers = null;
		if (trustManagerFactory != null) trustManagers = trustManagerFactory.getTrustManagers();
		sslContext.init(keyManagers, trustManagers, null);
		sslServerFactory = sslContext.getServerSocketFactory();
		serverSocketSSL =(SSLServerSocket) sslServerFactory.createServerSocket(port);
		serverSocketSSL.setNeedClientAuth(requireClientAuth);
	}
	
	
	
	/*
	 * 
	 * Called once per connect()
	 * In server program, this creates the socket to receive incoming connections
	 * 
	 */
	private void createServerSocket() throws IOException {
		serverSocket = new ServerSocket(port);
	}

	
	
	/*
	 * 
	 * Called by SimpleJNetClientWrapper
	 * In client program, this sends a message to the server
	 * 
	 */
	public void sendMessage(JSONObject message) {
		if (isConnected()) {
			writer.println(message.toString());
		} else {wrapper.error("Message Send Failure - Connection Closed");}
	}
	
	
	
	/*
	 * 
	 * Called by SimpleJNetClientWrapper
	 * In client program, this sets the server address
	 * 
	 */
	public void setServerAddress(String address) {
		if (!isConnected()) {
			this.serverAddress = address;
		} else {
			wrapper.error("Can't change address while still connected.");
		}
	}
	
	
	
	/*
	 * 
	 * Called by SimpleJNetClientWrapper
	 * Sets port for server
	 * 
	 */
	public void setPort(int port) {
		//Sets the port for the server to use - Only works before server start
		if (!isConnected()) {
			this.port = port;
		} else {
			wrapper.error("Can't change port while still connected.");
		}
	}
	
	
	
	/*
	 * 
	 * Called by SimpleJNetClientWrapper
	 * Set to true if you want to use SSL
	 * 
	 */
	public void setUseSSL(boolean useSSL) {
		//Sets variable to use SSL encryption or not - Only works before server start
		if (!isConnected()) {
			this.useSSL = useSSL;
		} else {
			wrapper.error("Can't change SSL Settings while still connected.");
		}
	}
	
	
	
	/*
	 * 
	 * Called by SimpleJNetClientWrapper
	 * Set to true if you want to require SSL client authorization
	 * 
	 */
	public void setRequireSSLClientAuth(boolean requireClientAuth) {
		if (!isConnected()) {
			this.requireClientAuth = requireClientAuth;
		} else {
			wrapper.error("Can't change SSL Settings while still connected.");
		}
	}

	
	
	/*
	 * 
	 * Called by SimpleJNetClientWrapper
	 * Sets the SSL's trust store inputstream and password.
	 * 
	 */
	public void setTrustStore(InputStream inputStream, String trustStorePassword) {
		if (!isConnected()) {
			try {
				KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
				trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				InputStream trustStoreStream = inputStream;
				ts.load(trustStoreStream, trustStorePassword.toCharArray());
				trustManagerFactory.init(ts);
				inputStream.close();
			} catch (Exception e) {e.printStackTrace();}
		} else {
			wrapper.error("Can't change SSL Settings while still connected.");
		}
	}

	
	
	/*
	 * 
	 * Called by SimpleJNetClientWrapper
	 * Sets the SSL's key store inputstream and password.
	 * 
	 */
	public void setKeyStore(InputStream inputStream, String keyStorePassword) {
		if (!isConnected()) {
			try {
				KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
				keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				InputStream keyStoreStream = inputStream;
				keyStore.load(keyStoreStream, keyStorePassword.toCharArray());
				keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
				inputStream.close();
			} catch (Exception e) {e.printStackTrace();}
		} else {
			wrapper.error("Can't change SSL Settings while still connected.");
		}
	}

	
	
	/*
	 * 
	 * Called by connect() and disconnect()
	 * In client program, this is called to get the client's socket.
	 * 
	 */
	private Socket getSocket() {
		if (useSSL) return sslSocket;
		else return socket;
	}
	
	
	/*
	 * 
	 * Called by disconnect() and ClientListener.run()
	 * In server program, this is called to get the server's socket.
	 * 
	 */
	private ServerSocket getServerSocket() {
		if (useSSL) return serverSocketSSL;
		else return serverSocket;
	}	
	
	
	
	/*
	 * 
	 * Called by disconnect(), SimpleJNetBaseClient.disconnect(), and SimpleJNetBaseClient.run()
	 * Returns the wrapper.
	 * 
	 */
	public SimpleJNetBaseWrapper getWrapper() {
		return wrapper;
	}
	
	
	
	/*
	 * 
	 * In client program, this is a permanent thread class that receives messages from the server it's connected to.
	 * 
	 */
	private class ServerMessageListener extends Thread{
		private SimpleJNetBaseServer baseServer;
		
		private ServerMessageListener(SimpleJNetBaseServer server) {
			baseServer = server;
		}
		
		public void run() {
			String rawText = "";
	    	while(true){
	    		if (baseServer.isConnected()) {
	    			try {
	    				rawText = baseServer.reader.readLine();
	    				JSONObject message = (JSONObject)JSONValue.parse(rawText);
	    				((SimpleJNetClientWrapper) wrapper).onMessageReceived(message);
	    		    } catch (IOException e) {
	    		    	closeConnection();
	    		    } catch (Exception e) {
	    		    	e.printStackTrace();
	    		    	closeConnection();
	    		    }
	    		} else {
	    			synchronized(threadPause) {
	    				try {threadPause.wait();} catch (InterruptedException e) {e.printStackTrace();}
	    			}
	    		}
	    	}
		}
		private void closeConnection() {
			baseServer.connected = false;
			baseServer.wrapper.onConnectionClosed();
		}
	}
	
	
	
	/*
	 * 
	 * In a server program, this is a permanent thread class that receives incoming client connections and creates SimpleJNetClient objects when they arrive
	 * 
	 */
	private class ClientListener extends Thread{
		private static final String TAG = "SimpleNetClientListener";
		private Socket clientSocket;
		private SimpleJNetBaseServer baseServer;

		private ClientListener(SimpleJNetBaseServer server) {
			this.baseServer = server;
			System.out.println("clientlistener");
		}
		
		public void run() {
			System.out.println("run");
			while (true) {
				System.out.println("Beginning of loop");
				//TODO DEBUG!!!
				if (baseServer.isConnected()) {
					try {
						System.out.println("Listening for clients");
						//TODO DEBUG!!!
						clientSocket = baseServer.getServerSocket().accept();
						SimpleJNetClient client = new SimpleJNetClient();
						client.init(clientSocket, baseServer);
						((SimpleJNetServerWrapper) wrapper).onNewConnection(client);
					}
					catch (IOException e) {
						closeConnection();
					} catch (Exception e) {
						e.printStackTrace();
						closeConnection();
					}
				} else {
					System.out.println("Not connected!");
					//TODO DEBUG!!!
					synchronized(threadPause) {
						try {threadPause.wait();} catch (InterruptedException e) {e.printStackTrace();}
					}
				}
			}
		}
		
		private void closeConnection() {
			baseServer.connected = false;
			baseServer.wrapper.onConnectionClosed();
		}
	}
	
}
