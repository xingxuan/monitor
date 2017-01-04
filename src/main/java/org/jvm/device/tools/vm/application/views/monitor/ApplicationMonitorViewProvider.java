package org.jvm.device.tools.vm.application.views.monitor;

import org.jvm.device.tools.vm.application.Application;

public class ApplicationMonitorViewProvider {
	

	public ApplicationMonitorView createView(Application application) {
        return new ApplicationMonitorView(ApplicationMonitorModel.create(application, true));
    }

}
