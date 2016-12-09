import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;



import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Server {
	public  Map<String,ServerInfo> serverlist;
	public  Map<String,UserInfo> userlist;
	public  Set<String> lockedIdentity;
	public  Map<String,RemoteChatroomInfo> remoteChatroomList;
	public  Map<String,LocalChatroomInfo> localChatroomList;
	public  Set<String> lockedChatroom;
	public ServerInfo serverInfo =null;
	private LocalChatroomInfo mainhall;
	
	public Server(){
		this.serverlist = new HashMap<String, ServerInfo>();
		userlist= new HashMap<String,UserInfo>();
		lockedIdentity = new HashSet<String>();
		remoteChatroomList= new HashMap<String,RemoteChatroomInfo>();
		localChatroomList= new HashMap<String,LocalChatroomInfo>();
		lockedChatroom= new HashSet<String>();
		serverInfo =null;
	}
	
	public synchronized LocalChatroomInfo getMainhall() {
		return mainhall;
	}
	
	public synchronized Map<String,LocalChatroomInfo> localChatroomList() {
		return localChatroomList;
	}
	
	public synchronized Map<String,RemoteChatroomInfo> remoteChatroomList() {
		return remoteChatroomList;
	}
	public synchronized Map<String,UserInfo> userlist() {
		return userlist;
	}
	public synchronized  Set<String> lockedIdentity() {
		return lockedIdentity;
	}
	public synchronized  Set<String> lockedChatroom() {
		return lockedChatroom;
	}
	
	
	public static void main(String[] args) throws CmdLineException, IOException 
	{
		Server server  = new Server();
		server.mainhall=null;
		// CmdLineReader
		CmdLineArgs argsBean = new CmdLineArgs();
		CmdLineParser parser = new CmdLineParser(argsBean);
		parser.parseArgument(args);
		String serverid = argsBean.getServerid();
		String path = argsBean.getSevers_conf();
		FileReader file= new FileReader(path);
		BufferedReader br = new BufferedReader(file);
		String line = null;
		
		while ((line=br.readLine())!=null)
		{
			String[] words=line.split(" ");
			if (words[0].equals(serverid))
			{
				server.serverInfo=new ServerInfo(serverid,words[1],Integer.valueOf(words[2]),Integer.valueOf(words[3]));
			}
			else
			{
				ServerInfo otherserver=new ServerInfo(words[0],words[1],Integer.valueOf(words[2]),Integer.valueOf(words[3]));
				server.serverlist.put(words[0],otherserver);
				server.remoteChatroomList.put("MainHall"+"-"+words[0], new RemoteChatroomInfo("MainHall"+"-"+words[0],words[0]));
			}
		}
		br.close();

		server.mainhall= new LocalChatroomInfo("MainHall"+"-"+server.serverInfo.getServerName(),"",new HashSet<String>());
		server.localChatroomList.put(server.mainhall.getChatroomId(), server.mainhall);
		
		
		
//		∂‡Serverº‡Ã˝Socket;
		ServerSocket serverListenSocket=new ServerSocket(server.serverInfo.getManagementPort());
		ServerListenThread  serverListened=new ServerListenThread(server,serverListenSocket);
		serverListened.setName("ServerListeningThread");
		serverListened.start();
		
//		ClientÕ®–≈Socket
		ServerSocket ClientListenSocket = null;
		try {
			 ClientListenSocket = new ServerSocket(server.serverInfo.getPort());
			System.out.println(Thread.currentThread().getName() + 
					" - Server listening on client-port "+server.serverInfo.getPort()+" for a connection");
			
			
			int clientNum=0;
			while (true)
			{
				Socket clientSocket = ClientListenSocket.accept();
				System.out.println(Thread.currentThread().getName()+ " - Client conection accepted");
				clientNum++;
				ClientListenThread clientListened = new ClientListenThread(server,clientSocket,clientNum);	
				clientListened.setName("clientThread" + clientNum);
				clientListened.start();
				ServerState.getInstance().clientConnected(clientListened);
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			if(ClientListenSocket != null) 
			{
				try {
					ClientListenSocket.close();
					} 
				catch (IOException e) {e.printStackTrace();}
			}
		}
}
}
		
		

