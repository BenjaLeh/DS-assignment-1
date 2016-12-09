import org.kohsuke.args4j.Option;

public class CmdLineArgs {

	@Option(required = true, name = "-n", aliases = {"--serverid"}, usage = "ServerId")
	private String serverid;
	
	@Option(required = true, name = "-l", usage = "Server Config")
	private String severs_conf;

	public String getServerid() {
		return serverid;
	}

	public String getSevers_conf() {
		return severs_conf;
	}
	
}
