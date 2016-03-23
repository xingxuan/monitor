package com.miaozhen.device.tools.vm.host.impl;

import java.net.UnknownHostException;

import com.miaozhen.device.tools.vm.host.Host;
/**
 * @author jiangzhixiong
 *
 */
final class LocalHostImpl extends Host {

	/**
	 * @throws UnknownHostException
	 */
	public LocalHostImpl() throws UnknownHostException {
		super("localhost");
	}

}
