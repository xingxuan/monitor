package com.miaozhen.device.tools.vm.tools.jmx;

import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import static java.lang.management.ManagementFactory.*;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.LoggingMXBean;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * 获取JvmMXBeans实现的工厂类
 * @author jiangzhixiong
 *
 */
public final class JvmMXBeansFactory {
	private JvmMXBeansFactory() {
	}

	public static JvmMXBeans getJvmMXBeans(MBeanServerConnection mbsc) {
		return new JvmMXBeansImpl(mbsc);
	}

	public static JvmMXBeans getJvmMXBeans(MBeanServerConnection mbsc,
			int interval) throws IllegalArgumentException {
		if (interval < 0) {
			throw new IllegalArgumentException("interval cannot be negative");
		}
		return new JvmMXBeansImpl(CachedMBeanServerConnectionFactory.getCachedMBeanServerConnection(mbsc, interval));
	}
	
	/**
	 * 通过JmxModel来创建JvmMXBeansImpl
	 * JvmMXBeans({@link JvmMXBeansImpl})
	 * @param jmx
	 * @return
	 */
	public static JvmMXBeans getJvmMXBeans(JmxModel jmx) {
        return new JvmMXBeansImpl(jmx.getMBeanServerConnection());
    }
	
	public static JvmMXBeans getJvmMXBeans(JmxModel jmx, int interval)
			throws IllegalArgumentException {
		if (interval < 0) {
			throw new IllegalArgumentException("interval cannot be negative"); // 
		}
		return new JvmMXBeansImpl(CachedMBeanServerConnectionFactory.getCachedMBeanServerConnection(jmx.getMBeanServerConnection(), interval));
	}

	/**
	 * JvmMXBeans的实现类
	 * @author jiangzhixiong
	 *
	 */
	static class JvmMXBeansImpl implements JvmMXBeans {

		protected MBeanServerConnection mbsc;
		private ClassLoadingMXBean classLoadingMXBean = null;
		private CompilationMXBean compilationMXBean = null;
		private LoggingMXBean loggingMXBean = null;
		private MemoryMXBean memoryMXBean = null;
		private OperatingSystemMXBean operatingSystemMXBean = null;
		private RuntimeMXBean runtimeMXBean = null;
		private ThreadMXBean threadMXBean = null;
		private List<GarbageCollectorMXBean> garbageCollectorMXBeans = null;
		private List<MemoryManagerMXBean> memoryManagerMXBeans = null;
		private List<MemoryPoolMXBean> memoryPoolMXBeans = null;
		private final static Logger LOGGER = Logger.getLogger(JvmMXBeansImpl.class.getName());


		public JvmMXBeansImpl(MBeanServerConnection mbsc) {
			this.mbsc = mbsc;
		}

		/**
		 * 获取类加载的MXBean
		 */
		public synchronized ClassLoadingMXBean getClassLoadingMXBean() {
			if (mbsc != null && classLoadingMXBean == null) {
				classLoadingMXBean = getMXBean(CLASS_LOADING_MXBEAN_NAME,ClassLoadingMXBean.class);
			}
			return classLoadingMXBean;
		}

		/**
		 * 获取编译系统的MXBean
		 */
		public synchronized CompilationMXBean getCompilationMXBean() {
			if (mbsc != null && compilationMXBean == null) {
				compilationMXBean = getMXBean(COMPILATION_MXBEAN_NAME,CompilationMXBean.class);
			}
			return compilationMXBean;
		}

		/**
		 * 获取日志MXBean
		 */
		public synchronized LoggingMXBean getLoggingMXBean() {
			if (mbsc != null && loggingMXBean == null) {
				loggingMXBean = getMXBean(LogManager.LOGGING_MXBEAN_NAME,LoggingMXBean.class);
			}
			return loggingMXBean;
		}


		/**
		 * 获取垃圾回收MXBean
		 */
		public synchronized Collection<GarbageCollectorMXBean> getGarbageCollectorMXBeans() {
			// TODO: How to deal with changes to the list?
			if (mbsc != null && garbageCollectorMXBeans == null) {
				ObjectName gcName;
				try {
					gcName = new ObjectName(
							GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*");
				} catch (MalformedObjectNameException e) {
					// Should never happen
					LOGGER.throwing(JvmMXBeansImpl.class.getName(),
							"getGarbageCollectorMXBeans", e);
					return null;
				}
				Set<ObjectName> mbeans;
				try {
					mbeans = mbsc.queryNames(gcName, null);
				} catch (Exception e) {
					LOGGER.throwing(JvmMXBeansImpl.class.getName(),
							"getGarbageCollectorMXBeans", e);
					return null;
				}
				if (mbeans != null) {
					garbageCollectorMXBeans = new ArrayList<GarbageCollectorMXBean>();
					for (ObjectName on : mbeans) {
						String name = GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE
								+ ",name=" + on.getKeyProperty("name");
						try {
							GarbageCollectorMXBean mbean = newPlatformMXBeanProxy(
									mbsc, name, GarbageCollectorMXBean.class);
							garbageCollectorMXBeans.add(mbean);
						} catch (Exception e) {
							LOGGER.throwing(JvmMXBeansImpl.class.getName(),
									"getGarbageCollectorMXBeans", e); 
						}
					}
				}
			}
			return garbageCollectorMXBeans;
		}

		/**
		 * 获取内存管理器MXBean
		 */
		public synchronized Collection<MemoryManagerMXBean> getMemoryManagerMXBeans() {
			// TODO: How to deal with changes to the list?
			if (mbsc != null && memoryManagerMXBeans == null) {
				ObjectName managerName;
				try {
					managerName = new ObjectName(
							MEMORY_MANAGER_MXBEAN_DOMAIN_TYPE + ",*");
				} catch (MalformedObjectNameException e) {
					// Should never happen
					LOGGER.throwing(JvmMXBeansImpl.class.getName(),
							"getMemoryManagerMXBeans", e); // 
					return null;
				}
				Set<ObjectName> mbeans;
				try {
					mbeans = mbsc.queryNames(managerName, null);
				} catch (Exception e) {
					LOGGER.throwing(JvmMXBeansImpl.class.getName(),
							"getMemoryManagerMXBeans", e); // 
					return null;
				}
				if (mbeans != null) {
					memoryManagerMXBeans = new ArrayList<MemoryManagerMXBean>();
					for (ObjectName on : mbeans) {
						String name = MEMORY_MANAGER_MXBEAN_DOMAIN_TYPE
								+ ",name=" + on.getKeyProperty("name"); // 
						try {
							MemoryManagerMXBean mbean = newPlatformMXBeanProxy(
									mbsc, name, MemoryManagerMXBean.class);
							memoryManagerMXBeans.add(mbean);
						} catch (Exception e) {
							LOGGER.throwing(JvmMXBeansImpl.class.getName(),
									"getMemoryManagerMXBeans", e); // 
						}
					}
				}
			}
			return memoryManagerMXBeans;
		}

		public synchronized MemoryMXBean getMemoryMXBean() {
			if (mbsc != null && memoryMXBean == null) {
				memoryMXBean = getMXBean(MEMORY_MXBEAN_NAME, MemoryMXBean.class);
			}
			return memoryMXBean;
		}


		public synchronized Collection<MemoryPoolMXBean> getMemoryPoolMXBeans() {
			// TODO: How to deal with changes to the list?
			if (mbsc != null && memoryPoolMXBeans == null) {
				ObjectName poolName;
				try {
					poolName = new ObjectName(MEMORY_POOL_MXBEAN_DOMAIN_TYPE
							+ ",*");
				} catch (MalformedObjectNameException e) {
					// Should never happen
					LOGGER.throwing(JvmMXBeansImpl.class.getName(),
							"getMemoryPoolMXBeans", e); // 
					return null;
				}
				Set<ObjectName> mbeans;
				try {
					mbeans = mbsc.queryNames(poolName, null);
				} catch (Exception e) {
					LOGGER.throwing(JvmMXBeansImpl.class.getName(),
							"getMemoryPoolMXBeans", e); // 
					return null;
				}
				if (mbeans != null) {
					memoryPoolMXBeans = new ArrayList<MemoryPoolMXBean>();
					for (ObjectName on : mbeans) {
						String name = MEMORY_POOL_MXBEAN_DOMAIN_TYPE + ",name="
								+ on.getKeyProperty("name"); // 
						try {
							MemoryPoolMXBean mbean = newPlatformMXBeanProxy(
									mbsc, name, MemoryPoolMXBean.class);
							memoryPoolMXBeans.add(mbean);
						} catch (Exception e) {
							LOGGER.throwing(JvmMXBeansImpl.class.getName(),
									"getMemoryPoolMXBeans", e); // 
						}
					}
				}
			}
			return memoryPoolMXBeans;
		}


		public synchronized OperatingSystemMXBean getOperatingSystemMXBean() {
			if (mbsc != null && operatingSystemMXBean == null) {
				operatingSystemMXBean = getMXBean(OPERATING_SYSTEM_MXBEAN_NAME,
						OperatingSystemMXBean.class);
			}
			return operatingSystemMXBean;
		}


		public synchronized RuntimeMXBean getRuntimeMXBean() {
			if (mbsc != null && runtimeMXBean == null) {
				runtimeMXBean = getMXBean(RUNTIME_MXBEAN_NAME,RuntimeMXBean.class);
			}
			return runtimeMXBean;
		}

		public synchronized ThreadMXBean getThreadMXBean() {
			if (mbsc != null && threadMXBean == null) {
				threadMXBean = getMXBean(THREAD_MXBEAN_NAME, ThreadMXBean.class);
			}
			return threadMXBean;
		}


		public <T> T getMXBean(ObjectName objectName, Class<T> interfaceClass) {
			return getMXBean(objectName.toString(), interfaceClass);
		}

		<T> T getMXBean(String objectNameStr, Class<T> interfaceClass) {
			if (mbsc != null) {
				try {
					return newPlatformMXBeanProxy(mbsc, objectNameStr,interfaceClass);
				} catch (IOException e) {
					LOGGER.throwing(JvmMXBeansImpl.class.getName(),"getMXBean", e);
				} catch (IllegalArgumentException iae) {
					LOGGER.log(Level.INFO, JvmMXBeansImpl.class.getName() + ".getMXBean()", iae);
				}
			}
			return null;
		}

		public void addMBeanCacheListener(MBeanCacheListener listener) {
			if (mbsc instanceof CachedMBeanServerConnection) {
				((CachedMBeanServerConnection) mbsc).addMBeanCacheListener(listener);
			} else {
				throw new UnsupportedOperationException(
						"The underlying MBeanServerConnection does not support caching."); // 
			}
		}

		public void removeMBeanCacheListener(MBeanCacheListener listener) {
			if (mbsc instanceof CachedMBeanServerConnection) {
				((CachedMBeanServerConnection) mbsc).removeMBeanCacheListener(listener);
			} else {
				throw new UnsupportedOperationException("The underlying MBeanServerConnection does not support caching.");
			}
		}

		public void flush() {
			if (mbsc instanceof CachedMBeanServerConnection) {
				((CachedMBeanServerConnection) mbsc).flush();
			} else {
				throw new UnsupportedOperationException("The underlying MBeanServerConnection does not support caching."); // 
			}
		}

		public int getInterval() {
			if (mbsc instanceof CachedMBeanServerConnection) {
				return ((CachedMBeanServerConnection) mbsc).getInterval();
			} else {
				throw new UnsupportedOperationException(
						"The underlying MBeanServerConnection does not support caching."); // 
			}
		}

	}

}
