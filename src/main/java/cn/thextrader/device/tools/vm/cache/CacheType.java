package cn.thextrader.device.tools.vm.cache;

public interface CacheType {
	/**
	 * CPU使用率历史记录
	 */
	@Deprecated
	String CPU = "CPU_HISTORY";
	
	/**
	 * CPU使用率最新数据
	 */
	@Deprecated
	String CPU_LASTVALUE = "CPU_LASTVALUE";
	
	/**
	 * 显示内存监控数据的json
	 * key example:jd_wireless_device_monitor_127.0.0.1:9008_VISUAL_GC_LASTJSONSTR
	 */
	String VISUAL_GC_LASTJSONSTR = "VISUAL_GC_LASTJSONSTR";
	
	/**
	 * 内存监控数据的历史
	 * key example:jd_wireless_device_monitor_127.0.0.1:9008_VISUAL_GC_HISTORYJSONSTR
	 */
	String VISUAL_GC_HISTORYJSONSTR = "VISUAL_GC_HISTORYJSONSTR10";
	
	String OLDGEN_HISTORYJSONSTR = "OLDGEN_HISTORYJSONSTR";
	String PERMGEN_HISTORYJSONSTR = "PERMGEN_HISTORYJSONSTR";
	String SURVIVOR_HISTORYJSONSTR = "SURVIVOR_HISTORYJSONSTR";
	
	String YGCT_HISTORYJSONSTR = "YGCT_HISTORYJSONSTR";
	String OGCT_HISTORYJSONSTR = "OGCT_HISTORYJSONSTR";
	String THREAD_HISTORYJSONSTR = "THREAD_HISTORYJSONSTR";
	String CPU_HISTORYJSONSTR = "CPU_HISTORYJSONSTR";
	String CLAZZ_HISTORYJSONSTR = "CLAZZ_HISTORYJSONSTR";

	/**
	 * jvm的概述信息
	 * key example:jd_wireless_device_monitor_127.0.0.1:9008_OVERVIEW
	 */
	String OVERVIEW = "OVERVIEW";
	
	/**
	 * 线程的状态信息
	 */
	String THREADDATA = "THREADDATA";
	
	/**
	 * 被监控JVM的IP和端口集合
	 * 
	 */
	String IPANDPORTSET = "IPANDPORTSET";
	
	/**
	 * 所有被监控应用的hash缓存
	 */
	String APPLICATIONHASH = "APPLICATIONHASH";
	
	/**
	 * 已经连接上的
	 */
	String CONNECTED = "CONNECTED";
	
	/**
	 * 失去连接的
	 */
	String DISCONNECTED = "DISCONNECTED";

	/**
	 * 短时间历史
	 */
	int SHORT_HISTORY_LENGTH = 600;
	String SHORT_HISTORY = "SHORT_HISTORY";
	
}
