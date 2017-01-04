package org.jvm.device.json;

public class OverviewJson {
	private String pid = "-1";
    private String hostName = "unknow";
    private String mainClass = "unknow";
    private String mainArgs = "unknow";
    private String vmId = "unknow";
    private String javaHome = "unknow";
    private String javaVersion = "unknow";
    private String javaVendor = "unknow";
    private String jvmFlags = "unknow";
    private String jvmArgs = "unknow";
    private String systemProperties = "unknow";
    private String uptime = "";
	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getMainClass() {
		return mainClass;
	}
	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}
	public String getMainArgs() {
		return mainArgs;
	}
	public void setMainArgs(String mainArgs) {
		this.mainArgs = mainArgs;
	}
	public String getVmId() {
		return vmId;
	}
	public void setVmId(String vmId) {
		this.vmId = vmId;
	}
	public String getJavaHome() {
		return javaHome;
	}
	public void setJavaHome(String javaHome) {
		this.javaHome = javaHome;
	}
	public String getJavaVersion() {
		return javaVersion;
	}
	public void setJavaVersion(String javaVersion) {
		this.javaVersion = javaVersion;
	}
	public String getJavaVendor() {
		return javaVendor;
	}
	public void setJavaVendor(String javaVendor) {
		this.javaVendor = javaVendor;
	}
	public String getJvmFlags() {
		return jvmFlags;
	}
	public void setJvmFlags(String jvmFlags) {
		this.jvmFlags = jvmFlags;
	}
	public String getJvmArgs() {
		return jvmArgs;
	}
	public void setJvmArgs(String jvmArgs) {
		this.jvmArgs = jvmArgs;
	}
	public String getSystemProperties() {
		return systemProperties;
	}
	public void setSystemProperties(String systemProperties) {
		this.systemProperties = systemProperties;
	}
	public String getUptime() {
		return uptime;
	}
	public void setUptime(String uptime) {
		this.uptime = uptime;
	}
    
}
