package org.jvm.device.tools.vm.host;

import org.jvm.device.tools.vm.host.impl.HostProperties;
import org.jvm.device.tools.vm.host.impl.HostProvider;

/**
 * @author jiangzhixiong
 *
 */
public final class HostsSupport {
	private static final Object hostsStorageDirectoryLock = new Object();

	private static HostsSupport instance;
	private final HostProvider hostProvider = new HostProvider();


//	private Semaphore hostsLockedSemaphore = new Semaphore(1);

    /**
     * @return
     */
    public static synchronized HostsSupport getInstance() {
        if (instance == null) instance = new HostsSupport();
        return instance;
    }
	
	Host createLocalHost() {
        return hostProvider.createLocalHost();
    }
	
	Host createUnknownHost() {
        return hostProvider.createUnknownHost();
    }
	
	public Host getOrCreateHost(String hostname, boolean interactive) {
        return createHost(new HostProperties(hostname, null), false, interactive);
    }
	
	Host createHost(HostProperties properties, boolean createOnly, boolean interactive) {
        return hostProvider.createHost(properties, createOnly, interactive);
    }

	private HostsSupport() {
		
	}
}
