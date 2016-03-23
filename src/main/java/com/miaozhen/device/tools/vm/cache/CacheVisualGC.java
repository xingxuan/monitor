package com.miaozhen.device.tools.vm.cache;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.miaozhen.device.json.JSONUtil;
import com.miaozhen.device.json.VisualGCJson;
import com.miaozhen.device.tools.vm.tools.jmx.SimpleXYChartUtils;
import com.miaozhen.monitor.Constant;

/**
 * 监控数据的缓存操作
 * @author jiangzhixiong
 *
 */
@Component
public class CacheVisualGC {
	private static CacheSupport cacheSupport;

	//-----保存历史数据开始，默认过期时间为10天
	public static void saveVisualGCJsonStr(final String connectionString, final VisualGCJson gcJson) {
		String visualGcKey = connectionString + "_" + CacheType.VISUAL_GC_LASTJSONSTR;
		String visualGcHistoryKey = connectionString + "_" + CacheType.VISUAL_GC_HISTORYJSONSTR + "_" + SimpleXYChartUtils.getTodayStr();
		String jsonStr = JSONUtil.write2JsonStr(gcJson);
		if(jsonStr != null && jsonStr.length()>0){
			cacheSupport.set(visualGcKey, jsonStr);//保存内存监控实时数据
			if (gcJson.getTime() / 1000 % 10 == 0) {//历史eden数据10秒存一次
				storeHistoryData(visualGcHistoryKey, "[" + gcJson.getTime() + "," + gcJson.getEden().get("used").intValue() + "],");
			}
			//保存最近几分钟数据
			saveShortHistory(connectionString, gcJson);
		}
	}

	private static void saveShortHistory(String connectionString, VisualGCJson gcJson) {
		String str = "[" + gcJson.getTime() + "," + gcJson.getCpuUsage() + "," + gcJson.getGcUsage() + ","
				+ gcJson.getHeap().get("heapCapacity") + "," + gcJson.getHeap().get("heapUsed") + ","
				+ gcJson.getClazz().get("totalLoaded") + "," + gcJson.getClazz().get("totalUnloaded") + ","
				+ gcJson.getThread().get("totalThreads") + "," + gcJson.getThread().get("daemonThreads") + ","
				+ gcJson.getEden().get("used") + "," + gcJson.getSurvivor().get("used") + ","
				+ gcJson.getOld().get("used") + "," + gcJson.getPerm().get("used") + "]";
		String shortHistoryKey = connectionString + "_" + CacheType.SHORT_HISTORY;
		while (cacheSupport.llen(shortHistoryKey) >= CacheType.SHORT_HISTORY_LENGTH) {
			cacheSupport.lpop(shortHistoryKey);
		}
		cacheSupport.rpush(shortHistoryKey, str);
	}

	public static String getShortHistory(final String connectionString) {
		String shortHistoryKey = connectionString + "_" + CacheType.SHORT_HISTORY;
		List<String> result = cacheSupport.lrange(shortHistoryKey, 0, CacheType.SHORT_HISTORY_LENGTH);
		return result.toString();
	}

	public static void saveOldGenSpace(final String connectionString,final String jsonStr){
		String oldGenSpaceHistoryKey = connectionString + "_" + CacheType.OLDGEN_HISTORYJSONSTR +"_"+SimpleXYChartUtils.getTodayStr();
		//cacheSupport.delKey(oldGenSpaceHistoryKey);
		if(jsonStr != null && jsonStr.length()>0){
			storeHistoryData(oldGenSpaceHistoryKey, jsonStr);
		}
	}
	
	public static void savePermGenSpace(final String connectionString,final String jsonStr){
		String permGenSpaceHistoryKey = connectionString + "_" + CacheType.PERMGEN_HISTORYJSONSTR +"_"+SimpleXYChartUtils.getTodayStr();
		//cacheSupport.delKey(permGenSpaceHistoryKey);
		if(jsonStr != null && jsonStr.length()>0){
			storeHistoryData(permGenSpaceHistoryKey, jsonStr);
		}
	}
	
	public static void saveSurvivorSpace(final String connectionString,final String jsonStr){
		String survivorSpaceHistoryKey = connectionString + "_" + CacheType.SURVIVOR_HISTORYJSONSTR +"_"+SimpleXYChartUtils.getTodayStr();
		//cacheSupport.delKey(survivorSpaceHistoryKey);
		if(jsonStr != null && jsonStr.length()>0){
			storeHistoryData(survivorSpaceHistoryKey, jsonStr);
		}
	}
	
	public static void saveYongGcTime(final String connectionString,final String jsonStr){
		String ygctHistoryKey = connectionString + "_" + CacheType.YGCT_HISTORYJSONSTR +"_"+SimpleXYChartUtils.getTodayStr();
		//cacheSupport.delKey(ygctHistoryKey);
		if(jsonStr != null && jsonStr.length()>0){
			storeHistoryData(ygctHistoryKey, jsonStr);
		}
	}
	
	public static void saveOldGcTime(final String connectionString,final String jsonStr){
		String ogctHistoryKey = connectionString + "_" + CacheType.OGCT_HISTORYJSONSTR +"_"+SimpleXYChartUtils.getTodayStr();
		//cacheSupport.delKey(ogctHistoryKey);
		if(jsonStr != null && jsonStr.length()>0){
			storeHistoryData(ogctHistoryKey, jsonStr);
		}
	}
	
	public static void saveTotalThread(final String connectionString,final String jsonStr){
		String threadHistoryKey = connectionString + "_" + CacheType.THREAD_HISTORYJSONSTR +"_"+SimpleXYChartUtils.getTodayStr();
		//cacheSupport.delKey(threadHistoryKey);
		if(jsonStr != null && jsonStr.length()>0){
			storeHistoryData(threadHistoryKey, jsonStr);
		}
	}
	
	public static void saveCpuUse(final String connectionString,final String jsonStr){
		String cpuUseHistoryKey = connectionString + "_" + CacheType.CPU_HISTORYJSONSTR +"_"+SimpleXYChartUtils.getTodayStr();
		//cacheSupport.delKey(cpuUseHistoryKey);
		if(jsonStr != null && jsonStr.length()>0){
			storeHistoryData(cpuUseHistoryKey, jsonStr);
		}
	}

	public static void saveClazzUse(final String connectionString, final String jsonStr) {
		String key = connectionString + "_" + CacheType.CLAZZ_HISTORYJSONSTR + "_" + SimpleXYChartUtils.getTodayStr();
		if (jsonStr != null && jsonStr.length() > 0) {
			storeHistoryData(key, jsonStr);
		}
	}
	//-----保存历史数据结束，默认过期时间为10天====

	public static String getOldGenSpaceJsonStr(final String connectionString,String date) {
		String oldGenSpaceHistoryKey = connectionString + "_" + CacheType.OLDGEN_HISTORYJSONSTR + "_"+ date;
		String s = cacheSupport.get(oldGenSpaceHistoryKey);
		return s != null && s.length() > 0 ? s.substring(0, s.length() - 1) : "";
	}
	public static String getPermGenSpaceJsonStr(final String connectionString,String date) {
		String permGenSpaceHistoryKey = connectionString + "_" + CacheType.PERMGEN_HISTORYJSONSTR + "_"+ date;
		String s = cacheSupport.get(permGenSpaceHistoryKey);
		return s != null && s.length() > 0 ? s.substring(0, s.length() - 1) : "";
	}
	public static String getSurvivorSpaceJsonStr(final String connectionString,String date) {
		String survivorSpaceHistoryKey = connectionString + "_" + CacheType.SURVIVOR_HISTORYJSONSTR + "_"+ date;
		String s = cacheSupport.get(survivorSpaceHistoryKey);
		return s != null && s.length() > 0 ? s.substring(0, s.length() - 1) : "";
	}

	public static String getGcHistory(final String connectionString, String date) {
		String visualGcHistoryKey = connectionString + "_" + CacheType.VISUAL_GC_HISTORYJSONSTR + "_" + date;
		String s = cacheSupport.get(visualGcHistoryKey);
		return s != null && s.length() > 0 ? s.substring(0, s.length() - 1) : "";
	}
	/**
	 * yong gc历史数据
	 * @param connectionString
	 * @param date
	 * @return
	 */
	public static String getYGCTHistory(final String connectionString, String date) {
		String historyKey = connectionString + "_" + CacheType.YGCT_HISTORYJSONSTR + "_" + date;
		String s = cacheSupport.get(historyKey);
		return s != null && s.length() > 0 ? s.substring(0, s.length() - 1) : "";
	}
	/**
	 * full gc历史数据
	 * @param connectionString
	 * @param date
	 * @return
	 */
	public static String getOGCTHistory(final String connectionString, String date) {
		String historyKey = connectionString + "_" + CacheType.OGCT_HISTORYJSONSTR + "_" + date;
		String s = cacheSupport.get(historyKey);
		return s != null && s.length() > 0 ? s.substring(0, s.length() - 1) : "";
	}
	/**
	 * cpu历史数据
	 * @param connectionString
	 * @param date
	 * @return
	 */
	public static String getCpuUseHistory(final String connectionString, String date) {
		String historyKey = connectionString + "_" + CacheType.CPU_HISTORYJSONSTR + "_" + date;
		String s = cacheSupport.get(historyKey);
		return s != null && s.length() > 0 ? s.substring(0, s.length() - 1) : "";
	}

	public static String getClazzHistory(final String connectionString, String date) {
		String historyKey = connectionString + "_" + CacheType.CLAZZ_HISTORYJSONSTR + "_" + date;
		String s = cacheSupport.get(historyKey);
		return s != null && s.length() > 0 ? s.substring(0, s.length() - 1) : "";
	}
	/**
	 * 线程历史数据
	 * @param connectionString
	 * @param date
	 * @return
	 */
	public static String getTotalThreadHistory(final String connectionString, String date) {
		String historyKey = connectionString + "_" + CacheType.THREAD_HISTORYJSONSTR + "_" + date;
		String s = cacheSupport.get(historyKey);
		return s != null && s.length() > 0 ? s.substring(0, s.length() - 1) : "";
	}
	
	public static String getVisualGCJsonStr(final String connectionString) {
		String visualGcKey = connectionString + "_" + CacheType.VISUAL_GC_LASTJSONSTR;
		return cacheSupport.get(visualGcKey);
	}
	
	public static Long size(final String connectionString){
		String visualGcHistoryKey = connectionString + "_" + CacheType.VISUAL_GC_HISTORYJSONSTR +"_"+SimpleXYChartUtils.getTodayStr();
		return cacheSupport.llen(visualGcHistoryKey);
	}
	
	@Resource
	public void setCacheSupport(CacheSupport cacheSupport) {
		this.cacheSupport = cacheSupport;
	}
	public static Long size(final String connectionString,String date){
		String visualGcHistoryKey = connectionString + "_" + CacheType.VISUAL_GC_HISTORYJSONSTR +"_"+date;
		return cacheSupport.llen(visualGcHistoryKey);
	}
	
	public static long storeHistoryData(String key,String value){
		long result = 0;
		//如果不存在，表示第一次添加到缓存
		if(!cacheSupport.exists(key)){
			result = cacheSupport.append(key, value);
			cacheSupport.expire(key, Constant.CACHESTORETIME);
		}else {
			result = cacheSupport.append(key, value);
		}
		
		return result;
	}


}
