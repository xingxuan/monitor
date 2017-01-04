package org.jvm.device.json;

import java.util.List;

import org.jvm.device.tools.vm.application.views.thread.ThreadInf;

public class ThreadDataJson {
	private long time = System.currentTimeMillis();
	private List<ThreadInf> list;
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}

	public List<ThreadInf> getList() {
		return list;
	}

	public void setList(List<ThreadInf> list) {
		this.list = list;
	}
}
