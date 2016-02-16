# SimpleJNet
1. Description
2. Basic setup
3. SSL Encyption
4. Sending and receiving messages
5. !Important! Notes on threads
6. Classes list


<br>
<br>
**************************************************
<b>(1) Description</b>
**************************************************
Made by Matthew Breden<br>
A very simple to use high level thread-per-connection socket networking framework using JSON for Java.
Using the JSON Library by stleary here: https://github.com/stleary/JSON-java

SimpleJNet is for projects that require a simple, quick to set up, and reliable networking framework.

Key Features:
  -Uses a JSON library as the method for writing and reading information
  -TCP Socket based protocol
  -Supports SSL Encryption
  -UTF-8 Character Set
  -(Not really a feature..) Thread-per-connection based setup will not work for large scale setups. However, could be modified to use NIO.
<br>
<br>
<br>
**************************************************
<b>(2) Basic setup</b>
**************************************************
To use, simply import the framework into your project and set up a wrapper as follows:

<b>Setup For Servers:</b>

Create a SimpleJNetServerWrapper object and add the details you want to it.
  
	SimpleJNetServerWrapper serverWrapper = new SimpleJNetServerWrapper();
	serverWrapper.setPort(8500);
	serverWrapper.connect();
      
It's that simple to get started. If you want to use SSL, I will cover that below.
<br>
<br>
<b>Setup For Clients:</b>

Create a SimpleJNetClientWrapper object and add the details you want to it.
  
	SimpleJNetClientWrapper clientWrapper = new SimpleJNetClientWrapper();
	clientWrapper.setPort(8500);
	clientWrapper.setServerAddress("yourserveraddress.com");
	clientWrapper.connect();
      
Done! Now the client should connect to the server if it's possible.
<br>
<br>
<br>
**************************************************
<b>(3) SSL Encryption</b>
**************************************************
Adding SSL Encryption
If you want to use SSL encryption, you need to add the details to your wrapper before you connect. For example:
      
	wrapper.setUseSSL(true);
      
      
If the wrapper is a server, and you don't require client auth, then simply set the key store and you're done. You will need an InputStream for the keystore file, as well as the keystore password.
      
	wrapper.setKeyStore(keyStoreInputStream, keyStorePassword);
		  
		  
And likewise, if the wrapper is for a client and the server doesn't require client auth, you just set the trust store. Again, you'll need the InputStream for the file, and the password.
	
	wrapper.setTrustStore(trustStoreInputStream, trustStorePassword);
		  
	
If the server requires client auth, then you will need to set the key store and trust store for both server & clients. You will also need to add the following:

	wrapper.setRequireSSLClientAuth(true);
	    
	    
Remember, all of this must be done before the connect() function is called. If you wish to change any settings (port, address, SSL settings) then you must first call disconnect() on the server, then change the settings, and then call connect() again.
<br>
<br>
<br>	
**************************************************
<b>(4) Sending and receiving messages</b>
**************************************************
SimpleJNet uses JSON to write and read messages, which again is all done through the wrapper.

<b>Sending a message as a Server</b>

To send a message to a client from a server, first you need to know which client to send it to. I reccomend creating an array in the wrapper class to keep track of your clients, and giving them properties in the SimpleJNetClient class such as their user name, etc. (When clients connect, the function onNewConnection() will call. When their connection closes, onClientConnectionClosed() will call)
  
Once you have the client object found, simply do the following to send a hello world message
    
	JSONObject message = new JSONObject();
	message.put("Title", "Hello From Canada");
	serverWrapper.sendMessage(message,client);
		  
Done! The client will now receive the message.
<br>
<br>
<b>Receiving messages as a server</b>

When the server wrapper receives a message, the onMessageReceived() function will call. The two parameters will include the client that sent the message, and the message itself. To println a message that has a key of "Title", do the following
  
	System.out.println("The title is " + message.getString("Title"));
  
And that's it!
<br>
<br>
<b>Sending a message as a client</b>

Sending messages as a client is pretty much identical as the server implementation, except you don't need to reference a client as you are the client!
  
	clientWrapper.sendMessage(message);
      
And the message will send.
<br>
<br>
<b>Receiving messages as a client</b>

Basically the same story here as well. The onMessageReceived() function will call, and the only parameter will be the message.
<br>
<br>
<br>
**************************************************
<b>(5) !Important! Notes on threads</b>
**************************************************
The threads are as follows:<br><br>
<b>Server Wrapper</b><br>
1 Permanent thread created to listen for incoming connections<br>
1 Semi-Permanent thread created per EACH client to listen for incoming messages (Thread terminates when connection is closed)<br>
<br>
<b>Client Wrapper</b><br>
1 Permanent thread created to listen for incoming server messages<br>
<br>
You should only ever create a single wrapper object for your server or client needs. The code will maintain that object for the lifespan of the program. If you need to change settings for your client or server (IE Port, address, SSL settings) then call disconnect(), apply your changes, and then call connect(). Do not create a new wrapper object, as it will duplicate the listener threads. The listener threads will only try and read messages when the connection is alive, and will dyanmically adjust to changes in server settings.
<br>
<br>
<br>
**************************************************
<b>(6) Classes list</b>
**************************************************
<b>SimpleJNetBaseClient</b> - Base client object for handling I/O<br>
<b>SimpleJNetBaseServer</b> - Base server object to handle I/O, as well as handling details for both the server wrapper and client wrapper<br>
<b>SimpleJNetBaseWrapper</b> - Interface for the wrappers<br>
<b>SimpleJNetClient</b> - Bare bones client object that extends <b>SimpleJNetBaseClient</b>. This is the class that the wrapper uses. Put any user-defined variables in here, as any future updates may over-write the base client.<br>
<b>SimpleJNetClientWrapper</b> - If using the program as a client, this is the wrapper you use.<br>
<b>SimpleJNetServerWrapper</b> - If using the program as a server, this is the wrapper you use.<br>
