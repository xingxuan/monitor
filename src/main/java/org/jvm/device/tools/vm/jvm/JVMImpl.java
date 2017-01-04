package org.jvm.device.tools.vm.jvm;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.jvm.device.tools.vm.application.Application;
import org.jvm.device.tools.vm.application.jvm.Jvm;
import org.jvm.device.tools.vm.application.jvm.MonitoredData;
import org.jvm.device.tools.vm.application.jvm.MonitoredDataListener;
import org.jvm.device.tools.vm.core.datasupport.Stateful;
import org.jvm.device.tools.vm.tools.jmx.JmxModel;
import org.jvm.device.tools.vm.tools.jmx.JmxModelFactory;
import org.jvm.device.tools.vm.tools.jmx.JvmMXBeans;
import org.jvm.device.tools.vm.tools.jvmstat.JvmstatListener;

public class JVMImpl extends Jvm implements JvmstatListener{
	private static final String HEAP_DUMP_ON_OOME = "HeapDumpOnOutOfMemoryError";
    private static final String HEAP_DUMP_PATH = "HeapDumpPath";
    Application application;
    //    Boolean isDumpOnOOMEnabled;
    //监控的数据监听器
    Set<MonitoredDataListener> listeners;
    
    JmxSupport jmxSupport;//com.jd.device.tools.vm.jvm.JmxSupport
    
    // static JVM data 
    private boolean staticDataInitialized; 
    private Object staticDataLock = new Object();
    private String commandLine;
    private String jvmArgs;
    private String jvmFlags;
    private String mainArgs;
    private String mainClass;
    private String vmVersion;
    private String javaVersion;
    private String javaHome;
    private String vmInfo;
    private String vmName;
    private String vmVendor;
    
    JVMImpl(Application app) {
        application = app;
        jmxSupport = new JmxSupport(app,this);
        listeners = new HashSet();
    }
    
    public void disableTimer(){
    	this.staticDataInitialized = false;
    	this.jmxSupport.disableTimer();
    }
    
    public void resetStaticDataInitialized(){
    	this.staticDataInitialized = false;
    }
    
    /**
     * 是否支持概要信息
     */
    public boolean isBasicInfoSupported() {
        return true;
    }
    
    public String getCommandLine() {
        initStaticData();
        return commandLine;
    }
    
    public String getJvmArgs() {
        initStaticData();
        return jvmArgs;
    }
    
    public String getJvmFlags() {
        initStaticData();
        return jvmFlags;
    }
    
    public String getMainArgs() {
        initStaticData();
        return mainArgs;
    }
    
    public String getMainClass() {
        initStaticData();
        return mainClass;
    }
    
    public String getVmVersion() {
        initStaticData();
        return vmVersion;
    }

    public String getJavaVersion() {
        initStaticData();
        if (javaVersion != null) {
            return javaVersion;
        }
        return vmVersion;
    }
    
    public String getJavaHome() {
        initStaticData();
        return javaHome;
    }
    
    public String getVmInfo() {
        initStaticData();
        return vmInfo;
    }
    
    public String getVmName() {
        initStaticData();
        return vmName;
    }
    
    public String getVmVendor() {
        initStaticData();
        return vmVendor;
    }
    
    public boolean is14() {
        String ver = getVmVersion();
        if (ver != null && ver.startsWith("1.4.")) {    // 
            return true;
        }
        return false;
    }
    
    public boolean is15() {
        String ver = getJavaVersion();
        if (ver != null && ver.startsWith("1.5.")) {    // 
            return true;
        }
        return false;
    }
    
    public boolean is16() {
        String ver = getJavaVersion();
        if (ver != null && (ver.startsWith("1.6.") || ver.startsWith("10.") || ver.startsWith("11."))) {    // 
            return true;
        }
        return false;
    }
    
    public boolean is17() {
        String ver = getJavaVersion();
        if (ver != null && (ver.startsWith("1.7.") || ver.startsWith("12.") || ver.startsWith("13.") || ver.startsWith("14."))) {  // 
            return true;
        }
        return false;
    }
    
    public boolean is18() {
        String ver = getJavaVersion();
        if (ver != null && ver.startsWith("1.8.")) {
            return true;
        }
        return false;
    }
    
    public boolean is19() {
        String ver = getJavaVersion();
        if (ver != null && ver.startsWith("1.9.")) {    // 
            return true;
        }
        return false;
    }
    
    protected JmxModel getJmxModel() {
        return JmxModelFactory.getJmxModelFor(application);
    }
    protected void initStaticData() {
        synchronized (staticDataLock) {
            if (staticDataInitialized) {
                return;
            }
            
            commandLine = "";
            jvmFlags = "";
            mainArgs = "";
            mainClass = "";
            
			jvmArgs = jmxSupport.getJvmArgs();
			Properties prop = getJmxModel().getSystemProperties();
			if (prop != null) {
				vmVersion = prop.getProperty("java.vm.version"); // 
				javaVersion = prop.getProperty("java.version"); // 
				javaHome = prop.getProperty("java.home"); // 
				vmInfo = prop.getProperty("java.vm.info"); // 
				vmName = prop.getProperty("java.vm.name"); // 
				vmVendor = prop.getProperty("java.vm.vendor"); // 
			}

			staticDataInitialized = true;
        }
    }

	@Override
	public boolean isAttachable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isMonitoringSupported() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isCompileMonitoringSupported() {
		return jmxSupport.getCompilationMXBean() != null;
	}

	@Override
	public boolean isClassMonitoringSupported() {
		return jmxSupport.getRuntime() != null;
	}
	

	@Override
	public boolean isThreadMonitoringSupported() {
		return jmxSupport.getRuntime() != null;
	}

	@Override
	public boolean isMemoryMonitoringSupported() {
		return jmxSupport.getRuntime() != null;
	}

	@Override
	public boolean isCpuMonitoringSupported() {
		return jmxSupport.hasProcessCPUTimeAttribute();
	}

	@Override
	public boolean isCollectionTimeSupported() {
		 Collection gcList = jmxSupport.getGarbageCollectorMXBeans();
	     return gcList != null && !gcList.isEmpty();
	}

	@Override
	public void removeMonitoredDataListener(MonitoredDataListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] getGenName() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 是否支持系统属性
	 */
	@Override
	public boolean isGetSystemPropertiesSupported() {
		return jmxSupport.getRuntime() != null;
	}

	@Override
	public Properties getSystemProperties() {
		Properties prop = null;
		JmxModel jmx = getJmxModel();
		if (jmx != null) {
			prop = jmx.getSystemProperties();
		}
		if (prop != null) {
			return prop;
		}
		return null;
	}

	@Override
	public boolean isDumpOnOOMEnabledSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDumpOnOOMEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setDumpOnOOMEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isTakeHeapDumpSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public File takeHeapDump() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isTakeThreadDumpSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public File takeThreadDump() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * 添加数据监听器,添加成功则在此启动定时器
	 */
	public void addMonitoredDataListener(MonitoredDataListener l) {
        synchronized (listeners) {
            if (listeners.add(l)) {
            	if (jmxSupport != null) jmxSupport.initTimer();
            }
        }
    }

	/**
	 * 获取监控的数据
	 */
	@Override
	public MonitoredData getMonitoredData() {
		if (application.getState() == Stateful.STATE_AVAILABLE) {
           if (jmxSupport != null) {
                JvmMXBeans jmx = jmxSupport.getJvmMXBeans();
                if (jmx != null) {
                    return new MonitoredDataImpl(jmxSupport,jmx);
                }
            }
        }
        return null;
	}
	
	/**
	 * 通知所有JVMImpl中所有的MonitoredDataListener
	 * 在定时器中，每间隔一秒去通知监听器
	 * @param data
	 */
	void notifyListeners(final MonitoredData data) {
        List<MonitoredDataListener> listenersCopy;
        synchronized (listeners) {
            listenersCopy = new ArrayList(listeners);
        }
        for (MonitoredDataListener listener : listenersCopy) {
            listener.monitoredDataEvent(data);
        }        
    }
	
	public void updateMBeanServerConnection(){
		this.jmxSupport.updateJvmMXBeans();
	}

}
