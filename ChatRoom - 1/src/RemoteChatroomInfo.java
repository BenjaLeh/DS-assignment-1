public class RemoteChatroomInfo extends ChatroomInfo
	{
		private String managingServer;
		public RemoteChatroomInfo(String chatroomId,String managingServer){
			super(chatroomId);
			this.managingServer=managingServer;
			
		}
		public String getManagingServer() {
			return managingServer;
		}

		public void setManagingServer(String managingServer) {
			this.managingServer = managingServer;
		}
	} 	