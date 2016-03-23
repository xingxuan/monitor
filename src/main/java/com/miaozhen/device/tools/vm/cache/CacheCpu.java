package com.miaozhen.device.tools.vm.cache;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.miaozhen.device.tools.vm.tools.jmx.SimpleXYChartUtils;

@Deprecated
@Component
public class CacheCpu {
	
	private static CacheSupport cacheSupport;

	/**
	 * 保存CPU使用率的信息
	 * 历史key为 127.0.0.1:9008_CPU_HISTORY
	 * 最新值key为 127.0.0.1:9008_CPU_LASTVALUE
	 *
	 * @param value
	 */
	public static void setCpuInfo(String connectionString, final String value) {
		String cpukey = connectionString + "_" + CacheType.CPU;
		String cpuLastValueKey = connectionString + "_" + CacheType.CPU_LASTVALUE;
		if (value != null && value.length() > 0) {
			cacheSupport.set(cpuLastValueKey, value);
			if (!cacheSupport.exists(cpukey) || cacheSupport.get(cpukey) == null) {//如果不存在或值为null，设置一个过期时间
				cacheSupport.setex(cpukey, 60 * 60 * 24, value);
			} else {
				cacheSupport.append(cpukey, "," + value);
			}
		}
	}

	/**
	 *
	 * 获取CPU使用率的信息
	 * @return
	 */
	public static String getCpuInfo(final String connectionString){
		String cpukey = connectionString + "_" + CacheType.CPU;
		String cpuinfo = cacheSupport.get(cpukey);
		if (cpuinfo == null || cpuinfo.length() == 0) {
			return SimpleXYChartUtils.DEFAULT_DOUBLE;
		} else {
			return cpuinfo;
		}
	}
	
	/**
	 * 获取最新CPU使用率
	 * @return
	 */
	public static String getLastCpuInfo(String connectionString){
		String cpuLastValueKey = connectionString + "_" +CacheType.CPU_LASTVALUE;
		String value = cacheSupport.get(cpuLastValueKey);
		if (value == null || value.length() == 0 ) {
			return SimpleXYChartUtils.DEFAULT_DOUBLE;
		}else {
			return value;
		}
	}

	@Resource
	public void setCacheSupport(CacheSupport cacheSupport) {
		this.cacheSupport = cacheSupport;
	}

}
