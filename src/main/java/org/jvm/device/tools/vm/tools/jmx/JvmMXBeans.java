package org.jvm.device.tools.vm.tools.jmx;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Collection;
import java.util.logging.LoggingMXBean;

import javax.management.ObjectName;

/**
 * 对JvmMXBeans信息获取的接口
 * @author jiangzhixiong
 *
 */
public interface JvmMXBeans extends MBeanCacheOperations {
	
	/**
	 * JVM类加载的MXBean
	 * @return
	 */
	public ClassLoadingMXBean getClassLoadingMXBean();
	
	/**
	 * JVM编译的MXBean
	 * @return
	 */
	public CompilationMXBean getCompilationMXBean();
	
	public LoggingMXBean getLoggingMXBean();
	
	public Collection<GarbageCollectorMXBean> getGarbageCollectorMXBeans();
	
	public Collection<MemoryManagerMXBean> getMemoryManagerMXBeans();
	
	public MemoryMXBean getMemoryMXBean();
	
	public Collection<MemoryPoolMXBean> getMemoryPoolMXBeans();
	
	public OperatingSystemMXBean getOperatingSystemMXBean();
	
	public RuntimeMXBean getRuntimeMXBean();
	
	public ThreadMXBean getThreadMXBean();
	
	public <T> T getMXBean(ObjectName objectName, Class<T> interfaceClass);

}
