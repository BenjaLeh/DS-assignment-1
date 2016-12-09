import java.util.Set;

public class LocalChatroomInfo extends ChatroomInfo
	{
		private String owner;
		private Set<String> member;
		public LocalChatroomInfo(String chatroomId,String owner,Set<String> member)
		{
			super(chatroomId);
			this.owner=owner;
			this.member=member;
			
		}
		public String getOwner() {
			return owner;
		}
		public void setOwner(String owner) {
			this.owner = owner;
		}
		public Set<String> getMember() {
			return member;
		}
		public void addMember(String memberIdentity) {
			this.member.add(memberIdentity);
			
		}
		public void deleteMember(String memberIdentity) {
			this.member.remove(memberIdentity);
		}
	}