
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ClientMessageReader extends Thread {

	private BufferedReader reader; 
	private BlockingQueue<Message> messageQueue;
	private ClientListenThread managingThread;
	
	public ClientListenThread getManagingThread(){
		return managingThread;
	}
	
	public ClientMessageReader(ClientListenThread managingThread,BufferedReader reader, BlockingQueue<Message> messageQueue) {
		this.managingThread=managingThread;
		this.reader = reader;
		this.messageQueue = messageQueue;
	}
	
	@Override
	//This thread reads messages from the client's socket input stream
	public void run() {
		try {
			
			System.out.println(Thread.currentThread().getName() 
					+ " - Reading messages from client connection");
			
			String clientMsg = null;
			String clientMsgBrocast=null;
			String a=null;
			while ((a=reader.readLine())!=null ){
				
				JSONParser parser = new JSONParser();
				JSONObject comingJsonObj = (JSONObject) parser.parse(a);
				String type = (String) comingJsonObj.get("type");
				
				JSONObject jMsg = new JSONObject();
				JSONObject jMsgBroadcast=new JSONObject();
				Message msg=null;
				Message msgBroadcast=null;
				ServerRequest requestor=new ServerRequest(this.getManagingThread().getServer());
					
					
					if(type.equals("newidentity"))
					{		
						
							String identity =(String) comingJsonObj.get("identity");
							if ((identity.matches("([A-Za-z0-9]+)"))&&
								(identity.length()>2)&&
								(identity.length()<17)&&
								(identity.toUpperCase().charAt(0)<='Z')&&
								(!this.getManagingThread().getServer().userlist.containsKey(identity)))
							{	
								
								if(requestor.idRequest(identity))
								{
									this.getManagingThread().getClientInfo().setIdentity(identity);
									String mainhall=this.getManagingThread().getServer().getMainhall().getChatroomId();
									this.getManagingThread().getClientInfo().setCurrentChatroom(mainhall);
									//更新用户信息
									this.getManagingThread().getServer().localChatroomList.get(mainhall).addMember(identity);
									//加入房间
									this.getManagingThread().getServer().userlist.put(identity, this.getManagingThread().getClientInfo());
									//服务器用户名单注册
									msg = new jsonMessage().idfeedBack(true);
									messageQueue.add(msg);
									//单播同意回执

									msgBroadcast=new jsonMessage().roomeChange(identity, "", mainhall, mainhall);
									messageQueue.add(msgBroadcast);
									//广播变更房间
								}
								else
								{
									msg = new jsonMessage().idfeedBack(false);
									messageQueue.add(msg);
									//单播拒绝回执
								}
							}
							else
							{
								msg = new jsonMessage().idfeedBack(false);
								messageQueue.add(msg);
								//单播拒绝回执
							}
						}

//					new Id
					
					
					
					
					else if(type.equals("createroom"))
					{
							String roomid =(String) comingJsonObj.get("roomid");
							String currentroomid=this.getManagingThread().getClientInfo().getCurrentChatroom();
							LocalChatroomInfo currentroom=this.getManagingThread().getServer().localChatroomList.get(currentroomid);
							String owner=currentroom.getOwner();
							String userid=this.getManagingThread().getClientInfo().getIdentity();
							if ((roomid.matches("([A-Za-z0-9]+)"))&&
								(roomid.length()>2)&&
								(roomid.length()<17)&&
								(roomid.toUpperCase().charAt(0)<='Z')&&
								(!userid.equals(owner))&&
								(!this.getManagingThread().getServer().lockedChatroom.contains(roomid))&&
								(!this.getManagingThread().getServer().localChatroomList.containsKey(roomid)))
								
							{
								
								if(requestor.roomRequest(roomid))
									// 服务器通过						
								{
									LocalChatroomInfo newchatroom=new LocalChatroomInfo(roomid,userid,new HashSet<String>());
									newchatroom.addMember(userid);
									//构造新房
									this.getManagingThread().getServer().userlist.get(userid).setCurrentChatroom(roomid);
									this.getManagingThread().getServer().localChatroomList.put(roomid, newchatroom);
									this.getManagingThread().getServer().localChatroomList.get(currentroomid).deleteMember(userid);
									//注册到ChatroomList
									this.getManagingThread().getClientInfo().setCurrentChatroom(roomid);
									this.getManagingThread().getClientInfo().set_owner(true);
									//更新用户信息

									msg = new jsonMessage().roomfeedBack("createroom", roomid, true); 
									messageQueue.add(msg);
									//单播同意回执
									msgBroadcast= new jsonMessage().roomeChange(userid, currentroomid, roomid, currentroomid);
									messageQueue.add(msgBroadcast);
									Message msgBroadcast2= new jsonMessage().roomeChange(userid, currentroomid, roomid, roomid);
									messageQueue.add(msgBroadcast2);
									//广播变更房间
								}
								else
								{
									msg = new jsonMessage().roomfeedBack("createroom", roomid, false);
									messageQueue.add(msg);
									//单播拒绝回执
								}
							}
							else
							{
								jMsg.put("type","createroom");
								jMsg.put("roomid",roomid);
								jMsg.put("approved", "false");
								clientMsg=jMsg.toJSONString();
								msg = new Message(1, clientMsg);
								messageQueue.add(msg);
								//单播拒绝回执
							}		
					}
					
					
					else if(type.equals("join"))
					{ 
						String roomid =(String) comingJsonObj.get("roomid");
						Set localRoomlist=this.getManagingThread().getServer().localChatroomList.keySet();
						Set remoteRoomlist=this.getManagingThread().getServer().remoteChatroomList.keySet();
						String currentroomid=this.getManagingThread().getClientInfo().getCurrentChatroom();
						LocalChatroomInfo currentroom=this.getManagingThread().getServer().localChatroomList.get(currentroomid);
						String owner=currentroom.getOwner();
						String userid=this.getManagingThread().getClientInfo().getIdentity();
						if ((!roomid.equals(currentroomid))&&(!owner.equals(userid))&&((localRoomlist.contains(roomid))||(remoteRoomlist.contains(roomid))))
						{
//							localServer情况
							if(localRoomlist.contains(roomid))
							{
								this.getManagingThread().getClientInfo().setCurrentChatroom(roomid);
								this.getManagingThread().getServer().userlist.get(userid).setCurrentChatroom(roomid);
								//更新用户信息
								this.getManagingThread().getServer().localChatroomList.get(currentroomid).deleteMember(userid);
								this.getManagingThread().getServer().localChatroomList.get(roomid).addMember(userid);
								//
								msgBroadcast=new jsonMessage().roomeChange(userid, currentroomid, roomid, currentroomid);
								messageQueue.add(msgBroadcast);
								Message msgBroadcast2=new jsonMessage().roomeChange(userid, currentroomid, roomid, roomid);
								messageQueue.add(msgBroadcast2);
							}
							else
							{	
								LocalChatroomInfo source=this.getManagingThread().getServer().localChatroomList.get(currentroomid);
								source.deleteMember(userid);
								//房间除名
								this.getManagingThread().getServer().userlist.remove(userid);
								//服务器除名
								RemoteChatroomInfo destination=this.getManagingThread().getServer().remoteChatroomList.get(roomid);
							    ServerInfo destServer=this.getManagingThread().getServer().serverlist.get(destination.getManagingServer());
								msgBroadcast=new jsonMessage().roomeChange(userid, currentroomid, roomid, currentroomid);
								messageQueue.add(msgBroadcast);
								//广播变更通知
							    jMsg.put("type","route");
								jMsg.put("roomid",roomid);
								jMsg.put("host",destServer.getaddress());
								jMsg.put("port",Integer.toString(destServer.getPort()));
								clientMsg=jMsg.toJSONString();
								msg=new Message(1,clientMsg);
								messageQueue.add(msg);
								//单播路由通知
							}
						}
						else
						{
							msg=new jsonMessage().roomeChange(userid, currentroomid, currentroomid);
							messageQueue.add(msg);
						}
					}
					
					else if(type.equals("deleteroom"))
					{
						String roomid =(String) comingJsonObj.get("roomid");
						String owner=this.getManagingThread().getServer().localChatroomList.get(roomid).getOwner();
						String userid=this.getManagingThread().getClientInfo().getIdentity();
						String mainhall=this.getManagingThread().getServer().getMainhall().getChatroomId();
						
							if(userid.equals(owner))
							{	
								List<ClientListenThread> connectedClients = ServerState.getInstance().getConnectedClients();
								List<String> nameList=new ArrayList<String>();
								for(ClientListenThread client : connectedClients)
								{	
									if (client.getClientInfo().getCurrentChatroom().equals(roomid))
									{
										client.getClientInfo().setCurrentChatroom(mainhall);
										String clientName=client.getClientInfo().getIdentity();
										nameList.add(clientName);
										this.getManagingThread().getServer().getMainhall().addMember(clientName);
									}
								}
//								更新所有房间成员信息
								for(String name : nameList)
								{	
									msgBroadcast=new jsonMessage().roomeChange(name, roomid, mainhall,mainhall);
									messageQueue.add(msgBroadcast);
								}
//								Server Communication
								this.getManagingThread().getServer().localChatroomList.remove(roomid);
								this.getManagingThread().getClientInfo().set_owner(false);
								msg=new jsonMessage().roomfeedBack("deleteroom", roomid, true);
								messageQueue.add(msg);
								requestor.deleteRoomRequest(roomid);
							}
							else
							{
								msg=new jsonMessage().roomfeedBack("deleteroom", roomid, false);
								messageQueue.add(msg);	
							}

						}

					
					
					
					else if(type.equals("list"))
					{
							JSONArray chatroomlist=new JSONArray();
							chatroomlist.addAll(this.getManagingThread().getServer().localChatroomList.keySet());
							chatroomlist.addAll(this.getManagingThread().getServer().remoteChatroomList.keySet());
							jMsg.put("type","roomlist");
							jMsg.put("rooms",chatroomlist);
							clientMsg=jMsg.toJSONString();
							msg = new Message(1, clientMsg);
							messageQueue.add(msg);
							//单播房间列表
					}
					else if(type.equals("who"))
					{
						String croom = this.getManagingThread().getClientInfo().getCurrentChatroom();
						String owner =this.getManagingThread().getServer().localChatroomList.get(croom).getOwner();
						JSONArray userlist=new JSONArray();
						userlist.addAll(this.getManagingThread().getServer().localChatroomList.get(croom).getMember());
						jMsg.put("type","roomcontents");
						jMsg.put("roomid",this.getManagingThread().getClientInfo().getCurrentChatroom());
						jMsg.put("identities",userlist);
						jMsg.put("owner",owner);
						clientMsg=jMsg.toJSONString();
						msg = new Message(1, clientMsg);
						messageQueue.add(msg);
							//单播用户列表
					}
					else if(type.equals("message"))
					{		String currentRoomid=this.getManagingThread().getClientInfo().getCurrentChatroom();
							comingJsonObj.put("identity",this.getManagingThread().getClientInfo().getIdentity());
							clientMsgBrocast=comingJsonObj.toJSONString();
							msgBroadcast= new Message(2, clientMsgBrocast);
							msgBroadcast.setRoom2Cast(currentRoomid);
							messageQueue.add(msgBroadcast);
							//广播内容
					}
					else if(type.equals("quit"))
					{
						quit();
						
					}
					else if(type.equals("movejoin"))
					{
						String roomid =(String) comingJsonObj.get("roomid");
						String identity=(String) comingJsonObj.get("identity");
						String former =(String) comingJsonObj.get("former");
						if (!this.getManagingThread().getServer().localChatroomList().containsKey(roomid))
						{
							roomid=this.getManagingThread().getServer().getMainhall().getChatroomId();
						}
						this.getManagingThread().getClientInfo().setIdentity(identity);
						this.getManagingThread().getClientInfo().setCurrentChatroom(roomid);
						//更新用户信息
						this.getManagingThread().getServer().localChatroomList.get(roomid).addMember(identity);
						//加入房间
						this.getManagingThread().getServer().userlist.put(identity, this.getManagingThread().getClientInfo());
						//注册用户身份
						msgBroadcast=new jsonMessage().roomeChange(identity, former, roomid, roomid);
						messageQueue.add(msgBroadcast);
						//广播换房通知						
						jMsg.put("type","serverchange");
						jMsg.put("serverid",this.getManagingThread().getServer().serverInfo.getServerName());
						jMsg.put("approved","true");
						clientMsg=jMsg.toJSONString();
						msg=new Message(1,clientMsg);
						messageQueue.add(msg);
						//单播成功回执						
					}
				}
			quit();
			Message exit = new Message(0, "");
			messageQueue.add(exit);		
			} 
		catch (Exception e) {
			quit();
			Message exit = new Message(0, "exit");
			messageQueue.add(exit);
		}
	}
	public void quit()
	{	
		ServerRequest requestor=new ServerRequest(this.getManagingThread().getServer());
		Message msgBroadcast=null;
		String roomid =this.getManagingThread().getClientInfo().getCurrentChatroom();
		String userid=this.getManagingThread().getClientInfo().getIdentity();
		String mainhall=this.getManagingThread().getServer().getMainhall().getChatroomId();
		if (this.getManagingThread().getClientInfo().is_owner())
		{
			List<ClientListenThread> connectedClients = ServerState.getInstance().getConnectedClients();
			List<String> nameList=new ArrayList<String>();
			for(ClientListenThread client : connectedClients)
			{	if (client.getClientInfo().getCurrentChatroom().equals(roomid))
				{
					client.getClientInfo().setCurrentChatroom(mainhall);
					String clientName=client.getClientInfo().getIdentity();
					nameList.add(clientName);
					this.getManagingThread().getServer().getMainhall().addMember(clientName);
				}
			}
			for(String name : nameList)
			{	
				msgBroadcast=new jsonMessage().roomeChange(name, roomid, mainhall, mainhall);
				messageQueue.add(msgBroadcast);
			}
			this.getManagingThread().getServer().localChatroomList.remove(roomid);
			this.getManagingThread().getClientInfo().set_owner(false);
			requestor.deleteRoomRequest(roomid);
		}
		this.getManagingThread().getServer().userlist.remove(userid);
		roomid =this.getManagingThread().getClientInfo().getCurrentChatroom();
		if (roomid!=null)
		{	this.getManagingThread().getServer().localChatroomList().get(roomid).deleteMember(userid);
		msgBroadcast=new jsonMessage().roomeChange(userid, roomid, "", roomid);
		messageQueue.add(msgBroadcast);
		}
	}
	
	
}
