package com.miaozhen.device.tools.vm.jmx.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.miaozhen.device.tools.vm.application.jvm.JvmFactory;
import com.miaozhen.device.tools.vm.core.datasupport.Stateful;
import com.miaozhen.device.tools.vm.host.Host;
import com.miaozhen.device.tools.vm.host.HostsSupport;
import com.miaozhen.device.tools.vm.jmx.EnvironmentProvider;
import com.miaozhen.device.tools.vm.tools.jmx.JmxModel;
import com.miaozhen.device.tools.vm.tools.jmx.JmxModelFactory;
import com.miaozhen.device.tools.vm.tools.jmx.JmxModel.ConnectionState;

import javax.management.remote.JMXServiceURL;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * 创建JmxApplication实例的提供者
 * 通过JmxApplication对象创建JMX连接(JmxModelImpl)
 * 
 * @author jiangzhixiong
 *
 */
public class JmxApplicationProvider {
	private final static Log LOGGER = LogFactory.getLog(JmxApplicationProvider.class);
	
	static final String JMX_SUFFIX = ".jmx";
	

	/**
	 * 创建并返回一个JmxApplication实例
	 * @param newApp
	 * @param serviceURL
	 * @param connectionName
	 * @param displayName
	 * @param hostName
	 * @return
	 * @throws JMXException
	 */
	private JmxApplication addJmxApplication(boolean newApp, JMXServiceURL serviceURL,String connectionName,
			String displayName,String hostName,final String connectionString,EnvironmentProvider provider) throws JMXException {
		//解析JMXServiceURL,如果解析失败则抛出异常
		if (serviceURL == null) {
            try {
                serviceURL = getServiceURL(connectionName);
            } catch (MalformedURLException ex) {
                throw new JMXException(true,"MSG_Invalid_JMX_connection", ex); // 
            }
        }
		Host host = null;
		
		//获取一个Host实例,如果没有获取成功则抛出异常
		try {
            host = getHost(hostName, serviceURL);
        } catch (Exception e) {
        	throw new JMXException(false,"MSG_Cannot_resolve_host", e);
        }
        
        
        //创建一个JmxApplication,通过主机或serviceURL
        final JmxApplication application = new JmxApplication(host, serviceURL,provider,connectionString);
        
        //通过创建的JmxApplication获取一个JmxModelImpl实例连接到JMX代理
        JmxModel model = JmxModelFactory.getJmxModelFor(application);
        if(model.getMBeanServerConnection() == null){
        	throw new JMXException(false,"MSG_jmx_Connection_refused");
        }
        
        //连接状态来更新应用程序的状态
        model.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
            	LOGGER.info(connectionString+" connectionstate changed:"+evt.getNewValue());
                if (evt.getNewValue() == ConnectionState.CONNECTED) {
                    application.setStateImpl(Stateful.STATE_AVAILABLE);
                } else {
                    application.setStateImpl(Stateful.STATE_UNAVAILABLE);
                }
            }
        });
        
        //为JmxApplication应用的Jvm创建实例
        application.jvm = JvmFactory.getJVMFor(application);
        
        //如果没有错误，则添加到仓库中
        host.getRepository().addDataSource(application);
        
        return application;
	}
	
	/**
	 * 创建JmxApplication实例
	 * @param connectionString
	 * @param displayName
	 * @return
	 */
	public JmxApplication createJmxApplication(String connectionString, String displayName,String username,String password){
		final String normalizedConnectionName = normalizeConnectionName(connectionString);
        final JMXServiceURL serviceURL;
        try {
            serviceURL = getServiceURL(normalizedConnectionName);
            String hostName = getHostName(serviceURL);
            hostName = hostName == null ? "" : hostName;
            
            EnvironmentProvider provider = new EnvironmentProvider(username, password);
            
            try {
				return addJmxApplication(true, serviceURL, normalizedConnectionName, displayName,hostName,connectionString,provider);
			} catch (JMXException e) {
				//e.printStackTrace();
				throw e;
			}
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        
	}
	
	/**
	 * 通过连接字符串创建一个JMXServiceURL实例
	 * @param connectionString
	 * @return
	 * @throws MalformedURLException
	 */
	private JMXServiceURL getServiceURL(String connectionString) throws MalformedURLException {
        return new JMXServiceURL(connectionString);
    }
	
	/**
	 * 初始化操作
	 */
	public void initialize() {
		//initApplications();
	}
	
	private void initApplications() {
		try {
			addJmxApplication(false, null,"","displayName","localhost","",null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 标准化一个JMX连接的协议字符串
	 * @param connectionName
	 * @return
	 */
	private String normalizeConnectionName(String connectionName) {
        if (connectionName.startsWith("service:jmx:")) return connectionName;
        return "service:jmx:rmi:///jndi/rmi://" + connectionName + "/jmxrmi";
    }
	
	/**
	 * 判断是为本地主机
	 * @param hostname
	 * @return
	 * @throws IOException
	 */
	private static boolean isLocalHost(String hostname) throws IOException {
        InetAddress remoteAddr = InetAddress.getByName(hostname);
        Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
        while (nis.hasMoreElements()) {
            NetworkInterface ni = nis.nextElement();
            Enumeration<InetAddress> addrs = ni.getInetAddresses();
            while (addrs.hasMoreElements()) {
                InetAddress localAddr = addrs.nextElement();
                if (localAddr.equals(remoteAddr)) {
                    return true;
                }
            }
        }
        return false;
    }
	
	/**
	 * 根据参数创建一个主机实例
	 * @param hostname
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private Host getHost(String hostname, JMXServiceURL url) throws IOException {
		if (hostname != null) {
			if (hostname.isEmpty() || isLocalHost(hostname)) {
				return Host.LOCALHOST;
			} else {
				return HostsSupport.getInstance().getOrCreateHost(hostname,false);
			}
		}

		return Host.UNKNOWN_HOST;
	}
	
	/**
	 * 通过JMXServiceURL获取主机名
	 * @param serviceURL
	 * @return
	 */
	private String getHostName(JMXServiceURL serviceURL) {
        String hostname = serviceURL.getHost();
        if (hostname == null || hostname.isEmpty()) {
            hostname = null;
            if ("rmi".equals(serviceURL.getProtocol()) && serviceURL.getURLPath().startsWith("/jndi/rmi://")) {
                String urlPath = serviceURL.getURLPath().substring("/jndi/rmi://".length()); 
                if ('/' == urlPath.charAt(0)) {
                    hostname = "localhost";
                } else if ('[' == urlPath.charAt(0)) { // IPv6 address
                    int closingSquareBracketIndex = urlPath.indexOf("]");
                    if (closingSquareBracketIndex == -1) {
                        hostname = null;
                    } else {
                        hostname = urlPath.substring(0, closingSquareBracketIndex + 1);
                    }
                } else {
                    int colonIndex = urlPath.indexOf(":"); 
                    int slashIndex = urlPath.indexOf("/"); 
                    int min = Math.min(colonIndex, slashIndex); // NOTE: can be -1!!!
                    if (min == -1) {
                        min = 0;
                    }
                    hostname = urlPath.substring(0, min);
                    if (hostname.isEmpty()) {
                        hostname = "localhost";
                    }
                }
            }
        }
        return hostname;
    }
	
	/**
	 * 自定义的异常
	 * @author jiangzhixiong
	 *
	 */
	private static class JMXException  extends Exception {
		private static final long serialVersionUID = 1L;
		public JMXException(boolean config, String message) { super(message);}
        public JMXException(boolean config, String message, Throwable cause) { super(message,cause);}
    }

}
