package org.jvm.device.tools.vm.core.datasupport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 一个属性变化工具类对应一个单一线程的线程池
 * @author jiangzhixiong
 *
 */
public class AsyncPropertyChangeSupport extends PropertyChangeSupport {
	private static final long serialVersionUID = 1L;
	/**
	 * 单一线程的线程池
	 */
	private transient ExecutorService executor = Executors.newSingleThreadExecutor();

	/**
	 * 构造函数
	 * @param sourceBean
	 */
	public AsyncPropertyChangeSupport(Object sourceBean) {
		
		super(sourceBean);
		
	}
	
	/**
	 * 关闭线程池
	 */
	public void shutdown(){
		executor.shutdown();
	}
	
	/**
	 * 对所有已经注册的监听器激发一个PropertyChangeEvent的事件
	 * 如果给定事件的旧值和新值相等并且都是非 null 的，则不会激发事件
	 */
	@Override
	public void firePropertyChange(final PropertyChangeEvent evt) {
		if (evt == null) {
			throw new NullPointerException();
		}
		executor.submit(new Runnable() {
			public void run() {
				//System.out.println(Thread.currentThread().getName());
				AsyncPropertyChangeSupport.super.firePropertyChange(evt);
			}
		});
		
		
	}

}
