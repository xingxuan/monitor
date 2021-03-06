package org.jvm.device.tools.vm.jmx.impl;

import org.jvm.device.tools.vm.core.datasource.DataSource;
import org.jvm.device.tools.vm.core.model.AbstractModelProvider;
import org.jvm.device.tools.vm.core.model.Model;
import org.jvm.device.tools.vm.tools.jmx.JmxModel;

/**
 * 返回JmxModelImpl实例的提供类
 * @author jiangzhixiong
 *
 */
public class JmxModelProvider extends AbstractModelProvider<Model, DataSource> {

	public JmxModel createModelFor(DataSource app) {
		
		if (app instanceof JmxApplication) {//JMX连接的实现
			return new JmxModelImpl((JmxApplication) app);
		}
		return null;
	}
}
