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

import org.json.JSONObject;

public class SimpleJNetBaseServer {
	/*
	 * Non user-interactable base class for the client/server connection.
	 * Doesn't use it's own thread outright, but each connection does.
	 */
	private static final String TAG = "SimpleJNetBaseServer";
	private int port = -1;
	private String serverAddress;
	private boolean useSSL = false;
	private boolean requireClientAuth = false;
	private boolean serverConnected = false;
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
	
	public SimpleJNetBaseServer(boolean isProgramServer, SimpleJNetBaseWrapper topServer) {
		this.isProgramServer = isProgramServer;
		this.wrapper = topServer;
		if (isProgramServer) {
			ClientListener listener = new ClientListener(this);
			listener.start();
		} else {
			ServerMessageListener listener = new ServerMessageListener(this);
			listener.start();
		}
	}
	
	public boolean isConnected() {
		return serverConnected;
	}
	
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
	
	private void onConnected() {
		serverConnected = true;
		wrapper.onConnected();
	}
	
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
	
	private void createSocket() throws IOException {
		socket = new Socket();
		socket.connect(new InetSocketAddress(serverAddress, port), 2000);
	}

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
	
	private void createServerSocket() throws IOException {
		serverSocket = new ServerSocket(port);
	}

	public void sendMessage(JSONObject message) {
		//When using this in a client program, this method sends messages to the server.
		if (isConnected()) {
			writer.println(message.toString());
		} else {wrapper.error("Message Send Failure - Connection Closed");}
	}
	
	public void setServerAddress(String address) {
		//When using this in a client program, this method sets the server's address
		if (!isConnected()) {
			this.serverAddress = address;
		} else {
			wrapper.error("Can't change address while still connected.");
		}
	}
		
	public void setPort(int port) {
		//Sets the port for the server to use - Only works before server start
		if (!isConnected()) {
			this.port = port;
		} else {
			wrapper.error("Can't change port while still connected.");
		}
	}
	
	public void setUseSSL(boolean useSSL) {
		//Sets variable to use SSL encryption or not - Only works before server start
		if (!isConnected()) {
			this.useSSL = useSSL;
		} else {
			wrapper.error("Can't change SSL Settings while still connected.");
		}
	}
	
	public void setRequireSSLClientAuth(boolean requireClientAuth) {
		if (!isConnected()) {
			this.requireClientAuth = requireClientAuth;
		} else {
			wrapper.error("Can't change SSL Settings while still connected.");
		}
	}

	public void setTrustStore(InputStream inputStream, String trustStorePassword) {
		//Sets the SSL trust store details - Only works before server start
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

	public void setKeyStore(InputStream inputStream, String keyStorePassword) {
		//Sets the SSL key store details
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

	private Socket getSocket() {
		if (useSSL) return sslSocket;
		else return socket;
	}
	
	private ServerSocket getServerSocket() {
		if (useSSL) return serverSocketSSL;
		else return serverSocket;
	}	
	
	public SimpleJNetBaseWrapper getWrapper() {
		return wrapper;
	}
	
	private class ServerMessageListener extends Thread{
		private SimpleJNetBaseServer baseServer;
		
		private ServerMessageListener(SimpleJNetBaseServer server) {
			baseServer = server;
		}
		
		public void run() {
			//Running program as a client
			String rawText = "";
	    	while(true){
	    		if (baseServer.isConnected()) {
	    			try {
	    				rawText = baseServer.reader.readLine();
	    				JSONObject message = new JSONObject(rawText);
	    				((SimpleJNetClientWrapper) wrapper).onMessageReceived(message);
	    		    } catch (IOException e) {
	    		    	disconnect();
	    		    } catch (Exception e) {
	    		    	e.printStackTrace();
	    		    	disconnect();
	    		    }
	    		} else {
	    			try {Thread.sleep(100);} catch (Exception e) {}
	    		}
	    	}
		}
		
		private void disconnect() {
			baseServer.serverConnected = false;
			baseServer.wrapper.onConnectionClosed();
		}
	}
	
	private class ClientListener extends Thread{
		private static final String TAG = "SimpleNetClientListener";
		private Socket clientSocket;
		private SimpleJNetBaseServer baseServer;

		private ClientListener(SimpleJNetBaseServer server) {
			this.baseServer = server;
		}
		
		public void run() {
			while (true) {
				if (baseServer.isConnected()) {
					try {
						clientSocket = baseServer.getServerSocket().accept();
						SimpleJNetClient client = new SimpleJNetClient();
						client.init(clientSocket, baseServer);
						((SimpleJNetServerWrapper) wrapper).onNewConnection(client);
					}
					catch (IOException e) {
						disconnect();
					} catch (Exception e) {
						e.printStackTrace();
						disconnect();
					}
				}
				try {Thread.sleep(100);} catch (Exception e) {};
			}
		}
		
		private void disconnect() {
			baseServer.serverConnected = false;
			baseServer.wrapper.onConnectionClosed();
		}
	}
	
}
