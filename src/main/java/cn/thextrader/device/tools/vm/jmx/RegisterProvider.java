package cn.thextrader.device.tools.vm.jmx;

import cn.thextrader.device.tools.vm.application.jvm.JvmFactory;
import cn.thextrader.device.tools.vm.jmx.impl.JmxModelProvider;
import cn.thextrader.device.tools.vm.jvm.JvmProvider;
import cn.thextrader.device.tools.vm.tools.jmx.JmxModelFactory;

/**
 * @author jiangzhixiong
 *
 */
public class RegisterProvider {
	
	public void restored() {
		JmxModelProvider jmxModeProvider = new JmxModelProvider();
		boolean bln = JmxModelFactory.getDefault().registerProvider(jmxModeProvider);
		System.out.println(bln);
		
		//---
		JvmFactory.getDefault().registerProvider(new JvmProvider());
	}

}
