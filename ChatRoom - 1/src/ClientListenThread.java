import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientListenThread extends Thread {
	private Server server;
	private UserInfo clientInfo;
	private Socket clientSocket;
	private BufferedReader reader;
	private BufferedWriter writer;
	private BlockingQueue<Message> messageQueue;
	private int clientNum;

	public ClientListenThread(Server server,Socket clientSocket, int clientNum) {
		try {
			this.clientInfo= new UserInfo(null,null,clientSocket,this);
			this.server=server;
			this.clientSocket = clientSocket;
			reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
			writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));	
			messageQueue = new LinkedBlockingQueue<Message>();
			this.clientNum = clientNum;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		
		try {
			
			//Start the client message reader thread. It 'listens' for any
			//incoming messages from the client's socket input stream and places
			//them in a queue (producer)
			ClientMessageReader messageReader = new ClientMessageReader(this,reader, messageQueue);
			messageReader.setName(this.getName() + "Reader");
			messageReader.start();
			
			System.out.println(Thread.currentThread().getName() 
					+ " - Processing client " + clientNum + "  messages");
			
			//Monitor the queue to process any incoming messages (consumer)
			while(true) 
			{
				
				//This method blocks until there is something to take from the queue
				//(when the messageReader receives a message and places it on the queue
				//or when another thread places a message on this client's queue)
				Message msg = messageQueue.take();
			
				if(msg.getMessageType()==0) 
				{
					break;
				}
				if(msg.getMessageType()==1)//单播类型
				{
					write(msg.getMessage());
				}				
				if(msg.getMessageType()==2)//广播类型
				{	
					
					Message msgForThreads = new Message(1, msg.getMessage());
					String room2Cast=msg.getRoom2Cast();
					List<ClientListenThread> connectedClients = ServerState.getInstance().getConnectedClients();
					for(ClientListenThread client : connectedClients) 
					{
						System.out.println(client.getClientInfo().getCurrentChatroom());
						if (client.getClientInfo().getCurrentChatroom().equals(room2Cast))
						{
							client.getMessageQueue().add(msgForThreads);
						}
					}
				}
				
			}
			clientSocket.close();
			ServerState.getInstance().clientDisconnected(this);
			System.out.println(Thread.currentThread().getName() 
					+ " - Client " + clientNum + " disconnected");

		} catch (Exception e) {
			e.printStackTrace();}

	}
	
	public BlockingQueue<Message> getMessageQueue() {
		return messageQueue;
	}

	public void write(String msg) {
		try {
			writer.write(msg + "\n");
			writer.flush();
			System.out.println(Thread.currentThread().getName() + " - Message sent to client " + clientNum);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Server getServer() {
		return server;
	}
	public Socket getClientSocket() {
		return clientSocket;
	}

	public synchronized UserInfo getClientInfo() {
		return clientInfo; 
	}

}
