import java.util.ArrayList;
import java.util.List;

public class ServerState {

	private static ServerState instance;
	private List<ClientListenThread> connectedClients;
	
	private ServerState() {
		connectedClients = new ArrayList<>();
	}
	
	public static synchronized ServerState getInstance() {
		if(instance == null) {
			instance = new ServerState();
		}
		return instance;
	}
	
	public synchronized void clientConnected(ClientListenThread client) {
		connectedClients.add(client);
	}
	
	public synchronized void clientDisconnected(ClientListenThread client) {
		connectedClients.remove(client);
	}
	
	public synchronized List<ClientListenThread> getConnectedClients() {
		return connectedClients;
	}
}
