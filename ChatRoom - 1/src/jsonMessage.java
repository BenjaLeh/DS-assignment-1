import org.json.simple.JSONObject;

public class jsonMessage {
//	public void jsonMessage(){
//		this.jsonMessage();
//	}
	public Message roomeChange(String id, String former, String to, String room2cast)
	{
		JSONObject jMsgBroadcast=new JSONObject();
		jMsgBroadcast.put("type","roomchange");
		jMsgBroadcast.put("identity",id);
		jMsgBroadcast.put("former",former);
		jMsgBroadcast.put("roomid",to);
		Message msgBroadcast=new Message(2,jMsgBroadcast.toJSONString());
		msgBroadcast.setRoom2Cast(room2cast);
		return msgBroadcast;
	}
	
	public Message roomeChange(String id, String former, String to)
	{
		JSONObject jMsgBroadcast=new JSONObject();
		jMsgBroadcast.put("type","roomchange");
		jMsgBroadcast.put("identity",id);
		jMsgBroadcast.put("former",former);
		jMsgBroadcast.put("roomid",to);
		Message msgBroadcast=new Message(1,jMsgBroadcast.toJSONString());
		return msgBroadcast;
	}
	
	public Message roomfeedBack(String type, String what, boolean approved){
		String a= Boolean.toString(approved);
		JSONObject jMsg=new JSONObject();
		jMsg.put("type",type);
		jMsg.put("roomid",what);
		jMsg.put("approved",a);
		Message msg=new Message(1,jMsg.toJSONString());
		return msg;
	}
	
	public Message idfeedBack(boolean approved){
		String a= Boolean.toString(approved);
		JSONObject jMsg=new JSONObject();
		jMsg.put("type","newidentity");
		jMsg.put("approved",a);
		Message msg=new Message(1,jMsg.toJSONString());
		return msg;
	}

}
