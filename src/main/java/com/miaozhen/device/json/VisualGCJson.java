package com.miaozhen.device.json;

import java.util.Map;

public class VisualGCJson {
	private long time = System.currentTimeMillis();
	private String uptime = "";
	private Map<String,Double> eden ;
	private Map<String,Double> survivor ;
	private Map<String,Double> old ;
	private Map<String,Double> perm ;
	
	private Map<String,Long> compile ;
	private Map<String,Long> gctime ;
	private Map<String,String> gcname;

	private float cpuUsage;
	private float gcUsage;
	private Map<String,Long> thread;
	private Map<String,Long> clazz;
	private Map<String, Float> heap;

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	public Map<String, Double> getEden() {
		return eden;
	}
	public void setEden(Map<String, Double> eden) {
		this.eden = eden;
	}
	public Map<String, Double> getSurvivor() {
		return survivor;
	}
	public void setSurvivor(Map<String, Double> survivor) {
		this.survivor = survivor;
	}
	public Map<String, Double> getOld() {
		return old;
	}
	public void setOld(Map<String, Double> old) {
		this.old = old;
	}
	public Map<String, Double> getPerm() {
		return perm;
	}
	public void setPerm(Map<String, Double> perm) {
		this.perm = perm;
	}


	public float getCpuUsage() {
		return cpuUsage;
	}

	public void setCpuUsage(float cpuUsage) {
		this.cpuUsage = cpuUsage;
	}

	public float getGcUsage() {
		return gcUsage;
	}

	public void setGcUsage(float gcUsage) {
		this.gcUsage = gcUsage;
	}

	public Map<String, Long> getThread() {
		return thread;
	}
	public void setThread(Map<String, Long> thread) {
		this.thread = thread;
	}
	public Map<String, Long> getClazz() {
		return clazz;
	}
	public void setClazz(Map<String, Long> clazz) {
		this.clazz = clazz;
	}

	public Map<String, Float> getHeap() {
		return heap;
	}

	public void setHeap(Map<String, Float> heap) {
		this.heap = heap;
	}

	public Map<String, Long> getCompile() {
		return compile;
	}

	public void setCompile(Map<String, Long> compile) {
		this.compile = compile;
	}

	public Map<String, Long> getGctime() {
		return gctime;
	}

	public void setGctime(Map<String, Long> gctime) {
		this.gctime = gctime;
	}
	public Map<String, String> getGcname() {
		return gcname;
	}
	public void setGcname(Map<String, String> gcname) {
		this.gcname = gcname;
	}
	public String getUptime() {
		return uptime;
	}
	public void setUptime(String uptime) {
		this.uptime = uptime;
	}
}
