package com.miaozhen.device.tools.vm.host.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.miaozhen.device.tools.vm.host.Host;

public class HostProvider {
	private static final Logger LOGGER = Logger.getLogger(HostProvider.class.getName());
	
    private static final String PROPERTY_HOSTNAME = "prop_hostname";

    private static InetAddress localhostAddress2;
    
    private Semaphore hostsLockedSemaphore = new Semaphore(1);
    
    /**
     * @return
     */
    public Host createLocalHost() {
        try {
            return new LocalHostImpl();
        } catch (UnknownHostException e) {
            LOGGER.severe("Critical failure: cannot resolve localhost");
            return null;
        }
    }
    
    /**
     * @return
     */
    public Host createUnknownHost() {
        try {
            return new Host("unknown", InetAddress.getByAddress(new byte[] { 0, 0, 0, 0 })) {};
        } catch (UnknownHostException e) {
            LOGGER.severe("Failure: cannot resolve <unknown> host");
            return null;
        }
    }
    
    /**
     * @param hostDescriptor
     * @param createOnly
     * @param interactive
     * @return
     */
    public Host createHost(final HostProperties hostDescriptor, final boolean createOnly, final boolean interactive) {
    	try {
    		lockHosts();
    		
    		final String hostName = hostDescriptor.getHostName();
            InetAddress inetAddress = null;
            try {
                inetAddress = InetAddress.getByName(hostName);
            } catch (UnknownHostException e) {
            	e.printStackTrace();
            }
            if (inetAddress != null) {
            	//String ipString = inetAddress.getHostAddress();

            	//TODO 
				Host newHost = null;
				try {
					newHost = new RemoteHostImpl(hostName);
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE, "Error creating host", e);
				}

				return newHost;
            }
            
            return null;
    	}catch (InterruptedException ex) {
            LOGGER.throwing(HostProvider.class.getName(), "createHost", ex);
            return null;
        } finally {
            unlockHosts();
        }
    }
    
    private Host getHostByAddressImpl(InetAddress inetAddress) {
    	return null;
    }
    
	private void lockHosts() throws InterruptedException {
		hostsLockedSemaphore.acquire();
	}

	private void unlockHosts() {
		hostsLockedSemaphore.release();
	}
	
	public HostProvider() {
		
	}

}
