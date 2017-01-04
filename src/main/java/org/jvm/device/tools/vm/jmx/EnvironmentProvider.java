package org.jvm.device.tools.vm.jmx;


/**
 * @author jiangzhixiong
 *
 */
public class EnvironmentProvider {
	private String username;
	private String password;
	
	public String getPassword() {
		return password;
	}
	public String getUsername() {
		return username;
	}
	public EnvironmentProvider(String username,String password){
		this.username = username;
		this.password = password;
	}
}
