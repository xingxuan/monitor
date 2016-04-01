package cn.thextrader.monitor;

/**
 * 监控机与被监控应用的基础信息
 * 
 * @author
 *
 */
public class UserJmx {
	private int id;
	private String erp;
	private String jmxUrl;
	private String applicationName;// 被监控应用名称
	private String jmxUsername;
	private String jmxPwd;
	private int state;// 0:连接状态，-2:失去链接
	private String serverName;// 监控机名称
	private String monitorIp;// 监控机IP
	private String monitorPort;// 监控机PORT
	private String createTime;
	private String to;
	private int reconnNum;// 尝试重连次数，达到条件后，则不再尝试连接


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getErp() {
		return erp;
	}

	public void setErp(String erp) {
		this.erp = erp;
	}

	public void setJmxUrl(String jmxUrl) {
		this.jmxUrl = jmxUrl;
	}

	public String getJmxUrl() {
		return jmxUrl;
	}

	public void setJmxUsername(String jmxUsername) {
		this.jmxUsername = jmxUsername;
	}

	public String getJmxUsername() {
		return jmxUsername;
	}

	public void setJmxPwd(String jmxPwd) {
		this.jmxPwd = jmxPwd;
	}

	public String getJmxPwd() {
		return jmxPwd;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getServerName() {
		return serverName;
	}

	public void setMonitorIp(String monitorIp) {
		this.monitorIp = monitorIp;
	}

	public void setMonitorPort(String monitorPort) {
		this.monitorPort = monitorPort;
	}

	public String getMonitorIp() {
		return monitorIp;
	}

	public String getMonitorPort() {
		return monitorPort;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getTo() {
		return to;
	}

	public int getReconnNum() {
		return reconnNum;
	}

	public void setReconnNum(int reconnNum) {
		this.reconnNum = reconnNum;
	}

}
