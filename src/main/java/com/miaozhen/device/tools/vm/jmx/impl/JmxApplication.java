package com.miaozhen.device.tools.vm.jmx.impl;

import javax.management.remote.JMXServiceURL;

import com.miaozhen.device.tools.vm.application.Application;
import com.miaozhen.device.tools.vm.application.jvm.Jvm;
import com.miaozhen.device.tools.vm.core.datasupport.Stateful;
import com.miaozhen.device.tools.vm.host.Host;
import com.miaozhen.device.tools.vm.jmx.EnvironmentProvider;
import com.miaozhen.device.tools.vm.tools.jmx.JmxModel;
import com.miaozhen.device.tools.vm.tools.jmx.JmxModelFactory;
import com.miaozhen.device.tools.vm.tools.jmx.JvmMXBeans;
import com.miaozhen.device.tools.vm.tools.jmx.JvmMXBeansFactory;
import com.miaozhen.device.tools.vm.tools.jmx.JmxModel.ConnectionState;

import java.lang.management.RuntimeMXBean;

/**
 * 这个application代表是通过一个 {@link JMXServiceURL}构建的application
 * @author jiangzhixiong
 *
 */
public final class JmxApplication extends Application {
	
	private int pid = UNKNOWN_PID;
	private final JMXServiceURL url;
	private EnvironmentProvider envProvider;//
	private final String connstr ;
	
	public Jvm jvm;//

	public JmxApplication(Host host,JMXServiceURL url,EnvironmentProvider envProvider,String connstr) {
		super(host, createId(url));
		this.url = url;
		this.envProvider = envProvider;
		this.connstr = connstr;
	}
	
	public JMXServiceURL getJMXServiceURL() {
        return url;
    }
	
	public String getConnstr(){
		return this.connstr;
	}

	/**
	 * 
	 */
	public int getPid() {
        if (pid == UNKNOWN_PID) {
            JmxModel jmxModel = JmxModelFactory.getJmxModelFor(this);//
            
            if (jmxModel != null && jmxModel.getConnectionState() == ConnectionState.CONNECTED) {
            	//
                JvmMXBeans mxbeans = JvmMXBeansFactory.getJvmMXBeans(jmxModel);
                if (mxbeans != null) {
                    RuntimeMXBean rt = mxbeans.getRuntimeMXBean();
                    if (rt != null) {
                        String name = rt.getName();
                        if (name != null && name.indexOf("@") != -1) { // 
                            name = name.substring(0, name.indexOf("@")); // 
                            pid = Integer.parseInt(name);
                        }
                    }
                }
            }
        }
        return pid;
    }
	
	/**
	 *
	 * @param url
	 * @return
	 */
	private static String createId(JMXServiceURL url){
        return url.toString();
    }

    public EnvironmentProvider getEnvironmentProvider() {
        return envProvider;
    }

    public void setStateImpl(int newState) {
        if (newState != Stateful.STATE_AVAILABLE) {
            pid = UNKNOWN_PID;
//            jvm = null;
        }
        setState(newState);
    }

    public String toString() {
        return "JmxApplication [id: " + getId() + "]";
	}

}
