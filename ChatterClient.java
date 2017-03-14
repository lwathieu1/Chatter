package Chatter;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ChatterClient
{
	ClientListener listener;
	private Scanner scanner;
	//String nickName;
	
	BufferedWriter bout;

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

	
	public ChatterClient(String name, int port)
	{		
		try
		{
	        Socket sock = new Socket(name, port);
	         
	        OutputStream out = sock.getOutputStream();
		    bout = new BufferedWriter( new OutputStreamWriter( out ) );
		    
	 		scanner = new Scanner(System.in);
			System.out.println("Welcome! What is your nickname (no spaces)?");
			String nickName = getUserInput();
			//System.out.println("Hi "+ nickName +"! DIRECTIONS HERE");
			write("/nick " + nickName);
		     
	        listener = new ClientListener(sock);
	        listener.start();
	         
	        while(true)
	        {
	        	String userCommand = getUserInput();
	        	write(userCommand);
	        	if (userCommand.split(" ")[0].equals("/quit"))
	        	{
	        		System.out.println("quit somehow!");
	        		//What to do to quit??
	        	}
	        			
	        }
				
	    }
	    catch ( Exception e )
	     { System.err.println(e); }
	}
	
	
	private String getUserInput()
	{
		String line = scanner.nextLine();
		return line;
	}
	
	public void write(String s) throws IOException 
	{
        bout.write(s + "\n");
        bout.flush();
	}
	
	public class ClientListener extends Thread
	{
		//private Socket serverSocket;
		BufferedReader bin;
		
		
		public ClientListener(Socket s) throws Exception
		{
			//serverSocket = s;
			
	        InputStream in = s.getInputStream();
	        bin = new BufferedReader( new InputStreamReader(in) );
		}
		
		public String readLine() throws IOException
		{
	         String line = bin.readLine();
	         return line;
		}
		
		@Override
	    public void run()
	    {
			try
			{
				while(true)
				{
					String serverLine = readLine();
					System.out.println(serverLine);
				}
			}
			catch (Exception e) 
			{e.printStackTrace();}
	    }
		
	}
}
