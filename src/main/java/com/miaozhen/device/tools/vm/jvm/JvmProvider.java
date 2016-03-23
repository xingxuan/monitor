package com.miaozhen.device.tools.vm.jvm;

import com.miaozhen.device.tools.vm.application.Application;
import com.miaozhen.device.tools.vm.application.jvm.Jvm;
import com.miaozhen.device.tools.vm.core.model.AbstractModelProvider;
import com.miaozhen.device.tools.vm.tools.jmx.JmxModel;
import com.miaozhen.device.tools.vm.tools.jmx.JmxModelFactory;
import com.miaozhen.device.tools.vm.tools.jmx.JvmMXBeans;
import com.miaozhen.device.tools.vm.tools.jmx.JvmMXBeansFactory;

/**
 * Jvm的提供者,通过application创建JVMImpl实例
 * {@link JVMImpl}
 * @author jiangzhixiong
 *
 */
public class JvmProvider extends AbstractModelProvider<Jvm, Application>{

	@Override
	public Jvm createModelFor(Application app) {
		JVMImpl jvm = null;
		//获取JmxModel实例
		JmxModel jmxModel = JmxModelFactory.getJmxModelFor(app);
		//如果jmx是连接状态，则获取Jvm的管理MXBean信息
        if (jmxModel != null && jmxModel.getConnectionState() == JmxModel.ConnectionState.CONNECTED) {
            JvmMXBeans jmx = JvmMXBeansFactory.getJvmMXBeans(jmxModel);
            if (jmx != null && jmx.getRuntimeMXBean() != null) {
                jvm = new JVMImpl(app);
            }
        }
        
        return jvm;
	}

}
