package org.jvm.device.tools.vm.tools.jmx;

import org.jvm.device.tools.vm.application.Application;
import org.jvm.device.tools.vm.core.datasource.DataSource;
import org.jvm.device.tools.vm.core.model.Model;
import org.jvm.device.tools.vm.core.model.ModelFactory;

/**
 * JmxModel工厂类，可以注册JmxModelProvider或获取JmxModel对象
 * 是一个单例类
 * @author jiangzhixiong
 *
 */
public class JmxModelFactory extends ModelFactory<Model, DataSource>{

	private static JmxModelFactory factory;
	private JmxModelFactory() {
    }
	
	public static synchronized JmxModelFactory getDefault() {
        if (factory == null) {
            factory = new JmxModelFactory();
        }
        return factory;
    }
	
	/**
	 * 通过application来JmxModel对象
	 * @param app
	 * @return
	 */
	public static JmxModel getJmxModelFor(Application app) {
        return (JmxModel)getDefault().getModel(app);
    }

}
