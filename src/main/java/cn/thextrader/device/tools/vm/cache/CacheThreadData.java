package cn.thextrader.device.tools.vm.cache;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

/**
 * 线程信息的缓存
 * @author jiangzhixiong
 *
 */
@Component
public class CacheThreadData {
	private static CacheSupport cacheSupport;
	@Resource
	public void setCacheSupport(CacheSupport cacheSupport) {
		this.cacheSupport = cacheSupport;
	}
	
	public static void saveThreadDataJsonStr(final String connectionString,final String jsonStr) {
		String key = connectionString + "_" + CacheType.THREADDATA;
		if(jsonStr != null && jsonStr.length()>0){
			cacheSupport.set(key, jsonStr);
		}
	}
	
	public static String getThreadDataJsonStr(final String connectionString) {
		String key = connectionString + "_" + CacheType.THREADDATA;
		return cacheSupport.get(key);
	}
}
