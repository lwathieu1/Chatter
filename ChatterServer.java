package Chatter;

import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.LinkedList;



public class ChatterServer
{
	ServerSocket sock;
	LinkedList<ServerListener> listOfListeners = new LinkedList<ServerListener>();
	
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
	
	public ChatterServer(int port)
	{
		try
		{
	        sock = new ServerSocket(port); // open socket
	        answerThePhone();
		}
	    catch( Exception e ) { System.err.println("DateServer: error = "+e); } 
	}
	
	
	private void answerThePhone() throws Exception 
	{
		while (true) // has no way to stop as written
        {
			System.out.println("Listening on port "+sock.getLocalPort()+"...");
			// when client calls, establish output stream to client and send date
	    	// does not return until client calls up
	        Socket client = sock.accept(); // this blocks code until a client calls      
	        System.out.println("Chatter Server: accepts client connection! ");
	        ServerListener sListenerThread = new ServerListener(client);
	        listOfListeners.add(sListenerThread);
	        sListenerThread.start();
        }

	}
	
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
	
	
	public class ServerListener extends Thread
	{
		public String nickName = "defaultName";
		private Socket clientSocket;
		BufferedReader bin;
		BufferedWriter bout;
		//PrintWriter pout;
		
		public ServerListener(Socket s) throws Exception
		{
			clientSocket = s;
			
			InputStream in = clientSocket.getInputStream();
			bin = new BufferedReader( new InputStreamReader(in) );
			
			OutputStream out = clientSocket.getOutputStream();
		    bout = new BufferedWriter( new OutputStreamWriter( out ) );
			
		}
		
		
		public void write(String s) throws IOException 
		{
			bout.write(s+"\n");
	        bout.flush();
		}
		
		public String readLine() throws IOException
		{
            // Thread.sleep(1000);
            String msg = bin.readLine();
            return msg;
		}
		
		@Override
	    public void run()
	    {
		try {
			while(!clientSocket.isClosed())
			{
				String inLine = readLine();
				System.out.println("Got something from the user!");
				if (!inLine.isEmpty())
				{
					System.out.println("The Line from "+ nickName +" is = ");
					System.out.println(inLine);
					
					//Process the incoming string
					String[] arrOfWords = inLine.split(" ");
					if (arrOfWords[0].equals("/nick") && arrOfWords.length > 1)
					{
						String oldNickName = nickName;
						nickName = arrOfWords[1];
						if (oldNickName.equals("defaultName"))
							tellOthers(this, nickName+" joined the Chat Room!");
						else
							tellOthers(this, oldNickName + " changed their name to " + nickName);
					}
					else if (arrOfWords[0].equals("/dm") && arrOfWords.length > 1)
					{
						String ya = "DM from "+nickName+ ": "+ inLine.split(" ", 2)[1].split(" ", 2)[1];
						if (!tellOne(ya, arrOfWords[1]))
							tellOne("\""+ya+"\" was not found in the chat room!", nickName);
					}
					else if (arrOfWords[0].equals("/quit"))
					{
						tellOthers(this, nickName + " has left the chat room!");
						clientSocket.close();
						//break;
					}
					else 
						tellOthers(this, nickName+": "+inLine);
				}
			}

			
			Iterator <ServerListener> it = listOfListeners.iterator();
		 	while( it.hasNext() )
		 	{
		 		ServerListener listener = it.next(); 
		 		if (listener.equals(this))
		 		{
		 			it.remove();
		 		}
		 	}
		}
		catch (Exception e) 
			{e.printStackTrace();}
	    }
	}
	
	
}
