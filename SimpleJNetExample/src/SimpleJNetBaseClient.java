
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class SimpleJNetBaseClient extends Thread{
	/*
	 * This base client handles I/O and socket connection details for clients connected to the server (if the wrapper is a server wrapper)
	 * There is a base client object created per client connection, each running it's own thread.
	 * Uses UTF-8 encoding
	 */
	private static final String TAG = "SimpleJNetBaseClient";
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	private SimpleJNetBaseServer baseServer;
	
	
	
	/*
	 *	Initializes the start variables, creates the I/O handlers, and starts the thread
	 */
	public void init(Socket socket, SimpleJNetBaseServer baseServer) {
		this.socket = socket;
		this.baseServer = baseServer;
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),StandardCharsets.UTF_8));
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),StandardCharsets.UTF_8),true);
		} catch (Exception e) {e.printStackTrace();}
		this.start();
	}
	
	
	
	/*
	 *	Main thread part of the base client
	 */
	public void run() {
		String in;
		try {
			while ((in = reader.readLine()) != null) {
				//When new messages are received, we pass them to the onMessageReceived() function in the server wrapper
				JSONObject message = (JSONObject)JSONValue.parse(in);
				((SimpleJNetServerWrapper)baseServer.getWrapper()).onMessageReceived((SimpleJNetClient)this,message);
			}
		}
		catch (IOException e) {}
		catch (Exception e) {e.printStackTrace();}
		finish();
	}
	
	
	
	/*
	 * Called by run()
	 * Cleans up all the streams and closes the socket if it's not already.
	 */
	private void finish() {
		closeSocketConnection();
		if (baseServer.getClientList().contains(this)) baseServer.getClientList().remove(this);
		((SimpleJNetServerWrapper)baseServer.getWrapper()).onClientConnectionClosed((SimpleJNetClient)this);
		try {
			writer.close();
			reader.close();
		} catch (Exception e) {}
	}
	
	
	
	/*
	 * 	Called by SimpleJNetBaseServer.ClientListener.run()
	 *	This function closes the client's socket
	 */
	public void closeSocketConnection() {
		try {
			if (!socket.isClosed()) socket.close();
		} catch (Exception e) {}
	}


	
	/*
	 *	Called from the server wrapper. Sends the message through the writer to the actual client.
	 */
	public void sendMessage(JSONObject message) {
		writer.println(message.toString());
	} 
}
