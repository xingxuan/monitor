package cn.thextrader.device.tools.vm.application.views.monitor;

import cn.thextrader.device.tools.vm.application.Application;

public class ApplicationMonitorViewProvider {
	

	public ApplicationMonitorView createView(Application application) {
        return new ApplicationMonitorView(ApplicationMonitorModel.create(application, true));
    }

}
