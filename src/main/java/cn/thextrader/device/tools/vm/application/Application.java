package cn.thextrader.device.tools.vm.application;

import cn.thextrader.device.tools.vm.core.datasource.DataSource;
import cn.thextrader.device.tools.vm.core.datasupport.Stateful;
import cn.thextrader.device.tools.vm.host.Host;

/**
 * 一个Java应用程序的抽象实现
 * 每个应用程序被定义为在一个主机上运行的和一个特定的标识
 * @author jiangzhixiong
 *
 */
public abstract class Application extends DataSource implements Stateful{
	
	/**
	 * 当前的应用
	 */
	public static final Application CURRENT_APPLICATION = ApplicationSupport.getInstance().createCurrentApplication();
	
	/**
	 * 未知的进程ID
	 */
	public static final int UNKNOWN_PID = -1;
	
	 private String id;
	 private Host host;//应用程序所属的主机
	 private int state = STATE_AVAILABLE;//应用的状态
	 
	public Application(Host host, String id) {
		if (host == null) throw new IllegalArgumentException("Host cannot be null");
		if (id == null) throw new IllegalArgumentException("Application id cannot be null");
		this.host = host;
		this.id = id;
	}
	
	public final String getId() {
        return id;
    }
	
	public int getPid() {
        return UNKNOWN_PID;
    }
	
	public final Host getHost() {
        return host;
    }
	
	@Override
    public final int hashCode() {
        return getId().hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof Application)) return false;
        Application app = (Application) obj;
        return getId().equals(app.getId());
    }
    
    /**
     * 判断是否为一个本地主机
     * @return
     */
    public final boolean isLocalApplication() {
        return Host.LOCALHOST.equals(getHost());
    }
    
    /**
     * 设置最新的JMX连接状态
     * 通知已经注册的监听器 DataSource$changeSupport
     * @param newState JMX最的连接状态
     */
    protected final synchronized void setState(final int newState) {
    	final int oldState = state;
        state = newState;
        getChangeSupport().firePropertyChange(PROPERTY_STATE, oldState, newState);
    }
    
    @Override
    public String toString() {
        return "Application [id: " + getId() + ", pid: " + getPid() + ", host: " + getHost().getHostName() + "]";   // NOI18N
    }
	
    /**
     * 获取应用状态
     */
    public final synchronized int getState() {
        return state;
    }
	

}
