package com.miaozhen.device.tools.vm.tools.jmx;

import java.util.EventListener;

/**
 * 
 * @author jiangzhixiong
 *
 */
public interface MBeanCacheListener extends EventListener {

	/**
     * 
     */
    public void flushed();
}
