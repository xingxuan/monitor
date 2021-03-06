package org.jvm.device.tools.vm.application.views;

import org.jvm.device.tools.vm.application.Application;
import org.jvm.device.tools.vm.application.views.monitor.ApplicationMonitorView;
import org.jvm.device.tools.vm.application.views.monitor.ApplicationMonitorViewProvider;


public class ApplicationViewsSupport {
	private static ApplicationViewsSupport sharedInstance;
	
	private ApplicationMonitorViewProvider monitorPluggableView = new ApplicationMonitorViewProvider();

	public static synchronized ApplicationViewsSupport sharedInstance() {
        if (sharedInstance == null) sharedInstance = new ApplicationViewsSupport();
        return sharedInstance;
    }
	
	private ApplicationViewsSupport() {
        
    }
	
	public ApplicationMonitorView getView(Application application){
		return monitorPluggableView.createView(application);
	}
}
