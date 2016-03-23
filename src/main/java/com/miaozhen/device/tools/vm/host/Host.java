package com.miaozhen.device.tools.vm.host;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.miaozhen.device.tools.vm.core.datasource.DataSource;
import com.miaozhen.device.tools.vm.core.datasupport.Stateful;

/**
 * 主机的抽象实现
 * @author jiangzhixiong
 *
 */
public abstract class Host extends DataSource implements Stateful {
	/**
	 */
	public static final Host LOCALHOST = HostsSupport.getInstance().createLocalHost();
	/**
	 */
	public static final Host UNKNOWN_HOST = HostsSupport.getInstance().createUnknownHost();
	
	private final String hostName;
	private InetAddress inetAddress;
	private int state = STATE_AVAILABLE;
	
	/**
	 * @param hostName
	 * @throws UnknownHostException
	 */
	public Host(String hostName) throws UnknownHostException {
        this(hostName, InetAddress.getByName(hostName));
    }

	/**
	 * @param hostName
	 * @param inetAddress
	 */
	public Host(String hostName, InetAddress inetAddress) {
        if (hostName == null) throw new IllegalArgumentException("Host name cannot be null");
        if (inetAddress == null) throw new IllegalArgumentException("InetAddress cannot be null");
        
        this.hostName = hostName;
        this.inetAddress = inetAddress;
    }
	
	public String getHostName() {
        return hostName;
    }
	
	public synchronized int getState() {
        return state;
    }
	
	public final InetAddress getInetAddress() {
        return inetAddress;
    }
	
	/**
	 * 修改主机的状态
	 * @param newState
	 */
	protected final synchronized void setState(int newState) {
        int oldState = state;
        state = newState;
        getChangeSupport().firePropertyChange(PROPERTY_STATE, oldState, newState);
    }
	
	public int hashCode() {
        if (Host.UNKNOWN_HOST == this) return super.hashCode();
        InetAddress address = getInetAddress();
        if (this == LOCALHOST) return address.hashCode();
        if (address.isLoopbackAddress()) return LOCALHOST.hashCode();
        else return address.hashCode();
    }
	
	public boolean equals(Object obj) {
        if (!(obj instanceof Host)) return false;
        if (Host.UNKNOWN_HOST == this) return obj == this;
        Host host = (Host)obj;
        InetAddress thisAddress = getInetAddress();
        InetAddress otherAddress = host.getInetAddress();
        if (thisAddress.isLoopbackAddress() && otherAddress.isLoopbackAddress()) return true;
        return thisAddress.equals(otherAddress);
    }
	
	public String toString() {
        return getHostName() + " [IP: " + getInetAddress().getHostAddress() + "]";
    }
}
