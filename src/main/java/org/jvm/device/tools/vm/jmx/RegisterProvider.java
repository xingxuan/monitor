package org.jvm.device.tools.vm.jmx;

import org.jvm.device.tools.vm.application.jvm.JvmFactory;
import org.jvm.device.tools.vm.jmx.impl.JmxModelProvider;
import org.jvm.device.tools.vm.jvm.JvmProvider;
import org.jvm.device.tools.vm.tools.jmx.JmxModelFactory;

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
