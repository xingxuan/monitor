package com.miaozhen.device.tools.vm.jmx;

import com.miaozhen.device.tools.vm.application.jvm.JvmFactory;
import com.miaozhen.device.tools.vm.jmx.impl.JmxModelProvider;
import com.miaozhen.device.tools.vm.jvm.JvmProvider;
import com.miaozhen.device.tools.vm.tools.jmx.JmxModelFactory;

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
