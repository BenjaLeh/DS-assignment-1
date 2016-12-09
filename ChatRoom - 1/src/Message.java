
public class Message {

	//True if the message comes from a client, false if it comes from a thread
	private int messageType;
	private String message;
	private String room2Cast;

	
	public Message(int messageType, String message) {
		this.messageType = messageType;
		this.message = message;
		this.setRoom2Cast(null);
	}
	
	public int getMessageType() {
		return messageType;
	}
	public String getMessage() {
		return message;
	}

	public String getRoom2Cast() {
		return room2Cast;
	}

	public void setRoom2Cast(String room2Cast) {
		this.room2Cast = room2Cast;
	}


	
	
}
