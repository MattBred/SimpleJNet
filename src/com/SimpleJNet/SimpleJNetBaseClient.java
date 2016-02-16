package com.SimpleJNet;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class SimpleJNetBaseClient extends Thread{
	private static final String TAG = "SimpleJNetBaseClient";
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	private SimpleJNetBaseServer baseServer;

	public void init(Socket socket, SimpleJNetBaseServer baseServer) {
		this.socket = socket;
		this.baseServer = baseServer;
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),StandardCharsets.UTF_8));
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),StandardCharsets.UTF_8),true);
		} catch (Exception e) {e.printStackTrace();}
		this.start();
	}
	
	public void run() {
		String in = "";
		try {
			while (socket.isConnected() && !socket.isClosed() && in != null) {
				in = reader.readLine();
				JSONObject message = (JSONObject)JSONValue.parse(in);
				((SimpleJNetServerWrapper)baseServer.getWrapper()).onMessageReceived((SimpleJNetClient)this,message);
			}
		} catch (Exception e) {
			((SimpleJNetServerWrapper)baseServer.getWrapper()).onClientConnectionClosed((SimpleJNetClient)this);
			try {
				writer.close();
				reader.close();
				if (!socket.isClosed()) socket.close();
			} catch (Exception e2) {}
		}
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public void sendMessage(JSONObject message) {
		writer.println(message.toString());
	} 
}
