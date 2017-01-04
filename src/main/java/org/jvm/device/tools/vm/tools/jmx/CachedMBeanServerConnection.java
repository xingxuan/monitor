package org.jvm.device.tools.vm.tools.jmx;

import javax.management.MBeanServerConnection;

/**
 * MBeanServerConnection
 * @author jiangzhixiong
 *
 */
public interface CachedMBeanServerConnection extends MBeanServerConnection, MBeanCacheOperations {

}
