package org.jvm.device.tools.vm.tools.jmx;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXServiceURL;

import org.jvm.device.tools.vm.core.datasupport.AsyncPropertyChangeSupport;
import org.jvm.device.tools.vm.core.model.Model;

/**
 * <p>这个抽象类封装了目标java应用程序的JMX功能.</p>
 * <p>可以通过调用{@link JmxModelFactory#getJmxModelFor(Application)} 来获得一个{@link JmxModel}类型的实例. </p>
 * 
 * <p>一般可以按以下方式来使用</p>
 * 
 * <pre>
 * JmxModel jmx = JmxModelFactory.getJmxModelFor(application);
 * if (jmx == null || jmx.getConnectionState() != JmxModel.ConnectionState.CONNECTED) {
 *     // JMX connection not available...
 * } else {
 *     MBeanServerConnection mbsc = jmx.getMBeanServerConnection();
 *     if (mbsc != null) {
 *         // Invoke JMX operations...
 *     }
 * }
 * </pre>
 * 
 * <p>假使Jmx连接还没有建立,你能在{@code JmxModel}上注册一个监听ConnectionState属性变化的监听器.
 * 当ConnectionState属性变成 CONNECTED and DISCONNECTED时,JmxModel会通知任何一个PropertyChangeListeners的监听器
 * JmxModel是所有事件发生的源
 * </p>
 * 
 * @author jiangzhixiong
 *
 */
public abstract class JmxModel extends Model {
	/** 监听器工具类  */
	protected PropertyChangeSupport propertyChangeSupport = new AsyncPropertyChangeSupport(this);
	
	public static final String CONNECTION_STATE_PROPERTY = "connectionState";
	
	/**
	 * JMX连接的状态
	 * @author jiangzhixiong
	 *
	 */
	public enum ConnectionState {
		/**
		 * 连接建立成功
		 */
		CONNECTED,
		/**
		 * 当前没有连接
		 */
		DISCONNECTED,
		/**
		 * 连接正准备建立
		 */
		CONNECTING
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
	
	/**
	 * 获取连接状态
	 * @return
	 */
	public abstract ConnectionState getConnectionState();
	
	/**
	 * 返回Jmx连接
	 * {@link MBeanServerConnection MBeanServerConnection} 
	 * @return
	 */
	public abstract MBeanServerConnection getMBeanServerConnection();
	
	public abstract JMXServiceURL getJMXServiceURL();
	
	public abstract Properties getSystemProperties();
	
	public abstract boolean isTakeHeapDumpSupported();
	
	public abstract boolean takeHeapDump(String fileName);
	
	public abstract boolean isTakeThreadDumpSupported();
	
	public abstract String takeThreadDump();
	
	public abstract String takeThreadDump(long[] threadIds);
	
	public abstract String getFlagValue(String name);
	
	public abstract void setFlagValue(String name,String value);
	
	public PropertyChangeSupport getPropertyChangeSupport() {
		return propertyChangeSupport;
	}
	
	public void shutdown(){
		((AsyncPropertyChangeSupport)propertyChangeSupport).shutdown();
	}

}
