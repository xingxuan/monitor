package com.miaozhen.device.tools.vm.tools.jmx;

/**
 *
 * @author jiangzhixiong
 *
 */
public interface MBeanCacheOperations {
	
	public void flush();
	
	public int getInterval();
	
	public void addMBeanCacheListener(MBeanCacheListener listener);
	
	public void removeMBeanCacheListener(MBeanCacheListener listener);

}
