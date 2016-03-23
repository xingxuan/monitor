package com.miaozhen.device.tools.vm.cache;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.miaozhen.device.json.JSONUtil;
import com.miaozhen.device.json.OverviewJson;

/**
 * 概要缓存
 * @author jiangzhixiong
 *
 */
@Component
public class CacheOverview {
	
	private static CacheSupport cacheSupport;

	public static void saveOverviewJsonStr(final String connectionString,final String jsonStr) {
		String key = connectionString + "_" + CacheType.OVERVIEW;
		if(jsonStr != null && jsonStr.length()>0){
			cacheSupport.set(key, jsonStr);
		}
	}
	
	public static String getOverviewJsonStr(final String connectionString) {
		String key = connectionString + "_" + CacheType.OVERVIEW;
		return cacheSupport.get(key);
	}

	@Resource
	public void setCacheSupport(CacheSupport cacheSupport) {
		this.cacheSupport = cacheSupport;
	}
	
	public static OverviewJson getOverviewJson(final String connectionString){
		String str = getOverviewJsonStr(connectionString);
		if(str == null || str.length()==0) return new OverviewJson();
		Object obj = JSONUtil.json2Object(str, OverviewJson.class);
		if(obj == null){
			return new OverviewJson();
		}else {
			return (OverviewJson)obj;
		}
		
	}

}
