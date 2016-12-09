
public class ServerInfo {
	private String serverName;
	private String address;
	private int port;
	private int managementPort;
	public ServerInfo(String serverName,String address,int port,int managementPort){
		this.serverName=serverName;
		this.address=address;
		this.setPort(port);
		this.setManagementPort(managementPort);
	}
	public void setServerName(String serverName)
	{
		this.serverName=serverName;
	}
	public String getServerName()
	{
		return serverName;
	}
	public void setaddress(String address)
	{
		this.address=address;
	}
	public String getaddress()
	{
		return address;
	}
	
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public int getManagementPort() {
		return managementPort;
	}
	public void setManagementPort(int managementPort) {
		this.managementPort = managementPort;
	}

}
