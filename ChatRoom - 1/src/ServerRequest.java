import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ServerRequest {
	private Server requestingServer;
	private String id2Request;
	private String room2Request;
	public ServerRequest(Server requestingServer){
		this.requestingServer=requestingServer;
	}
	
	public boolean idRequest(String identity)
	{	
		this.id2Request=identity;
		this.requestingServer.lockedIdentity.add(id2Request);
		boolean lockstate=true;
		Socket[] sSocket=new Socket[this.requestingServer.serverlist.keySet().size()];
		BufferedWriter[] Writer=new BufferedWriter[this.requestingServer.serverlist.keySet().size()];
		BufferedReader[] Reader=new BufferedReader[this.requestingServer.serverlist.keySet().size()];
		int socketNum=0;
		try {
			
			for (ServerInfo targetServer : this.requestingServer.serverlist.values()) 
			{
				String targetServerAddress =targetServer.getaddress();
				int targetServerPort=targetServer.getManagementPort();
				sSocket[socketNum]= new Socket(targetServerAddress, targetServerPort);
				Reader[socketNum] = new BufferedReader(new InputStreamReader(sSocket[socketNum].getInputStream(), "UTF-8"));
				Writer[socketNum] = new BufferedWriter(new OutputStreamWriter(sSocket[socketNum].getOutputStream(), "UTF-8"));
				socketNum++;
			}
			JSONParser parser=new JSONParser();
			JSONObject jIdRequest=new JSONObject();
			jIdRequest.put("type", "lockidentity");
			jIdRequest.put("serverid",this.requestingServer.serverInfo.getServerName());
			jIdRequest.put("identity",id2Request);
			
			for (int i=0;i<socketNum;i++){
				Writer[i].write(jIdRequest.toJSONString()+"\n");
				Writer[i].flush();
				System.out.println("Id问询"+i);
				System.out.println(jIdRequest.toJSONString());
				JSONObject comingJsonObj = (JSONObject) parser.parse(Reader[i].readLine());
				String type = (String) comingJsonObj.get("type");
				String locked = (String) comingJsonObj.get("locked");
				lockstate=lockstate&&locked.equals("true");
			}
			System.out.println("反馈结束"+lockstate);
			
			this.requestingServer.lockedIdentity.remove(id2Request);
			jIdRequest=new JSONObject();
			jIdRequest.put("type", "releaseidentity");
			jIdRequest.put("serverid", this.requestingServer.serverInfo.getServerName());
			jIdRequest.put("identity", id2Request);
			System.out.println("通知解锁");
			for (int i=0;i<socketNum;i++){
				Writer[i].write(jIdRequest.toJSONString()+"\n");
				Writer[i].flush();
				System.out.println(jIdRequest.toJSONString());
			}
			for (int i=0;i<socketNum;i++){
				sSocket[i].close();
			}
			System.out.println("问询结束");
			return lockstate;
		}
			catch (UnknownHostException e) {e.printStackTrace();return !lockstate;} 
		  	catch (IOException e) {e.printStackTrace();return !lockstate;} 
			catch (ParseException e) {e.printStackTrace();return !lockstate;}
	}
	
	public boolean roomRequest(String roomid) 
	{	
		this.room2Request=roomid;
		this.requestingServer.lockedChatroom.add(room2Request);
		Socket[] sSocket=new Socket[this.requestingServer.serverlist.keySet().size()];
		BufferedWriter[] Writer=new BufferedWriter[this.requestingServer.serverlist.keySet().size()];
		BufferedReader[] Reader=new BufferedReader[this.requestingServer.serverlist.keySet().size()];
		int socketNum=0;
		boolean lockstate=true;
	try{
		for (ServerInfo targetServer : this.requestingServer.serverlist.values()) 
		{
		   
		    String targetServerAddress =targetServer.getaddress();
		    int targetServerPort=targetServer.getManagementPort();
		    sSocket[socketNum]= new Socket(targetServerAddress, targetServerPort);
		    Reader[socketNum] = new BufferedReader(new InputStreamReader(sSocket[socketNum].getInputStream(), "UTF-8"));
			Writer[socketNum] = new BufferedWriter(new OutputStreamWriter(sSocket[socketNum].getOutputStream(), "UTF-8"));
			socketNum++;
		}
		JSONObject jIdRequest=new JSONObject();
		JSONParser parser=new JSONParser();
		jIdRequest.put("type", "lockroomid");
		jIdRequest.put("serverid",this.requestingServer.serverInfo.getServerName());
		jIdRequest.put("roomid",room2Request);
		for (int i=0;i<socketNum;i++){
			Writer[i].write(jIdRequest.toJSONString()+"\n");
			Writer[i].flush();
			System.out.println("Room问询"+i);
			System.out.println(jIdRequest.toJSONString());
			JSONObject comingJsonObj = (JSONObject) parser.parse(Reader[i].readLine());
			String type = (String) comingJsonObj.get("type");
			String locked = (String) comingJsonObj.get("locked");
			lockstate=lockstate&&locked.equals("true");
		}
		System.out.println("反馈结束"+lockstate);
		
		this.requestingServer.lockedChatroom.remove(room2Request);
		String approved=Boolean.toString(lockstate);
		jIdRequest=new JSONObject();
		jIdRequest.put("type", "releaseroomid");
		jIdRequest.put("serverid", this.requestingServer.serverInfo.getServerName());
		jIdRequest.put("roomid", room2Request);
		jIdRequest.put("approved", approved);
		for (int i=0;i<socketNum;i++){
			Writer[i].write(jIdRequest.toJSONString()+"\n");
			Writer[i].flush();
		}
		for (int i=0;i<socketNum;i++){
			sSocket[i].close();
		}
		return lockstate;
	} 
		catch (UnknownHostException e) {e.printStackTrace();return !lockstate;} 
	  	catch (IOException e) {e.printStackTrace();return !lockstate;} 
		catch (ParseException e) {e.printStackTrace();return !lockstate;}
	}
	public void deleteRoomRequest(String room2Delete) {
		Socket[] sSocket=new Socket[this.requestingServer.serverlist.keySet().size()];
		BufferedWriter[] Writer=new BufferedWriter[this.requestingServer.serverlist.keySet().size()];
		BufferedReader[] Reader=new BufferedReader[this.requestingServer.serverlist.keySet().size()];
		int socketNum=0;
		try {
		for (ServerInfo targetServer : this.requestingServer.serverlist.values()) 
		{
		    String targetServerAddress =targetServer.getaddress();
		    int targetServerPort=targetServer.getManagementPort();
		   
			sSocket[socketNum]= new Socket(targetServerAddress, targetServerPort);
		    Reader[socketNum] = new BufferedReader(new InputStreamReader(sSocket[socketNum].getInputStream(), "UTF-8"));
			Writer[socketNum] = new BufferedWriter(new OutputStreamWriter(sSocket[socketNum].getOutputStream(), "UTF-8"));
			socketNum++;
		}
		JSONObject jIdRequest=new JSONObject();
		jIdRequest.put("type", "deleteroom");
		jIdRequest.put("serverid",this.requestingServer.serverInfo.getServerName());
		jIdRequest.put("roomid",room2Delete);
		for (int i=0;i<socketNum;i++){
			Writer[i].write(jIdRequest.toJSONString()+"\n");
			Writer[i].flush();
		}
		for (int i=0;i<socketNum;i++){
			sSocket[i].close();
		}
		} catch (UnknownHostException e) {e.printStackTrace();
		} catch (IOException e) {e.printStackTrace();}
	}

}
