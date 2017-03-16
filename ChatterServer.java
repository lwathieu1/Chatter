package Chatter;

import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.LinkedList;

//Server side of a Chat room. This file should be run once.
public class ChatterServer
{
	private ServerSocket sock;
	//stores a list of each serverListener (each connected to a client
	private LinkedList<ServerListener> listOfListeners = new LinkedList<ServerListener>();
	
	public static void main( String[] args )
	{
		int portNum = 11123;
		if (args.length == 1)
			portNum = Integer.parseInt(args[0]);
		else
		{
			System.out.println("Please run with the following format:");
			System.out.println("java ChatterServer <port number>");
			System.out.println("In the meantime, using default port number of 11123");
		}

		new ChatterServer(portNum);
	}
	
	//constructor for ChatterServer, opens the socket if available and answers the phone
	public ChatterServer(int port)
	{
		try
		{
	        sock = new ServerSocket(port); // open socket
	        answerThePhone();
		}
	    catch( Exception e ) { System.err.println("DateServer: error = "+e); } 
	}
	
	//continuously waits on the chosen port for a client to call
	private void answerThePhone() throws Exception 
	{
		while (true) // has no way to stop as written
        {
			System.out.println("Listening on port "+sock.getLocalPort()+"...");
	        Socket client = sock.accept(); // this blocks code until a client calls      
	        System.out.println("Chatter Server accepted a client connection!");
	        //creates a ServerListener thread for each client, adds it to the list and starts the thread
	        ServerListener sListenerThread = new ServerListener(client);
	        listOfListeners.add(sListenerThread);
	        sListenerThread.start();
        }

	}
	
	//sends the message to all connected clients through ServerListener.write except the "sender" client
	private
	synchronized
	void tellOthers(ServerListener sender, String message) throws Exception 
	{
		System.out.println("trying to tell others!!");
		Iterator <ServerListener> it = listOfListeners.iterator();
	 	while( it.hasNext() )
	 	{
	 		ServerListener listener = it.next(); 
	 		if (!listener.equals(sender))
	 			listener.write(message);
	 	}
	}
	
	//sends the message to all connected clients wiht name "name" through ServerListener.write
	//returns true if "name" was found, and false if "name" was not found.
	private
	synchronized
	boolean tellOne(String message, String name) throws Exception
	{
		boolean found = false;
		System.out.println("trying to tell "+name);
		Iterator <ServerListener> it = listOfListeners.iterator();
	 	while( it.hasNext() )
	 	{
	 		ServerListener listener = it.next(); 
	 		if (listener.nickName.equals(name))
	 		{
	 			listener.write(message);
	 			found = true;
	 		}
	 	}
	 	return found;
	}
	
	//Thread that listens to the ChatterClient on that socket and processes what it receives.
	public class ServerListener extends Thread
	{
		public String nickName = "defaultName";
		private Socket clientSocket;
		private BufferedReader bin;
		private BufferedWriter bout;
		
		//constructor, stores the BufferedReader and BufferedWriter required for the socket
		public ServerListener(Socket s) throws Exception
		{
			clientSocket = s;
			
			InputStream in = clientSocket.getInputStream();
			bin = new BufferedReader( new InputStreamReader(in) );
			
			OutputStream out = clientSocket.getOutputStream();
		    bout = new BufferedWriter( new OutputStreamWriter( out ) );
		}
		
		
		//writes "s" to the client
		public void write(String s) throws IOException 
		{
			bout.write(s+"\n");
	        bout.flush();
		}
		
		//reads from the client, blocks until client writes "\n" to the pipe
		public String readLine() throws IOException
		{
            String msg = bin.readLine();
            return msg;
		}
		
		
		//takes "line" from client and processes it, seeing if any keywords were written. 
		//closes socket if "/quit" is written
		private void processClientLine(String line) throws Exception
		{
			//split string along spaces, to process easier
			String[] arrOfWords = line.split(" ");
			
			//user wants to change their name
			if (arrOfWords[0].equals("/nick") && arrOfWords.length > 1)
			{
				//if old name is "defaultName", nickName just joined. otherwise, it is a name change
				String oldNickName = nickName;
				nickName = arrOfWords[1];
				if (oldNickName.equals("defaultName"))
					tellOthers(this, nickName+" joined the Chat Room!");
				else
					tellOthers(this, oldNickName + " changed their name to " + nickName);
			}
			
			//user wants to write to a specific user
			else if (arrOfWords[0].equals("/dm") && arrOfWords.length > 1)
			{
				//strippedMessage takes out first 2 words ("/dm name .....")
				String strippedMessage = line.split(" ", 2)[1].split(" ", 2)[1];
				String cleanedMessage = "DM from "+nickName+ ": "+ strippedMessage;
				String recipient = arrOfWords[1];
				
				//returns false if "recipient" is not the name of a chat room user, reports to sender
				boolean messageSent = tellOne(cleanedMessage, recipient);
				if (!messageSent)
					tellOne("\""+recipient+"\" was not found in the chat room!", nickName);
			}
			
			//user wants to list all users in the chat room
			else if (arrOfWords[0].equals("/users"))
			{
				String listOfNames = "Current Users: ";
				Iterator <ServerListener> it = listOfListeners.iterator();
			 	while( it.hasNext() )
			 	{
			 		ServerListener listener = it.next(); 
			 		listOfNames += listener.nickName +", ";
			 	}
			 	listOfNames = listOfNames.substring(0, listOfNames.length() - 2);
			 	tellOne(listOfNames, nickName);
			}
			
			//user wants to quit
			else if (arrOfWords[0].equals("/quit"))
			{
				tellOthers(this, nickName + " has left the chat room!");
				clientSocket.close();
			}
			else 
				tellOthers(this, nickName+": "+line);
		}
		
		
		//executes when ".start()" is used
		@Override
	    public void run()
	    {
		try {
			//exits when the socket is closed (the user said /quit)
			while(!clientSocket.isClosed())
			{
				String inLine = readLine();
				if (!inLine.isEmpty())
				{
					System.out.println("The Line from "+ nickName +" is: ");
					System.out.println(inLine);
					
					//closes socket (leaves while loop) if /quit is typed
					processClientLine(inLine);
				}
			}

			//runs when the user quits, finds the appropriate listener in the list and deletes it
			Iterator <ServerListener> it = listOfListeners.iterator();
		 	while( it.hasNext() )
		 	{
		 		ServerListener listener = it.next(); 
		 		if (listener.equals(this))
		 			it.remove();
		 	}
		}
		catch (Exception e) 
			{e.printStackTrace();}
	    }
	}
}
