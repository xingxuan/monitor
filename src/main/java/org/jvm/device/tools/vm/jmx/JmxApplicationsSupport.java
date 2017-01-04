package org.jvm.device.tools.vm.jmx;

import org.jvm.device.tools.vm.application.Application;
import org.jvm.device.tools.vm.jmx.impl.JmxApplicationProvider;

/**
 * 获取JmxApplications对象的工具类
 * @author jiangzhixiong
 *
 */
public final class JmxApplicationsSupport {
	private static JmxApplicationsSupport instance;
	private JmxApplicationProvider applicationProvider = new JmxApplicationProvider();
	
	public static synchronized JmxApplicationsSupport getInstance() {
        if (instance == null) instance = new JmxApplicationsSupport();
        return instance;
    }
	
	public Application createJmxApplicationInteractive(String connectionString,
			String displayName, String username, String password) {

		return applicationProvider.createJmxApplication(connectionString, displayName, username, password);
	}
	
	
	private Application createJmxApplicationImpl(String connectionString,
			String displayName,String username, String password) throws JmxApplicationException {

		return applicationProvider.createJmxApplication(connectionString,displayName, username,  password);
	}
	
	private JmxApplicationsSupport() {
        applicationProvider.initialize();
    }

}
