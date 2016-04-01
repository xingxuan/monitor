package cn.thextrader.device.tools.vm.application;

import java.lang.management.ManagementFactory;

import cn.thextrader.device.tools.vm.host.Host;

final class ApplicationSupport {
	private static ApplicationSupport instance;
	
	public static synchronized ApplicationSupport getInstance() {
        if (instance == null) instance = new ApplicationSupport();
        return instance;
    }
	
	Application createCurrentApplication() {
        String selfName = ManagementFactory.getRuntimeMXBean().getName();
        final int selfPid = Integer.parseInt(selfName.substring(0, selfName.indexOf('@')));
        return new CurrentApplication(selfPid, Host.LOCALHOST, Host.LOCALHOST.getHostName() + "-" + selfPid);
    }
	
	private ApplicationSupport(){
		
	}
	
	
	/**
	 * 
	 * @author jiangzhixiong
	 *
	 */
    static class CurrentApplication extends Application {

        private int selfPid;
        
        private CurrentApplication(int selfPid, Host host, String id) {
            super(host, id);
            this.selfPid = selfPid;
        }

        public int getPid() {
            return selfPid;
        }
    }

}
