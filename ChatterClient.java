package Chatter;

import java.io.*;
import java.net.Socket;
import java.util.*;

//Client side of a Chat room. Each client should run this file.
public class ChatterClient
{
	private ClientListener listener;
	private Scanner scanner;
	private BufferedWriter bout;
	private Socket socket;

	//Starts a Chatter client with inputted hostname and portnumber, or with defaults 127.0.0.1 and 11123
	public static void main( String[] args )
	{	
		if (args.length == 2) 
		{
			new ChatterClient(args[0], Integer.parseInt(args[1]));
        }
		else
		{
			System.out.println("Use format: java ChatterClient <host name> <port number>");
            System.out.println("Using default hostname \"127.0.0.1\" and port 11123 ");
            new ChatterClient("127.0.0.1", 11123);
		}
	}

	//constructor for ChatterClient Class
	public ChatterClient(String name, int port)
	{		
		try
		{
			//this command will throw an exception if "port" is not open
	        socket = new Socket(name, port);
	        
	        OutputStream out = socket.getOutputStream();
		    bout = new BufferedWriter( new OutputStreamWriter( out ) );
		    
	 		scanner = new Scanner(System.in);
			System.out.println("Welcome to the Chat Room! What is your nickname (no spaces)?");
			String nickName = getUserInput();
			write("/nick " + nickName);
			System.out.println("Welcome " + nickName + "!");
			System.out.println("Simply type for group messages, or use these commands:");
			System.out.println("           /nick NAME (change nickname)");
			System.out.println("           /dm NAME MSG (Send personal MSG to NAME)");
			System.out.println("           /users (lists all current users of the chat room)");
			System.out.println("           /quit (Leave the chat room)\n");
			
			
		     
	        listener = new ClientListener(socket);
	        listener.start();
	         
	        //The main thread stays here and checks for keyboard commands (and sends them to the server) continuously 
	        //until the user types /quit
	        while(!socket.isClosed())
	        {
	        	String userCommand = getUserInput();
	        	write(userCommand);
	        	if (userCommand.split(" ")[0].equals("/quit"))
	        	{
	        		System.out.println("Thanks for using the Chat Room!");
	        		socket.close();
	        	}
	        }
	        System.exit(0);
				
	    }
	    catch ( Exception e )
	     { System.err.println(e); }
	}
	
	//Returns keyboard input, blocks until enter is hit
	private String getUserInput()
	{
		String line = scanner.nextLine();
		return line;
	}
	
	//writes to the server using a BufferedWriter, adding a "\n" so that the server can use "readline()"
	public void write(String s) throws IOException 
	{
        bout.write(s + "\n");
        bout.flush();
	}
	
	//Thread that continuously listens on the socket for ChatterServer messages
	public class ClientListener extends Thread
	{
		private BufferedReader bin;
		
		//constructor, sets up the BufferedReader "bin
		public ClientListener(Socket s) throws Exception
		{
	        InputStream in = s.getInputStream();
	        bin = new BufferedReader( new InputStreamReader(in) );
		}
		
		//Blocks until the server flushes a message to the socket
		public String readLine() throws IOException
		{
	         String line = bin.readLine();
	         return line;
		}
		
		//runs continuously
		@Override
	    public void run()
	    {
			try
			{
				while(true)
				{
					//prints the server's message as long as it's not empty
					String serverLine = readLine();
					if (!serverLine.isEmpty())
						System.out.println(serverLine);
				}
			}
			catch (Exception e) 
			{e.printStackTrace();}
	    }
	}
}
