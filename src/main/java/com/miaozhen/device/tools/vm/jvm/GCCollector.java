package com.miaozhen.device.tools.vm.jvm;

/**
 * 各GC的名称
 * @author jiangzhixiong
 *
 */
public abstract class GCCollector {
	public static final String copy = "Copy";
	public static final String markSweepCompact = "MarkSweepCompact";
	public static final String psScavenge = "PS Scavenge";
	public static final String psMarkSweep = "PS MarkSweep";
	public static final String parNew = "ParNew";
	public static final String concurrentMarkSweep = "ConcurrentMarkSweep";

	public static final String g1Young = "G1 Young Generation";
	public static final String g1Old = "G1 Old Generation";
	
	/**
	 * 判断是否为Yong GC
	 * @param gcName
	 * @return
	 */
	public static boolean isYongGC(String gcName){
		if(gcName == null || gcName.length() == 0) return false;
		if(copy.equals(gcName) || psScavenge.equals(gcName) 
				|| parNew.equals(gcName) || g1Young.equals(gcName)){
			return true;
		}
		return false;
	}

}
