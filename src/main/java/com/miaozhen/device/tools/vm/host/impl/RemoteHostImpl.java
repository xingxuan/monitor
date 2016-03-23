package com.miaozhen.device.tools.vm.host.impl;

import java.net.UnknownHostException;

import com.miaozhen.device.tools.vm.host.Host;

/**
 * @author jiangzhixiong
 *
 */
final class RemoteHostImpl extends Host {
	
	/**
	 * @param hostName
	 * @throws UnknownHostException
	 */
	RemoteHostImpl(String hostName) throws UnknownHostException {
        super(hostName);
    }
	
	public boolean supportsUserRemove() {
        return true;
    }

}
