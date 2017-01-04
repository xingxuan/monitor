package org.jvm.device.tools.vm.jmx.impl;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jvm.device.tools.vm.tools.jmx.JmxModel;
import org.jvm.device.tools.vm.tools.jmx.JvmMXBeans;
import org.jvm.device.tools.vm.tools.jmx.JvmMXBeansFactory;
import org.jvm.device.tools.vm.tools.jmx.JmxModel.ConnectionState;

/**
 * @author jiangzhixiong
 *
 */
public class JmxSupport {
	private final static Log LOGGER = LogFactory.getLog(JmxSupport.class);
	
	private final Object readOnlyConnectionLock = new Object();
	
	private JvmMXBeans mxbeans;
    private JmxModel jmxModel;
    
    private Boolean readOnlyConnection;
    
    private Boolean hasDumpAllThreads;
    private final Object hasDumpAllThreadsLock = new Object();
	
	JmxSupport(JmxModel jmx) {
        jmxModel = jmx;
    }
	
	private RuntimeMXBean getRuntime() {
        JvmMXBeans jmx = getJvmMXBeans();
        if (jmx != null) {
            return jmx.getRuntimeMXBean();
        }
        return null;
    }
	
	private synchronized JvmMXBeans getJvmMXBeans() {
        if (mxbeans == null) {
            if (jmxModel.getConnectionState() == ConnectionState.CONNECTED) {
                mxbeans = JvmMXBeansFactory.getJvmMXBeans(jmxModel);
            }
        }
        return mxbeans;
    }
	
	/**
	 * 连接断开后，将JvmMXBeans对象置为null
	 */
	public synchronized void disconnected(){
		mxbeans = null;
	}
	
	Properties getSystemProperties() {
        try {
            RuntimeMXBean runtime = getRuntime();
            if (runtime != null) {
                Properties prop = new Properties();
                prop.putAll(runtime.getSystemProperties());
                return prop;
            }
            return null;
        } catch (Exception e) {
            LOGGER.debug("getSystemProperties", e);
            return null;
        }
    }
	
	ThreadMXBean getThreadBean() {
        JvmMXBeans jmx = getJvmMXBeans();
        if (jmx != null) {
            return jmx.getThreadMXBean();
        }
        return null;
    }
	
	synchronized boolean isReadOnlyConnection() {
        synchronized (readOnlyConnectionLock) {
            if (readOnlyConnection == null) {
                readOnlyConnection = Boolean.FALSE;
                ThreadMXBean threads = getThreadBean();
                if (threads != null) {
                    try {
                        threads.getThreadInfo(1);
                    } catch (SecurityException ex) {
                        readOnlyConnection = Boolean.TRUE;
                    }
                }
            }
            return readOnlyConnection.booleanValue();
        }
    }
	
	private void printThreads(final StringBuilder sb, final ThreadMXBean threadMXBean, ThreadInfo[] threads) {
        boolean jdk16 = hasDumpAllThreads();
        
        for (ThreadInfo thread : threads) {
            if (thread != null) {
                if (jdk16) {
                    print16Thread(sb, threadMXBean, thread);
                } else {
                    print15Thread(sb, thread);
                }
            }
        }
    }
	
	private void print16Thread(final StringBuilder sb, final ThreadMXBean threadMXBean, final ThreadInfo thread) {
        MonitorInfo[] monitors = null;
        if (threadMXBean.isObjectMonitorUsageSupported()) {
            monitors = thread.getLockedMonitors();
        }
        sb.append("\n\"" + thread.getThreadName() +  "\" - Thread t@" + thread.getThreadId() + "\n");
        sb.append("   java.lang.Thread.State: " + thread.getThreadState());
        sb.append("\n");
        int index = 0;
        for (StackTraceElement st : thread.getStackTrace()) {
            LockInfo lock = thread.getLockInfo();
            String lockOwner = thread.getLockOwnerName();
            
            sb.append("\tat " + st.toString() + "\n");
            if (index == 0) {
                if ("java.lang.Object".equals(st.getClassName()) && "wait".equals(st.getMethodName())) {
                    if (lock != null) {
                        sb.append("\t- waiting on ");
                        printLock(sb,lock);
                        sb.append("\n");
                    }
                } else if (lock != null) {
                    if (lockOwner == null) {
                        sb.append("\t- parking to wait for ");
                        printLock(sb,lock);
                        sb.append("\n");
                    } else {
                        sb.append("\t- waiting to lock ");
                        printLock(sb,lock);
                        sb.append(" owned by \""+lockOwner+"\" t@"+thread.getLockOwnerId()+"\n");
                    }
                }
            }
            printMonitors(sb, monitors, index);
            index++;
        }
        StringBuilder jnisb = new StringBuilder();
        printMonitors(jnisb, monitors, -1);
        if (jnisb.length() > 0) {
            sb.append("   JNI locked monitors:\n");
            sb.append(jnisb);
        }
        if (threadMXBean.isSynchronizerUsageSupported()) {
            sb.append("\n   Locked ownable synchronizers:");
            LockInfo[] synchronizers = thread.getLockedSynchronizers();
            if (synchronizers == null || synchronizers.length == 0) {
                sb.append("\n\t- None\n");
            } else {
                for (LockInfo li : synchronizers) {
                    sb.append("\n\t- locked ");
                    printLock(sb,li);
                    sb.append("\n");
                }
            }
        }
    }
	
	private void printMonitors(final StringBuilder sb, final MonitorInfo[] monitors, final int index) {
        if (monitors != null) {
            for (MonitorInfo mi : monitors) {
                if (mi.getLockedStackDepth() == index) {
                    sb.append("\t- locked ");
                    printLock(sb,mi);
                    sb.append("\n");
                }
            }
        }
    }
	
	private void print15Thread(final StringBuilder sb, final ThreadInfo thread) {
        sb.append("\n\"" + thread.getThreadName() +  "\" - Thread t@" + thread.getThreadId() + "\n");
        sb.append("   java.lang.Thread.State: " + thread.getThreadState());
        if (thread.getLockName() != null) {
            sb.append(" on " + thread.getLockName());
            if (thread.getLockOwnerName() != null) {
                sb.append(" owned by: " + thread.getLockOwnerName());
            }
        }
        sb.append("\n");
        for (StackTraceElement st : thread.getStackTrace()) {
            sb.append("        at " + st.toString() + "\n");
        }
    }
	
	private void printLock(StringBuilder sb,LockInfo lock) {
        String id = Integer.toHexString(lock.getIdentityHashCode());
        String className = lock.getClassName();
        
        sb.append("<"+id+"> (a "+className+")");
    }
	
	private boolean hasDumpAllThreads() {
        synchronized (hasDumpAllThreadsLock) {
            if (hasDumpAllThreads == null) {
                hasDumpAllThreads = Boolean.FALSE;
                try {
                    ObjectName threadObjName = new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME);
                    MBeanInfo threadInfo = jmxModel.getMBeanServerConnection().getMBeanInfo(threadObjName);
                    if (threadInfo != null) {
                        for (MBeanOperationInfo op : threadInfo.getOperations()) {
                            if ("dumpAllThreads".equals(op.getName())) {
                                hasDumpAllThreads = Boolean.TRUE;
                            }
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.info("hasDumpAllThreads", ex);
                }
            }
            return hasDumpAllThreads.booleanValue();
        }
    }
	
	String takeThreadDump() {
        try {
            ThreadMXBean threadMXBean = getThreadBean();
            if (threadMXBean == null) {
                return null;
            }
            ThreadInfo[] threads;
            Properties prop = getSystemProperties();
            StringBuilder sb = new StringBuilder(4096);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sb.append(df.format(new Date()) + "\n");
            sb.append("Full thread dump " + prop.getProperty("java.vm.name") + " (" + prop.getProperty("java.vm.version") + " " + prop.getProperty("java.vm.info") + "):\n");
            if (hasDumpAllThreads()) {
                threads = threadMXBean.dumpAllThreads(true, true);
            } else {
                long[] threadIds = threadMXBean.getAllThreadIds();
                threads = threadMXBean.getThreadInfo(threadIds, Integer.MAX_VALUE);
            }
            printThreads(sb, threadMXBean, threads);
            return sb.toString();
        } catch (Exception e) {
            LOGGER.info("takeThreadDump", e);
            return null;
        }
    }

}
