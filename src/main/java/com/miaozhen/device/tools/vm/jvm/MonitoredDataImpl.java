package com.miaozhen.device.tools.vm.jvm;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Collection;
import java.util.logging.Logger;

import com.miaozhen.device.tools.vm.application.jvm.MonitoredData;
import com.miaozhen.device.tools.vm.application.views.thread.ThreadDataManager;
import com.miaozhen.device.tools.vm.jmx.impl.JmxApplication;
import com.miaozhen.device.tools.vm.tools.jmx.JvmMXBeans;

/**
 * 被监控数据的实现类
 * @author jiangzhixiong
 *
 */
public class MonitoredDataImpl extends MonitoredData {
	private final static Logger LOGGER = Logger.getLogger(MonitoredDataImpl.class.getName());

	private MonitoredDataImpl(JmxSupport jmxSupport) {
		try {
			Collection<GarbageCollectorMXBean> gcList = jmxSupport.getGarbageCollectorMXBeans();

			if (jmxSupport.hasProcessCPUTimeAttribute()) {
				processCpuTime = jmxSupport.getProcessCPUTime();
			}
			if (gcList != null && !gcList.isEmpty()) {
				for (GarbageCollectorMXBean gcBean : gcList) {
					long time = gcBean.getCollectionTime();
					long count = gcBean.getCollectionCount();
					String gcName = gcBean.getName();
					collectionTime += time;
					collectionCount += count;
					if( GCCollector.isYongGC(gcName) ){
						yongGcCount = count;
						yongGcTime = time;
						yongGcName = gcName;
					}else {
						oldGcCount = count;
						oldGcTime = time;
						oldGcName = gcName;
					}
				}
			}
		} catch (Exception ex) {
			LOGGER.throwing(MonitoredDataImpl.class.getName(),"MonitoredDataImpl.<init>", ex);
		}
	}

	/**
	 * 每隔几秒就创建此类的实例
	 * @param jmxSupport
	 * @param jmxModel
	 */
	MonitoredDataImpl(JmxSupport jmxSupport, JvmMXBeans jmxModel) {
		this(jmxSupport);
		RuntimeMXBean runtimeBean = jmxModel.getRuntimeMXBean();
		threadBean = jmxModel.getThreadMXBean();
		upTime = runtimeBean.getUptime();
		CompilationMXBean compileBean = jmxModel.getCompilationMXBean();
		ClassLoadingMXBean classBean = jmxModel.getClassLoadingMXBean();
		ThreadMXBean threadBean = jmxModel.getThreadMXBean();
		MemoryUsage mem = jmxModel.getMemoryMXBean().getHeapMemoryUsage();
		MemoryPoolMXBean permBean = jmxSupport.getPermGenPool();
		MemoryPoolMXBean edenBean = jmxSupport.getEdenSpacePool();
		MemoryPoolMXBean survivorBean = jmxSupport.getSurvivorSpacePool();
		MemoryPoolMXBean oldBean = jmxSupport.getOldSpacePool();
		unloadedClasses = classBean.getUnloadedClassCount();
		loadedClasses = classBean.getLoadedClassCount() + unloadedClasses;
		sharedLoadedClasses = 0;
		sharedUnloadedClasses = 0;
		
		//线程信息
		threadsDaemon = threadBean.getDaemonThreadCount();
		threadsLive = threadBean.getThreadCount();
		threadsLivePeak = threadBean.getPeakThreadCount();
		threadsStarted = threadBean.getTotalStartedThreadCount();
		
		applicationTime = 0;
		genCapacity = new long[2];
		genUsed = new long[2];
		genMaxCapacity = new long[2];
		genCapacity[0] = mem.getCommitted();//堆大小
		genUsed[0] = mem.getUsed();//堆已使用
		genMaxCapacity[0] = mem.getMax();//堆的最大值
		if (permBean != null) {
			MemoryUsage perm = permBean.getUsage();
			genCapacity[1] = perm.getCommitted();//PermGem大小
			genUsed[1] = perm.getUsed();//PermGem已使用
			genMaxCapacity[1] = perm.getMax();//PermGen最大值
			
			permGenSpaceUsage[0] = perm.getCommitted();
			permGenSpaceUsage[1] = perm.getInit();
			permGenSpaceUsage[2] = perm.getMax();
			permGenSpaceUsage[3] = perm.getUsed();
			
		}
		if(edenBean != null){
			MemoryUsage eden = edenBean.getUsage();
			edenSpaceUsage[0] = eden.getCommitted();
			edenSpaceUsage[1] = eden.getInit();
			edenSpaceUsage[2] = eden.getMax();
			edenSpaceUsage[3] = eden.getUsed();
		}
		if(survivorBean != null){
			MemoryUsage survivor = survivorBean.getUsage();
			survivorSpaceUsage[0] = survivor.getCommitted();
			survivorSpaceUsage[1] = survivor.getInit();
			survivorSpaceUsage[2] = survivor.getMax();
			survivorSpaceUsage[3] = survivor.getUsed();
		}
		if(oldBean != null){
			MemoryUsage old = oldBean.getUsage();
			oldGenSpaceUsage[0] = old.getCommitted();
			oldGenSpaceUsage[1] = old.getInit();
			oldGenSpaceUsage[2] = old.getMax();
			oldGenSpaceUsage[3] = old.getUsed();
		}
		
		totalCompilationTime = compileBean.getTotalCompilationTime();
		
	}
	

}
