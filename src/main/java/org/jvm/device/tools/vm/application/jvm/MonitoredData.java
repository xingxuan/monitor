package org.jvm.device.tools.vm.application.jvm;

import java.lang.management.ThreadMXBean;

/**
 * 被监听的数据抽象类
 * @author jiangzhixiong
 *
 */
public abstract class MonitoredData {
	protected long loadedClasses;
    protected long sharedLoadedClasses;
    protected long sharedUnloadedClasses;
    protected long unloadedClasses;
    protected long threadsDaemon;
    protected long threadsLive;
    protected long threadsLivePeak;
    protected long threadsStarted;
    protected long applicationTime;
    protected long upTime;
    protected long[] genCapacity;
    protected long[] genUsed;
    protected long[] genMaxCapacity;
    protected long processCpuTime;
    
    protected long collectionTime;
    protected long collectionCount;
    protected long yongGcTime;
    protected long yongGcCount;
    protected long oldGcTime;
    protected long oldGcCount;
    protected String yongGcName;
    protected String oldGcName;
    
    protected long totalCompilationTime;
    protected long[] edenSpaceUsage = {0,0,0,0};
    protected long[] survivorSpaceUsage = {0,0,0,0};
    protected long[] oldGenSpaceUsage = {0,0,0,0};
    protected long[] permGenSpaceUsage = {0,0,0,0};

//    protected Jvm monitoredVm;
    
    protected ThreadMXBean threadBean;
    
    public long getLoadedClasses() {
        return loadedClasses;
    }
    
    public long getSharedLoadedClasses() {
        return sharedLoadedClasses;
    }
    

    public long getSharedUnloadedClasses() {
        return sharedUnloadedClasses;
    }
    

    public long getUnloadedClasses() {
        return unloadedClasses;
    }
    

    public long getThreadsDaemon() {
        return threadsDaemon;
    }
    

    public long getThreadsLive() {
        return threadsLive;
    }
    

    public long getThreadsLivePeak() {
        return threadsLivePeak;
    }
    

    public long getThreadsStarted() {
        return threadsStarted;
    }
    

    public long getApplicationTime() {
        return applicationTime;
    }
    

    /*public Jvm getMonitoredVm() {
        return monitoredVm;
    }*/
    

    public long getUpTime() {
        return upTime;
    }
    

    public long[] getGenCapacity() {
        return genCapacity.clone();
    }
    

    public long[] getGenUsed() {
        return genUsed.clone();
    }
    

    public long[] getGenMaxCapacity() {
        return genMaxCapacity.clone();
    }
    

    public long getProcessCpuTime() {
        return processCpuTime;
    }
    

    public long getCollectionTime() {
        return collectionTime;
    }
    public long getCollectionCount() {
		return collectionCount;
	}
    public long getTotalCompilationTime() {
		return totalCompilationTime;
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
    
    public long getYongGcTime() {
		return yongGcTime;
	}
    public long getYongGcCount() {
		return yongGcCount;
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
    public ThreadMXBean getThreadBean() {
		return threadBean;
	}

}
