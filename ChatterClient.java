package Chatter;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ChatterClient
{
	private ClientListener listener;
	private Scanner scanner;
	private BufferedWriter bout;
	Socket socket;

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
	        socket = new Socket(name, port);
	         
	        OutputStream out = socket.getOutputStream();
		    bout = new BufferedWriter( new OutputStreamWriter( out ) );
		    
	 		scanner = new Scanner(System.in);
			System.out.println("Welcome! What is your nickname (no spaces)?");
			String nickName = getUserInput();
			write("/nick " + nickName);
			System.out.println(nickName + " joined the Chat Room!");
		     
	        listener = new ClientListener(socket);
	        listener.start();
	         
	        while(!socket.isClosed())
	        {
	        	String userCommand = getUserInput();
	        	write(userCommand);
	        	if (userCommand.split(" ")[0].equals("/quit"))
	        	{
	        		System.out.println("Thanks for using this!");
	        		socket.close();
	        	}
	        }
	        System.out.println("Out of while loop!");
	        System.exit(0);
				
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
		private BufferedReader bin;
		
		
		public ClientListener(Socket s) throws Exception
		{

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
					if (!serverLine.isEmpty())
						System.out.println(serverLine);
				}
			}
			catch (Exception e) 
			{e.printStackTrace();}
	    }
		
	}
}
