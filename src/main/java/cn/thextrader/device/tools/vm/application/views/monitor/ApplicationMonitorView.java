package cn.thextrader.device.tools.vm.application.views.monitor;

import org.springframework.util.StringUtils;

import cn.thextrader.device.json.JSONUtil;
import cn.thextrader.device.json.OverviewJson;
import cn.thextrader.device.json.VisualGCJson;
import cn.thextrader.device.tools.vm.cache.CacheOverview;
import cn.thextrader.device.tools.vm.cache.CacheVisualGC;
import cn.thextrader.device.tools.vm.tools.jmx.SimpleXYChartUtils;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 应用程序监控显示端
 * @author jiangzhixiong
 *
 */
public class ApplicationMonitorView {
    private static final Logger LOGGER = Logger.getLogger(ApplicationMonitorView.class.getName());
    private static final String UNKNOWN = "unknown";
    private final ApplicationMonitorModel model;

    public ApplicationMonitorView(ApplicationMonitorModel model) {
        this.model = model;
    }

    public void init() {
        model.initialize();
    }

    /**
     * 显示监控的数据
     */
    public void monitorView(final String connectionString) {

        final CpuViewSupport cpuViewSupport = new CpuViewSupport(model, connectionString);
        final VisualGCViewSupport visualGCViewSupport = new VisualGCViewSupport(model, connectionString);
        final JvmOverview jvmOverview = new JvmOverview(model, connectionString);

        final Runnable refresher = new Runnable() {
            public void run() {
                String[] details = cpuViewSupport.refresh(model);
                visualGCViewSupport.refresh(model,details);
                jvmOverview.refresh(model);
            }
        };
        refresher.run();

        //注册监听器,一旦注册成功,MonitoredDataListener实现中的fireChange()会定时通知这个监听器
        model.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
//            	long t = System.currentTimeMillis();
                refresher.run();
//                t = System.currentTimeMillis() - t;
            }
        });


    }

    //---------CPU
    /**
     * cpu监控数据的支持
     * 
     */
    private static class CpuViewSupport {
        private boolean liveModel;
        private int processorsCount;
        private boolean cpuMonitoringSupported;
        private boolean gcMonitoringSupported;

        public CpuViewSupport(ApplicationMonitorModel model, final String connectionString) {
            initModels(model);
        }

        /**
         * 更新CPU的最新使用率信息
         *
         * @param model
         */
        private String[] refresh(ApplicationMonitorModel model) {
        	String[] details = {"",""};
            long cpuUsage = -1;
            long gcUsage = -1;
            String cpuDetail = UNKNOWN;
            String gcDetail = UNKNOWN;

            long upTime = model.getUpTime() * 1000000;
            long prevUpTime = model.getPrevUpTime() * 1000000;

            boolean tracksProcessCpuTime = cpuMonitoringSupported && model.getPrevProcessCpuTime() != -1;
            long processCpuTime = tracksProcessCpuTime ? model.getProcessCpuTime() / processorsCount : -1;
            long prevProcessCpuTime = tracksProcessCpuTime ? model.getPrevProcessCpuTime() / processorsCount : -1;

            boolean tracksProcessGcTime = gcMonitoringSupported && model.getPrevProcessGcTime() != -1;
            long processGcTime = tracksProcessGcTime ? model.getProcessGcTime() * 1000000 / processorsCount : -1;
            long prevProcessGcTime = tracksProcessGcTime ? model.getPrevProcessGcTime() * 1000000 / processorsCount : -1;

            if (prevUpTime != -1 && (tracksProcessCpuTime || tracksProcessGcTime)) {
            	long upTimeDiff = upTime - prevUpTime;
                
                if (tracksProcessCpuTime) {
                	long processTimeDiff = processCpuTime - prevProcessCpuTime;
                	cpuUsage = upTimeDiff > 0 ? Math.min((long) (1000 * (float) processTimeDiff / (float) upTimeDiff), 1000) : 0;
                    //float cpuUse = Math.min((1000 * (float) processTimeDiff / (float) upTimeDiff), 1000);
                	cpuDetail = cpuUsage == -1 ? UNKNOWN : SimpleXYChartUtils.formatPercent(cpuUsage*0.1d);
                    //cpuDetail = cpuUsage == -1 ? UNKNOWN : SimpleXYChartUtils.formatDouble(cpuUse,0.1d);
                	cpuDetail = cpuDetail.replace("%", "");
                }
                if (tracksProcessGcTime) {
                	long processGcTimeDiff = processGcTime - prevProcessGcTime;
                    gcUsage = upTimeDiff > 0 ? Math.min((long) (1000 * (float) processGcTimeDiff / (float) upTimeDiff), 1000) : 0;
                    if (cpuUsage != -1 && cpuUsage < gcUsage) gcUsage = cpuUsage;
                    //float gcUse = Math.min((1000 * (float) processGcTimeDiff / (float) upTimeDiff), 1000);
                    gcDetail = gcUsage == -1 ? UNKNOWN : SimpleXYChartUtils.formatPercent(gcUsage*0.1d);
                    //gcDetail = gcUsage == -1 ? UNKNOWN : SimpleXYChartUtils.formatDouble(gcUse,0.1d);
                    gcDetail = gcDetail.replace("%", "");
                }
                
                if (liveModel) {
                    details[0] = cpuDetail;
                    details[1] = gcDetail;
                }
            }
            
            return details;
            
        }

        private void initModels(ApplicationMonitorModel model) {
            liveModel = model.isLive();
            processorsCount = model.getProcessorsCount();
            cpuMonitoringSupported = model.isCpuMonitoringSupported();
            gcMonitoringSupported = model.isGcMonitoringSupported();
        }
    }
    //---------CPU end

    public static String getTime(long millis) {
        // Hours
        long hours = millis / 3600000;
        String sHours = (hours == 0 ? "" : "" + hours);
        millis = millis % 3600000;

        // Minutes
        long minutes = millis / 60000;
        String sMinutes = (((hours > 0) && (minutes < 10)) ? "0" + minutes : "" + minutes);
        millis = millis % 60000;

        // Seconds
        long seconds = millis / 1000;
        String sSeconds = ((seconds < 10) ? "0" + seconds : "" + seconds);

        if (sHours.length() == 0) {
            return sMinutes + ":" + sSeconds;
        } else {
            return sHours + ":" + sMinutes + ":" + sSeconds;
        }
    }

    //------------VisualGCViewSupport start
    /**
     * GC相关监控数据的支持
     */
    private static class VisualGCViewSupport {
        private String connectionString;
        private boolean liveModel;
        private Double oldUsed;
        private Double permUsed;
        private Double survivorUsed;
        private Double ygct = 0d;
        private Double ogct = 0d;
        private Long totalThreads;
        private int cpuUse = -1;
        private int clazzUse = 0;
        private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        public VisualGCViewSupport(final ApplicationMonitorModel model, final String connectionString) {
            initModels(model);
            this.connectionString = connectionString;

            /**
             * 监听老年代内存变化
             * @author jiangzhixiong
             *
             */
            class OldGenSpaceListener implements PropertyChangeListener {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String jsonStr = "[" + model.getTimestamp() + "," + evt.getNewValue() + "],";
                    CacheVisualGC.saveOldGenSpace(connectionString, jsonStr);
                }
            }
            /**
             * perm gen内存变化监听器
             * @author jiangzhixiong
             *
             */
            class PermGenSpaceListener implements PropertyChangeListener {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String jsonStr = "[" + model.getTimestamp() + "," + evt.getNewValue() + "],";
                    CacheVisualGC.savePermGenSpace(connectionString, jsonStr);
                }
            }
            /**
             * survivor内存变化监听器
             * @author jiangzhixiong
             *
             */
            class SurvivorSpaceListener implements PropertyChangeListener {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String jsonStr = "[" + model.getTimestamp() + "," + evt.getNewValue() + "],";
                    CacheVisualGC.saveSurvivorSpace(connectionString, jsonStr);
                }
            }
            /**
             * 年轻代gc time监听器
             * @author jiangzhixiong
             *
             */
            class YongGcTimeListener implements PropertyChangeListener {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                	Double o = (Double)evt.getOldValue();
                	Double n = (Double)evt.getNewValue();
                	if(o == null || n == null) return;
                    Double v = n - o;
                    String jsonStr = "[" + model.getTimestamp() + "," + v.intValue() + "],";
                    CacheVisualGC.saveYongGcTime(connectionString, jsonStr);
                }
            }
            /**
             * 年老代gc time监听器
             * @author jiangzhixiong
             *
             */
            class OldGcTimeListener implements PropertyChangeListener {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                	Double o = (Double)evt.getOldValue();
                	Double n = (Double)evt.getNewValue();
                	if(o == null || n == null) return;
                    Double v = n - o;
                    String jsonStr = "[" + model.getTimestamp() + "," + v.intValue() + "],";
                    CacheVisualGC.saveOldGcTime(connectionString, jsonStr);
                }
            }
            /**
             * 活动线程变化监听器
             * @author jiangzhixiong
             *
             */
            class TotalThreadListener implements PropertyChangeListener {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String jsonStr = "[" + model.getTimestamp() + "," + evt.getNewValue() + "],";
                     CacheVisualGC.saveTotalThread(connectionString, jsonStr);
                }
            }
            /**
             * cpu使用变化监听器
             * @author jiangzhixiong
             *
             */
            class CpuUseListener implements PropertyChangeListener {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String jsonStr = "[" + model.getTimestamp() + "," + evt.getNewValue() + "],";
                     CacheVisualGC.saveCpuUse(connectionString, jsonStr);
                }
            }
            class ClazzListener implements PropertyChangeListener {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String jsonStr = "[" + model.getTimestamp() + "," + evt.getNewValue() + "],";
                    CacheVisualGC.saveClazzUse(connectionString, jsonStr);
                }
            }
            propertyChangeSupport.addPropertyChangeListener("oldUseProp", new OldGenSpaceListener());
            propertyChangeSupport.addPropertyChangeListener("permUseProp", new PermGenSpaceListener());
            propertyChangeSupport.addPropertyChangeListener("survivorUseProp", new SurvivorSpaceListener());
            
            propertyChangeSupport.addPropertyChangeListener("yongGcTimeProp", new YongGcTimeListener());
            propertyChangeSupport.addPropertyChangeListener("oldGcTimeProp", new OldGcTimeListener());
            propertyChangeSupport.addPropertyChangeListener("totalThreadProp", new TotalThreadListener());
            propertyChangeSupport.addPropertyChangeListener("cpuUseProp", new CpuUseListener());
            propertyChangeSupport.addPropertyChangeListener("clazzProp", new ClazzListener());

        }

        private void initModels(ApplicationMonitorModel model) {
            liveModel = model.isLive();
        }

        private void refresh(ApplicationMonitorModel model, String[] details) {
            //编译的总时间
            long totalCompilationTime = model.getTotalCompilationTime();
            long collectionCount = model.getCollectionCount();
            long collectionTime = model.getProcessGcTime();
            if (liveModel) {
                long[] edenArr = model.getEdenSpaceUsage();
                VisualGCJson gcJson = new VisualGCJson();
                gcJson.setTime(model.getTimestamp());
                gcJson.setUptime(getTime(model.getUpTime()));
                if (model.getTimestamp() / 1000 % 21600 == 0) {//每6小时放一次数据
                    permUsed = 0.0;
                    cpuUse = -1;
                    clazzUse = 0;
                }

                Map<String, Double> eden = new HashMap<String, Double>();
                eden.put("committed", SimpleXYChartUtils.formatMemSize(edenArr[0], 3));
                eden.put("init", SimpleXYChartUtils.formatMemSize(edenArr[1], 3));
                eden.put("max", SimpleXYChartUtils.formatMemSize(edenArr[2], 3));
                eden.put("used", SimpleXYChartUtils.formatMemSize(edenArr[3], 3));
                eden.put("percent", (10000 * edenArr[3] / edenArr[2]) / 100.0);
                eden.put("yongGcCount", (double) model.getYongGcCount());
                eden.put("yongGcTime", (double) model.getYongGcTime());
                gcJson.setEden(eden);
                Double oygct = ygct;
                ygct = eden.get("yongGcTime");
                if (oygct != 0) {
                    propertyChangeSupport.firePropertyChange("yongGcTimeProp", oygct, ygct);
                }

                long[] survivorArr = model.getSurvivorSpaceUsage();
                Map<String, Double> survivor = new HashMap<String, Double>();
                survivor.put("committed", SimpleXYChartUtils.formatMemSize(survivorArr[0], 3));
                survivor.put("init", SimpleXYChartUtils.formatMemSize(survivorArr[1], 3));
                survivor.put("max", SimpleXYChartUtils.formatMemSize(survivorArr[2], 3));
                survivor.put("used", SimpleXYChartUtils.formatMemSize(survivorArr[3], 3));
                survivor.put("percent", (10000 * survivorArr[3] / survivorArr[2]) / 100.0);
                gcJson.setSurvivor(survivor);
                Double su = survivorUsed;
                survivorUsed = SimpleXYChartUtils.formatMemSize(survivorArr[3], 1);
                propertyChangeSupport.firePropertyChange("survivorUseProp", su, survivorUsed);


                long[] oldArr = model.getOldGenSpaceUsage();
                Map<String, Double> old = new HashMap<String, Double>();
                old.put("committed", SimpleXYChartUtils.formatMemSize(oldArr[0], 3));
                old.put("init", SimpleXYChartUtils.formatMemSize(oldArr[1], 3));
                old.put("max", SimpleXYChartUtils.formatMemSize(oldArr[2], 3));
                old.put("used", SimpleXYChartUtils.formatMemSize(oldArr[3], 3));
                old.put("percent", (10000 * oldArr[3] / oldArr[2]) / 100.0);
                old.put("oldGcCount", (double) model.getOldGcCount());
                old.put("oldGcTime", (double) model.getOldGcTime());
                gcJson.setOld(old);
                Double ou = oldUsed;
                oldUsed = SimpleXYChartUtils.formatMemSize(oldArr[3], 1);
                propertyChangeSupport.firePropertyChange("oldUseProp", ou, oldUsed);
                
                Double oogct = ogct;
                ogct = old.get("oldGcTime");
                if (oogct != 0) {
                    propertyChangeSupport.firePropertyChange("oldGcTimeProp", oogct, ogct);
                }

                long[] permArr = model.getPermGenSpaceUsage();
                Map<String, Double> perm = new HashMap<String, Double>();
                perm.put("committed", SimpleXYChartUtils.formatMemSize(permArr[0], 3));
                perm.put("init", SimpleXYChartUtils.formatMemSize(permArr[1], 3));
                perm.put("max", SimpleXYChartUtils.formatMemSize(permArr[2], 3));
                perm.put("used", SimpleXYChartUtils.formatMemSize(permArr[3], 3));
                perm.put("percent", (10000 * permArr[3] / permArr[2]) / 100.0);
                gcJson.setPerm(perm);
                Double pu = permUsed;
                permUsed = SimpleXYChartUtils.formatMemSize(permArr[3], 1);
                propertyChangeSupport.firePropertyChange("permUseProp", pu, permUsed);

                /* 编译时间*/
                Map<String, Long> compile = new HashMap<String, Long>();
                compile.put("totalCompilationTime", totalCompilationTime);
                gcJson.setCompile(compile);

                //GC总次数和时间
                Map<String, Long> gctime = new HashMap<String, Long>();
                gctime.put("collectionCount", collectionCount);
                gctime.put("collectionTime", collectionTime);
                gcJson.setGctime(gctime);

                //GC名称
                Map<String, String> gcname = new HashMap<String, String>();
                gcname.put("yongGcName", model.getYongGcName());
                gcname.put("oldGcName", model.getOldGcName());
                gcJson.setGcname(gcname);
                //堆内存
                Map<String, Float> heap = new HashMap<String, Float>();
                heap.put("heapCapacity", ((model.getHeapCapacity() * 100) >> 20) / 100.0f);
                heap.put("heapUsed", ((model.getHeapUsed() * 100) >> 20) / 100.0f);
                heap.put("maxHeap", ((model.getMaxHeap() * 100) >> 20) / 100.0f);
                gcJson.setHeap(heap);
                //类加载
                Map<String, Long> clazz = new HashMap<String, Long>();
                clazz.put("totalLoaded", model.getTotalLoaded());
                clazz.put("totalUnloaded", model.getTotalUnloaded());
                clazz.put("sharedLoaded", model.getSharedLoaded());
                clazz.put("sharedUnloaded", model.getSharedUnloaded());
                gcJson.setClazz(clazz);
                int oclazzUse = clazzUse;
                clazzUse = clazz.get("totalLoaded").intValue();
                propertyChangeSupport.firePropertyChange("clazzProp", oclazzUse, clazzUse);
                //线程
                Map<String, Long> thread = new HashMap<String, Long>();
                thread.put("totalThreads", model.getTotalThreads());
                thread.put("startedThreads", model.getStartedThreads());
                thread.put("daemonThreads", model.getDeamonThreads());
                thread.put("peakThreads", model.getPeakThreads());
                gcJson.setThread(thread);
                Long ototalThreads = totalThreads;
                totalThreads = thread.get("totalThreads");
                propertyChangeSupport.firePropertyChange("totalThreadProp",ototalThreads,totalThreads);

                gcJson.setCpuUsage(StringUtils.isEmpty(details[0]) ? 0.0f : Float.valueOf(details[0]));
                gcJson.setGcUsage(StringUtils.isEmpty(details[1]) ? 0.0f : Float.valueOf(details[1]));
                
                int ocpuUse = cpuUse;
                cpuUse = (int)gcJson.getCpuUsage();
                propertyChangeSupport.firePropertyChange("cpuUseProp",ocpuUse,cpuUse);

                CacheVisualGC.saveVisualGCJsonStr(connectionString, gcJson);
            }
        }
    }

    /**
     * JVM概要信息
     *
     * @author jiangzhixiong
     */
    private static class JvmOverview {
        private String connectionString;
        private boolean liveModel;

        public JvmOverview(ApplicationMonitorModel model, final String connectionString) {
            initModels(model);
            this.connectionString = connectionString;
        }

        private void initModels(ApplicationMonitorModel model) {
            liveModel = model.isLive();
        }

        private void refresh(ApplicationMonitorModel model) {
            if (liveModel) {
                OverviewJson overviewJson = new OverviewJson();
                overviewJson.setPid(model.getPid());
                overviewJson.setHostName(model.getHostName());
                overviewJson.setMainClass(model.getMainClass());
                overviewJson.setMainArgs(model.getMainArgs());
                overviewJson.setVmId(model.getVmId());
                overviewJson.setJavaHome(model.getJavaHome());
                overviewJson.setJavaVersion(model.getJavaVersion());
                overviewJson.setJavaVendor(model.getJavaVendor());
                overviewJson.setJvmFlags(model.getJvmFlags());
                overviewJson.setJvmArgs(model.getJvmArgs());
                overviewJson.setSystemProperties(model.getSystemProperties());
                overviewJson.setUptime(getTime(model.getUpTime()));
                String jsonStr = JSONUtil.write2JsonStr(overviewJson);
                CacheOverview.saveOverviewJsonStr(connectionString, jsonStr);
            }
        }
    }
}
