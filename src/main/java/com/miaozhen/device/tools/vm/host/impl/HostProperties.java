package com.miaozhen.device.tools.vm.host.impl;

/**
 * @author jiangzhixiong
 *
 */
public class HostProperties {
	private final String hostName;
	private final String displayName;

	public HostProperties(String hostName, String displayName) {
		this.hostName = hostName;
		this.displayName = displayName;
	}

	public String getHostName() {
		return hostName;
	}

	public String getDisplayName() {
		return displayName;
	}

}
