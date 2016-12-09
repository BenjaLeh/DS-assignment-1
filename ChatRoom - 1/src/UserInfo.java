import java.net.Socket;

public class UserInfo {
	private String identity;
	private String currentChatroom;
	private boolean is_owner;
	private Socket usersocket;
	private ClientListenThread managingThread;
	
	public UserInfo(String identity,String currentChatroom,Socket usersocket,ClientListenThread managingThread)
	{
		this.identity = identity;
		this.currentChatroom = currentChatroom;
		this.usersocket = usersocket;
		this.is_owner=false;
		this.managingThread = managingThread;
	}
	
	public String getIdentity() {
		return identity;
	}
	public void setIdentity(String identity) {
		this.identity = identity;
	}
	public String getCurrentChatroom() {
		return currentChatroom;
	}
	public void setCurrentChatroom(String currentChatroom) {
		this.currentChatroom = currentChatroom;
	}

	public ClientListenThread getManagingThread() {
		return managingThread;
	}
	public void setManagingThread(ClientListenThread managingThread) {
		this.managingThread = managingThread;
	}
	public Socket getUsersocket() {
		return usersocket;
	}
	public void setUsersocket(Socket usersocket) {
		this.usersocket = usersocket;
	}
	public boolean is_owner(){
		return is_owner;
	}
	public void set_owner(boolean t_f){
		this.is_owner=t_f;
		
	}
}
