package org.jvm.device.tools.vm.application.views.monitor;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jvm.device.tools.vm.application.Application;
import org.jvm.device.tools.vm.application.jvm.Jvm;
import org.jvm.device.tools.vm.application.jvm.JvmFactory;
import org.jvm.device.tools.vm.application.jvm.MonitoredData;
import org.jvm.device.tools.vm.application.jvm.MonitoredDataListener;
import org.jvm.device.tools.vm.application.views.thread.ThreadDataManager;
import org.jvm.device.tools.vm.core.datasource.DataSource;
import org.jvm.device.tools.vm.jmx.impl.JmxApplication;
import org.jvm.device.tools.vm.tools.jmx.JmxModel;
import org.jvm.device.tools.vm.tools.jmx.JmxModelFactory;
import org.jvm.device.tools.vm.tools.jmx.JvmMXBeans;
import org.jvm.device.tools.vm.tools.jmx.JvmMXBeansFactory;
import org.jvm.device.tools.vm.tools.jmx.JmxModel.ConnectionState;

import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.*;

public final class ApplicationMonitorModel {
	
	private boolean initialized;
    private final DataSource source;
	private final boolean live;
	
	private final List<ChangeListener> listeners;
	
	private int processorsCount = -1;//CPU核数
	private boolean cpuMonitoringSupported = false;
	private boolean gcMonitoringSupported = false;
	private boolean classMonitoringSupported = false;
	private boolean compileMonitoringSupported = false;
    private boolean memoryMonitoringSupported = false;
    private boolean threadsMonitoringSupported = false;
	private long timestamp = -1;

    private long uptime = -1;//JVM运行时间
    private long prevUpTime = -1;//JVM上次运行时间
    private long processCpuTime = -1;//
    private long processGcTime = -1;
    private long prevProcessCpuTime = -1;
    private long prevProcessGcTime = -1;
    
    private long totalCompilationTime = -1;
    
    private long sharedUnloaded = -1;
    private long totalUnloaded = -1;
    private long sharedLoaded = -1;
    private long totalLoaded = -1;
    private long collectionCount = 0;
    
    private long yongGcTime = 0;
    private long yongGcCount = 0;
    private long oldGcTime = 0;
    private long oldGcCount = 0;
    private String yongGcName;
    private String oldGcName;
    
    private long[] edenSpaceUsage = {0,0,0,0};
    private long[] survivorSpaceUsage = {0,0,0,0};
    private long[] oldGenSpaceUsage = {0,0,0,0};
    private long[] permGenSpaceUsage = {0,0,0,0};
    private long heapCapacity = -1;
    private long heapUsed = -1;
    private long maxHeap = -1;
    private long permgenCapacity = -1;
    private long permgenUsed = -1;
    private long permgenMax = -1;
    //线程信息
    private long totalThreads = -1;
    private long daemonThreads = -1;
    private long peakThreads = -1;
    private long startedThreads = -1;
    
    private String pid;
    private String hostName;
    private String mainClass;
    private String mainArgs;
    private String vmId;
    private String javaHome;
    private String javaVersion;
    private String javaVendor;
    private String jvmFlags;
    private String jvmArgs;
    private String systemProperties;
    
    private Jvm jvm;
    //    private MemoryMXBean memoryMXBean;
    private ThreadMXBean threadBean;
    private MonitoredDataListener monitoredDataListener;
    
    Map<Long, String[]> threadIdsMap = new HashMap<Long, String[]>();
    
    /**
     * 初始化应用程序
     */
    public synchronized void initialize() {
        if (initialized) return;
        initialized = true;
        if (source instanceof Application) initialize((Application)source);
    }

    /**
     * 创建一个ApplicationMonitorModel应用监听模型
     * @param application
     * @param live
     * @return
     */
	public static ApplicationMonitorModel create(Application application,boolean live) {
		return new ApplicationMonitorModel(application, live);
	}
	public boolean isLive() {
		return live;
	}

	public int getProcessorsCount() {
		return processorsCount;
	}

	public boolean isCpuMonitoringSupported() {
		return cpuMonitoringSupported;
	}

	public boolean isGcMonitoringSupported() {
		return gcMonitoringSupported;
	}
	public long    getTimestamp() { return timestamp; }
	public long    getUpTime() { return uptime; }
    public long    getPrevUpTime() { return prevUpTime; }
    public long getProcessCpuTime() { return processCpuTime; }
    public long getProcessGcTime() { return processGcTime; }
    public long getPrevProcessCpuTime() { return prevProcessCpuTime; }
    public long getPrevProcessGcTime() { return prevProcessGcTime; }
    public long getTotalCompilationTime() {
		return totalCompilationTime;
	}
    public boolean isMemoryMonitoringSupported() { return memoryMonitoringSupported; }
    public boolean isThreadsMonitoringSupported() { return threadsMonitoringSupported; }
    public long getSharedUnloaded() { return sharedUnloaded; }
    public long getTotalUnloaded() { return totalUnloaded; }
    public long getSharedLoaded() { return sharedLoaded; }
    public long getTotalLoaded() { return totalLoaded; }
    public long getCollectionCount() {
		return collectionCount;
	}
    public long getYongGcCount() {
		return yongGcCount;
	}
    public long getYongGcTime() {
		return yongGcTime;
	}
    public long getOldGcCount() {
		return oldGcCount;
	}
    public long getOldGcTime() {
		return oldGcTime;
	}
    public String getYongGcName() {
		return yongGcName;
	}
    public String getOldGcName() {
		return oldGcName;
	}
    public long[] getEdenSpaceUsage() {
		return edenSpaceUsage;
	}
    public long[] getSurvivorSpaceUsage() {
		return survivorSpaceUsage;
	}
    public long[] getOldGenSpaceUsage() {
		return oldGenSpaceUsage;
	}
    public long[] getPermGenSpaceUsage() {
		return permGenSpaceUsage;
	}
    
    //--- JvmOverview JVM的概要信息
	public String getPid() { return pid; }
	public String getHostName() { return hostName; }
	public String getMainClass() { return mainClass; }
	public String getMainArgs() { return mainArgs; }
	public String getVmId() { return vmId; }
	public String getJavaHome() { return javaHome; }
	public String getJavaVersion() { return javaVersion; }
	public String getJavaVendor() { return javaVendor; }
	public String getJvmFlags() { return jvmFlags; }
	public String getJvmArgs() { return jvmArgs; }
	public String getSystemProperties() { return systemProperties; }
	//堆内存信息
	public long getHeapCapacity() { return heapCapacity; }
    public long getHeapUsed() { return heapUsed; }
    public long getMaxHeap() { return maxHeap; }
    public long getPermgenCapacity() { return permgenCapacity; }
    public long getPermgenUsed() { return permgenUsed; }
    public long getPermgenMax() { return permgenMax; }
    
    public long getTotalThreads() { return totalThreads; }
    public long getDeamonThreads() { return daemonThreads; }
    public long getPeakThreads() { return peakThreads; }
    public long getStartedThreads() { return startedThreads; }
	
	private ApplicationMonitorModel(DataSource source,boolean live) {
		initialized = false;
        
        this.source = source;
		this.live = live;
		
		listeners = Collections.synchronizedList(new ArrayList());
	}

    private static boolean isValidXMLChar(char ch) {
        return (ch == 0x9 || ch == 0xA || ch == 0xD ||
                ((ch >= 0x20) && (ch <= 0xD7FF)) ||
                ((ch >= 0xE000) && (ch <= 0xFFFD)) /*||
              ((ch >= 0x10000) && (ch <= 0x10FFFF))*/);
    }

	private static String expandInvalidXMLChars(CharSequence chars) {
        StringBuilder text = new StringBuilder(chars.length());
        char ch;

        for (int i = 0; i < chars.length(); i++) {
            ch = chars.charAt(i);
            text.append(isValidXMLChar(ch) ? ch :
                    "&lt;0x" + Integer.toHexString(0x10000 | ch).substring(1).toUpperCase() + "&gt;");
        }

        return text.toString();
    }
	
	public void removeChangeListener(ChangeListener listener) {
        if (live) listeners.remove(listener);
    }
	
	private void fireChange() {
        final List<ChangeListener> list = new ArrayList();
        synchronized (listeners) {
        	list.addAll(listeners);
        }
        for (ChangeListener l : list){
        	l.stateChanged(new ChangeEvent(this));
        }
    }
	
	//格式化字符串
	private static String formatJVMArgs(String jvmargs) {
        String mangledString = " ".concat(jvmargs).replace(" -", "\n");
        StringTokenizer tok = new StringTokenizer(mangledString, "\n");
        StringBuffer text = new StringBuffer(100);
        while (tok.hasMoreTokens()) {
            String arg = tok.nextToken().replace(" ", "&nbsp;");   
            int equalsSign = arg.indexOf('=');

            text.append("<b>");
            text.append("-");  
            if (equalsSign != -1) {
                text.append(arg.substring(0, equalsSign));
                text.append("</b>");   
                text.append(arg.substring(equalsSign));
            } else {
                text.append(arg);
                text.append("</b>");   
            }
            text.append("<br>");   
        }
        return text.toString();
    }
	
	private static String formatSystemProperties(Properties properties) {
        StringBuilder text = new StringBuilder(200);
        List keys = new ArrayList();
        Enumeration en = properties.propertyNames();
        Iterator keyIt;
        
        while (en.hasMoreElements()) {
            keys.add(en.nextElement());
        }

        Collections.sort(keys);
        keyIt = keys.iterator();
        while (keyIt.hasNext()) {
            String key = (String) keyIt.next();
            String val = properties.getProperty(key);
            
            if ("line.separator".equals(key) && val != null) {
                val = val.replace("\n", "\\n");
                val = val.replace("\r", "\\r");
            }

            text.append("<b>");
            text.append(key);
            text.append("</b>=");
            text.append(val);
            text.append("<br>");
        }
        return expandInvalidXMLChars(text);
    }
	
    /**
	 * 添加数据变化监控器
	 * @param listener
	 */
	public void addChangeListener(ChangeListener listener) {
        if (live) listeners.add(listener);
    }

    /**
     * 从MonitoredData中获取监控数据
     *
     * @param time
     * @param data
     */
    private void updateValues(final long time, final MonitoredData data) {
        timestamp = time;
        if (data != null) {
            prevUpTime = uptime;
            uptime = data.getUpTime();

            if (cpuMonitoringSupported) {
                prevProcessCpuTime = processCpuTime;
                processCpuTime = data.getProcessCpuTime();
            }

            if (gcMonitoringSupported) {
                prevProcessGcTime = processGcTime;
                processGcTime = data.getCollectionTime();
                collectionCount = data.getCollectionCount();

                yongGcCount = data.getYongGcCount();
                yongGcTime = data.getYongGcTime();
                oldGcCount = data.getOldGcCount();
                oldGcTime = data.getOldGcTime();
                yongGcName = data.getYongGcName();
                oldGcName = data.getOldGcName();

            }

            if (compileMonitoringSupported) {
                totalCompilationTime = data.getTotalCompilationTime();
            }

            if (classMonitoringSupported) {
                sharedUnloaded = data.getSharedUnloadedClasses();
                totalUnloaded = data.getUnloadedClasses();
                sharedLoaded = data.getSharedLoadedClasses();
                totalLoaded = data.getLoadedClasses();
            }

            if (memoryMonitoringSupported) {
                heapCapacity = data.getGenCapacity()[0];
                heapUsed = data.getGenUsed()[0];
                maxHeap = data.getGenMaxCapacity()[0];
                permgenCapacity = data.getGenCapacity()[1];
                permgenUsed = data.getGenUsed()[1];
                permgenMax = data.getGenMaxCapacity()[1];

                this.edenSpaceUsage = data.getEdenSpaceUsage();
                this.survivorSpaceUsage = data.getSurvivorSpaceUsage();
                this.oldGenSpaceUsage = data.getOldGenSpaceUsage();
                this.permGenSpaceUsage = data.getPermGenSpaceUsage();
            }

            if (threadsMonitoringSupported) {
                totalThreads = data.getThreadsLive();
                daemonThreads = data.getThreadsDaemon();
                peakThreads = data.getThreadsLivePeak();
                startedThreads = data.getThreadsStarted();
            }


        }
    }

    /**
     * 初始化操作
     *
     * @param application
     */
    private void initialize(final Application application) {

        processorsCount = 1;

        jvm = JvmFactory.getJVMFor(application);

        if (jvm != null) {//获取JMX中是否支持相关的监控
            cpuMonitoringSupported = jvm.isCpuMonitoringSupported();
            gcMonitoringSupported = jvm.isCollectionTimeSupported();
            memoryMonitoringSupported = jvm.isMemoryMonitoringSupported();
            compileMonitoringSupported = jvm.isCompileMonitoringSupported();
            classMonitoringSupported = jvm.isClassMonitoringSupported();
            threadsMonitoringSupported = jvm.isThreadMonitoringSupported();
            if (jvm.isBasicInfoSupported()) {
                int pidInt = application.getPid();
                pid = pidInt == Application.UNKNOWN_PID ? "unknow" : "" + pidInt;
                hostName = application.getHost().getHostName();
                mainClass = jvm.getMainClass();
                if (mainClass == null || "".equals(mainClass)) mainClass = "unknow";
                mainArgs = jvm.getMainArgs();
                if (mainArgs == null) mainArgs = "none";
                vmId = jvm.getVmName() + " (" + jvm.getVmVersion() + ", " + jvm.getVmInfo() + ")";
                javaHome = jvm.getJavaHome();
                javaVersion = jvm.getJavaVersion();
                javaVendor = jvm.getVmVendor();
                jvmFlags = jvm.getJvmFlags();
                if (jvmFlags == null || jvmFlags.length() == 0) jvmFlags = "none";
                String jvmArgss = jvm.getJvmArgs();
                if (jvmArgss != null) jvmArgs = formatJVMArgs(jvmArgss);

            }

            if (jvm.isGetSystemPropertiesSupported()) {
                Properties jvmProperties = jvm.getSystemProperties();
                if (jvmProperties != null) systemProperties = formatSystemProperties(jvmProperties);
            }
        }

//		memoryMXBean = null;
        threadBean = null;
        JmxModel jmxModel = JmxModelFactory.getJmxModelFor(application);
        if (jmxModel != null && jmxModel.getConnectionState() == ConnectionState.CONNECTED) {
            JvmMXBeans mxbeans = JvmMXBeansFactory.getJvmMXBeans(jmxModel);
            if (mxbeans != null) {
//                memoryMXBean = mxbeans.getMemoryMXBean();
                threadBean = mxbeans.getThreadMXBean();
                OperatingSystemMXBean osbean = mxbeans.getOperatingSystemMXBean();
                if (osbean != null) processorsCount = osbean.getAvailableProcessors();
            }
        }

        if (jvm != null) {
            //获取监控数值
            updateValues(System.currentTimeMillis(), jvm.getMonitoredData());
            //线程监控的相关数据
            String connstr = ((JmxApplication) application).getConnstr();
            final ThreadDataManager tdm = new ThreadDataManager(threadBean, connstr);
            tdm.fillThreadData(threadIdsMap);

            if (live) {
                //MonitoredDataListener的实现,{JVMImpl#notifyListeners} 会定时来执行此monitoredDataEvent方法
                monitoredDataListener = new MonitoredDataListener() {
                    long lastTimestamp = -1;

                    public void monitoredDataEvent(final MonitoredData data) {
                        long timestamp = System.currentTimeMillis();
                        threadBean = data.getThreadBean();
                        final long timestampF = lastTimestamp < timestamp ? lastTimestamp = timestamp : ++lastTimestamp;
                        updateValues(timestampF, data);
                        cpuMonitoringSupported = jvm.isCpuMonitoringSupported();
                        gcMonitoringSupported = jvm.isCollectionTimeSupported();
                        memoryMonitoringSupported = jvm.isMemoryMonitoringSupported();
                        compileMonitoringSupported = jvm.isCompileMonitoringSupported();
                        classMonitoringSupported = jvm.isClassMonitoringSupported();
                        threadsMonitoringSupported = jvm.isThreadMonitoringSupported();
                        if (jvm.isBasicInfoSupported()) {
                            int pidInt = application.getPid();
                            pid = pidInt == Application.UNKNOWN_PID ? "unknow" : "" + pidInt;
                            hostName = application.getHost().getHostName();
                            mainClass = jvm.getMainClass();
                            if (mainClass == null || "".equals(mainClass)) mainClass = "unknow";
                            mainArgs = jvm.getMainArgs();
                            if (mainArgs == null) mainArgs = "none";
                            vmId = jvm.getVmName() + " (" + jvm.getVmVersion() + ", " + jvm.getVmInfo() + ")";
                            javaHome = jvm.getJavaHome();
                            javaVersion = jvm.getJavaVersion();
                            javaVendor = jvm.getVmVendor();
                            jvmFlags = jvm.getJvmFlags();
                            if (jvmFlags == null || jvmFlags.length() == 0) jvmFlags = "none";
                            String jvmArgss = jvm.getJvmArgs();
                            if (jvmArgss != null) jvmArgs = formatJVMArgs(jvmArgss);

                        }

                        if (jvm.isGetSystemPropertiesSupported()) {
                            Properties jvmProperties = jvm.getSystemProperties();
                            if (jvmProperties != null) systemProperties = formatSystemProperties(jvmProperties);
                        }

                        JmxModel jmxModel = JmxModelFactory.getJmxModelFor(application);
                        if (jmxModel != null && jmxModel.getConnectionState() == ConnectionState.CONNECTED) {
                            JvmMXBeans mxbeans = JvmMXBeansFactory.getJvmMXBeans(jmxModel);
                            if (mxbeans != null) {
//			                    memoryMXBean = mxbeans.getMemoryMXBean();
                                threadBean = mxbeans.getThreadMXBean();
                                OperatingSystemMXBean osbean = mxbeans.getOperatingSystemMXBean();
                                if (osbean != null) processorsCount = osbean.getAvailableProcessors();
                            }
                        }
                        tdm.setThreadBean(threadBean);
                        try {
                            tdm.fillThreadData(threadIdsMap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        fireChange();// 去通知ChangeListener
                    }
                };
                // {@link JVMImpl#addMonitoredDataListener} 添加成功会启动一个定时器
                jvm.addMonitoredDataListener(monitoredDataListener);
            }


        }
    }

}
