package org.jvm.device.tools.vm.jvm;

import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jvm.device.tools.vm.application.Application;
import org.jvm.device.tools.vm.application.jvm.MonitoredData;
import org.jvm.device.tools.vm.core.datasupport.DataRemovedListener;
import org.jvm.device.tools.vm.tools.jmx.JmxModel;
import org.jvm.device.tools.vm.tools.jmx.JmxModelFactory;
import org.jvm.device.tools.vm.tools.jmx.JvmMXBeans;
import org.jvm.device.tools.vm.tools.jmx.JvmMXBeansFactory;
import org.jvm.device.tools.vm.tools.jmx.JmxModel.ConnectionState;

public class JmxSupport implements DataRemovedListener {
	private final static Logger LOGGER = Logger.getLogger(JmxSupport.class.getName());
    private static final String PROCESS_CPU_TIME_ATTR = "ProcessCpuTime";
    private static final String PROCESSING_CAPACITY_ATTR = "ProcessingCapacity";
    private static final String PERM_GEN = "Perm Gen";
    private static final String PS_PERM_GEN = "PS Perm Gen";
    private static final String CMS_PERM_GEN = "CMS Perm Gen";
    private static final String G1_PERM_GEN = "G1 Perm Gen";
    private static final String METASPACE = "Metaspace";
    private static final String IBM_PERM_GEN = "class storage";
    private static final ObjectName osName = getOSName();
    private static long INITIAL_DELAY = 100;

    private Application application;
    private JvmMXBeans mxbeans;
    private JVMImpl jvm;
    private Object processCPUTimeAttributeLock = new Object();
    private Boolean processCPUTimeAttribute;
    private long processCPUTimeMultiplier;
    private Timer timer;
    private MemoryPoolMXBean permGenPool;
    private MemoryPoolMXBean edenSpacePool;
    private MemoryPoolMXBean survivorSpacePool;
    private MemoryPoolMXBean oldSpacePool;
    private Collection<GarbageCollectorMXBean> gcList;
    private String[] genName;
    
    
    JmxSupport(Application app, JVMImpl vm) {
        jvm = vm;
        application = app;
        //app.notifyWhenRemoved(this);
    }
    
    RuntimeMXBean getRuntime() {
        JvmMXBeans jmx = getJvmMXBeans();
        if (jmx != null) {
            return jmx.getRuntimeMXBean();
        }
        return null;
    }
    
    CompilationMXBean getCompilationMXBean() {
        JvmMXBeans jmx = getJvmMXBeans();
        if (jmx != null) {
            return jmx.getCompilationMXBean();
        }
        return null;
    }
    
    synchronized JvmMXBeans getJvmMXBeans() {
        if (mxbeans == null) {
            JmxModel jmxModel = JmxModelFactory.getJmxModelFor(application);
            if (jmxModel != null && jmxModel.getConnectionState() == ConnectionState.CONNECTED) {
                mxbeans = JvmMXBeansFactory.getJvmMXBeans(jmxModel);
            }
        }else {
        	JmxModel jmxModel = JmxModelFactory.getJmxModelFor(application);
            if (jmxModel != null && jmxModel.getConnectionState() == ConnectionState.DISCONNECTED) {
                mxbeans = null;
                this.jvm.resetStaticDataInitialized();
            }
        }
        return mxbeans;
    }
    
	synchronized JvmMXBeans updateJvmMXBeans() {
		permGenPool = null;
		edenSpacePool = null;
		survivorSpacePool = null;
		oldSpacePool = null;
		gcList = null;
		JmxModel jmxModel = JmxModelFactory.getJmxModelFor(application);
		if (jmxModel != null && jmxModel.getConnectionState() == ConnectionState.CONNECTED) {
			mxbeans = JvmMXBeansFactory.getJvmMXBeans(jmxModel);
		}
		return mxbeans;
	}
    
    /**
     * 获取所有垃圾收集器
     * @return
     */
    synchronized Collection<GarbageCollectorMXBean> getGarbageCollectorMXBeans() {
        if (gcList == null) {
            JvmMXBeans jmx = getJvmMXBeans();
            if (jmx != null) {
                gcList = jmx.getGarbageCollectorMXBeans();
            }           
        }
        return gcList;
    }
    /**
     * 获取分代内存池，当使用不同GC时，内存池的名称会有不同
     * @return
     */
    MemoryPoolMXBean getPermGenPool() {
        try {
            if (permGenPool == null) {//只有当permGenPool为空时，才去获取内存池
                JvmMXBeans jmx = getJvmMXBeans();
                if (jmx != null) {
                    Collection<MemoryPoolMXBean> pools = jmx.getMemoryPoolMXBeans();
                    for (MemoryPoolMXBean pool : pools) {
                        MemoryType type = pool.getType();
                        String name = pool.getName();
                        if (MemoryType.NON_HEAP.equals(type) && (PERM_GEN.equals(name) ||
                                PS_PERM_GEN.equals(name) ||
                                CMS_PERM_GEN.equals(name) ||
                                G1_PERM_GEN.equals(name) ||
                                METASPACE.equals(name) ||
                                IBM_PERM_GEN.equals(name))) {
                            permGenPool = pool;
                            //break;
                        }else if(name.contains("Eden")){
                        	this.edenSpacePool = pool;
                        }else if(name.contains("Survivor")){
                        	this.survivorSpacePool = pool;
                        }else if(name.contains("Old") || name.contains("Tenured")){
                        	this.oldSpacePool = pool;
                        }
                    }
                }
            }
            return permGenPool;
        } catch (Exception e) {
            LOGGER.throwing(JmxSupport.class.getName(), "getPermGenPool", e);
            return null;
        }
    }
    
    MemoryPoolMXBean getEdenSpacePool() {
		return edenSpacePool;
	}
    MemoryPoolMXBean getOldSpacePool() {
		return oldSpacePool;
	}
    MemoryPoolMXBean getSurvivorSpacePool() {
		return survivorSpacePool;
	}
    
    String getJvmArgs() {
        try {
            RuntimeMXBean runtime = getRuntime();
            if (runtime != null) {
                StringBuilder buf = new StringBuilder();
                List<String> args = runtime.getInputArguments();
                for (String arg : args) {
                    buf.append(arg).append(' ');
                }
                return buf.toString();
            }
            return null;
        } catch (Exception e) {
            LOGGER.throwing(JmxSupport.class.getName(), "getJvmArgs", e); // 
            return null;
        }
    }
    
    /**
     * CPU 处理时间
     * @return
     */
    long getProcessCPUTime() {
        if (!hasProcessCPUTimeAttribute()) {
            throw new UnsupportedOperationException();
        }
        JmxModel jmx = JmxModelFactory.getJmxModelFor(application);
        
        if (jmx != null && jmx.getConnectionState().equals(ConnectionState.CONNECTED)) {
           MBeanServerConnection conn = jmx.getMBeanServerConnection();
            
           if (conn != null) {
                try {
                    Long cputime = (Long)conn.getAttribute(osName,PROCESS_CPU_TIME_ATTR);
                    
                    return cputime.longValue()*processCPUTimeMultiplier;
                } catch (Exception ex) {
                    LOGGER.throwing(JmxSupport.class.getName(), "hasProcessCPUTimeAttribute", ex);
                }
            }
        }
        return -1;
    }
    
    boolean hasProcessCPUTimeAttribute() {
        synchronized (processCPUTimeAttributeLock) {
            if (processCPUTimeAttribute != null) {
                return processCPUTimeAttribute.booleanValue();
            }
            processCPUTimeAttribute = Boolean.FALSE;
            JmxModel jmx = JmxModelFactory.getJmxModelFor(application);
           
            if (jmx != null && jmx.getConnectionState().equals(ConnectionState.CONNECTED)) {
                MBeanServerConnection conn = jmx.getMBeanServerConnection();
                
                if (conn != null) {
                    try {
                       MBeanInfo info = conn.getMBeanInfo(osName);
                       MBeanAttributeInfo[] attrs = info.getAttributes();
                       
                       processCPUTimeMultiplier = 1;
                       for (MBeanAttributeInfo attr : attrs) {
                           String name = attr.getName();
                           if (PROCESS_CPU_TIME_ATTR.equals(name)) {
                               processCPUTimeAttribute = Boolean.TRUE;
                           }
                           if (PROCESSING_CAPACITY_ATTR.equals(name)) {
                               Number mul = (Number) conn.getAttribute(osName,PROCESSING_CAPACITY_ATTR);
                               processCPUTimeMultiplier = mul.longValue();
                           }
                        }
                    } catch (Exception ex) {
                       LOGGER.throwing(JmxSupport.class.getName(), "hasProcessCPUTimeAttribute", ex); //
                    }
                }
            }
            return processCPUTimeAttribute.booleanValue();
        }
    }

    private static ObjectName getOSName() {
        try {
            return new ObjectName(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
        } catch (MalformedObjectNameException ex) {
            throw new RuntimeException(ex);
        }
    }
	
	/**
	 * 定时器,定时的去获取JMX连接中JVM的数据信息
	 */
	void initTimer() {
        int interval = 1000;
        final JvmMXBeans jmx = getJvmMXBeans();
        if (jmx != null) {
            TimerTask task = new MonitorTimer();
            timer = new Timer("JMX MonitoredData timer for "+application.getId());
            timer.schedule(task,INITIAL_DELAY,interval);
        }
    }
	//------------------------
	
	class MonitorTimer extends TimerTask{
		private JvmMXBeans jmx = getJvmMXBeans();
		public void run() {
            try {
            	jmx = getJvmMXBeans();
            	if(jmx != null){
            		MonitoredData data = new MonitoredDataImpl(JmxSupport.this, jmx);
                    jvm.notifyListeners(data);
            	}
            } catch (UndeclaredThrowableException e) {
                LOGGER.throwing(JmxSupport.class.getName(), "MonitoredDataImpl<init>", e);
            }
        }
	}
	
	/**
	 * 关闭定时器
	 */
	void disableTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }
	
	public void dataRemoved(Object dataSource) {
        disableTimer();
    }

}
