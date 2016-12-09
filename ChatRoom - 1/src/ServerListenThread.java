import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ServerListenThread extends Thread {
	private Server server;
	private ServerSocket serverListenSocket;
	public  ServerListenThread(Server server,ServerSocket serverListenSocket)
	{
			this.server=server;
			this.serverListenSocket=serverListenSocket;
	}	

	@Override
	public void run() 
	{
		try 
		{	
			System.out.println(Thread.currentThread().getName() + 
				" - Server listening on port "+server.serverInfo.getManagementPort()+" for a connection");
			int serverNum=0;
			while (true)
			{
					serverNum++;
					Socket serverSocket = serverListenSocket.accept();
					System.out.println(Thread.currentThread().getName()+ " - Server conection accepted");
					ServerConnectionThread serverConnection = new ServerConnectionThread(server,serverSocket,serverNum);	
					serverConnection.setName("ServerThread" + serverNum);
					serverConnection.start();
			}
		}
		
		catch (IOException e)
		{
			e.printStackTrace();
		}
				
	}
} 
		

