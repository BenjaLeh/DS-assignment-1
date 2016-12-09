import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ServerConnectionThread extends Thread{
	private Server receiveServer;
	private Socket comingsocket;
	private int serverNum;
	private BufferedReader reader;
	private BufferedWriter writer;
	public ServerConnectionThread(Server receiveServer,Socket comingsocket,int serverNum)
	{
		try 
		{	this.receiveServer=receiveServer;
			this.comingsocket=comingsocket;
			this.serverNum=comingsocket.getLocalPort()+serverNum;
			reader = new BufferedReader(new InputStreamReader(comingsocket.getInputStream(), "UTF-8"));
			writer = new BufferedWriter(new OutputStreamWriter(comingsocket.getOutputStream(), "UTF-8"));
		}
		catch (Exception e) 
		{
				e.printStackTrace();
		}
	}
	@Override
	public void run() 
	{
		try {
			System.out.println(Thread.currentThread().getName() 
					+ " - Reading messages from Server connection");
			String requestId=null;
			String requestIdr=null;
			while (true) { 
				
				JSONParser parser = new JSONParser();
				JSONObject comingJsonObj = (JSONObject) parser.parse(reader.readLine());
				System.out.println(comingJsonObj.toJSONString());
				String type = (String) comingJsonObj.get("type");
				String serverid=(String) comingJsonObj.get("serverid");
				JSONObject jMsg = new JSONObject();
				
				if(type.equals("lockidentity"))
				{	
					String identity=(String) comingJsonObj.get("identity");
					requestId=(String) comingJsonObj.get("serverid");
					jMsg.put("type", type);
					jMsg.put("identity", identity);
					jMsg.put("serverid", this.receiveServer.serverInfo.getServerName());
					String locked="false";
					
					if ((!this.receiveServer.userlist.containsKey(identity))&&(!this.receiveServer.lockedIdentity.contains(identity)))
					{
						System.out.println(this.receiveServer.userlist.containsKey(identity));
						System.out.println(this.receiveServer.lockedIdentity.contains(identity));
						locked="true";
						this.receiveServer.lockedIdentity.add(identity);
						
					}
					jMsg.put("locked", locked);
					writer.write(jMsg.toJSONString()+"\n");
					writer.flush();
					System.out.println(jMsg.toJSONString());
				}
				if(type.equals("releaseidentity")&&requestId.equals(serverid))
				{	
					
					String identity=(String) comingJsonObj.get("identity");
					{
						this.receiveServer.lockedIdentity.remove(identity);
					}
					break;
				}
				if(type.equals("lockroomid"))
				{
					String roomid=(String) comingJsonObj.get("roomid");
					requestId=(String) comingJsonObj.get("serverid");
					jMsg.put("type", type);
					jMsg.put("identity", roomid);
					jMsg.put("serverid", this.receiveServer.serverInfo.getServerName());
					String locked="false";
					if (!this.receiveServer.localChatroomList.containsKey(roomid)&&!(this.receiveServer.lockedChatroom.contains(roomid)))
					{
						locked="true";
						this.receiveServer.lockedIdentity.add(roomid);
					}
					jMsg.put("locked", locked);
					writer.write(jMsg.toJSONString()+"\n");
					writer.flush();
				}
				if(type.equals("releaseroomid")&&serverid.equals(requestId))
				{
					
					String roomid=(String) comingJsonObj.get("roomid");
					String approved=(String) comingJsonObj.get("approved");
					this.receiveServer.lockedChatroom.remove(roomid);
					if (approved.equals("true"))
					{	
						this.receiveServer.remoteChatroomList.put(roomid, new RemoteChatroomInfo(roomid,serverid));
					}
					break;
				}
				if(type.equals("deleteroom"))
				{	
					
					String roomid=(String) comingJsonObj.get("roomid");
					this.receiveServer.remoteChatroomList.remove(roomid);
					System.out.println("deleteroom"+roomid);
					break;
				}
			}
			this.comingsocket.close();
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void write(String msg) 
	{
		try 
		{
			writer.write(msg + "\n");
			writer.flush();
			System.out.println(Thread.currentThread().getName() + " - Message sent to Server " + "KKK");
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}

