package com.miaozhen.device.tools.vm.tools.jmx;

import javax.management.MBeanServerConnection;

/**
 * MBeanServerConnection
 * @author jiangzhixiong
 *
 */
public interface CachedMBeanServerConnection extends MBeanServerConnection, MBeanCacheOperations {

}
