package com.miaozhen.device.tools.vm.application.jvm;

import com.miaozhen.device.tools.vm.application.Application;
import com.miaozhen.device.tools.vm.core.model.ModelFactory;
import com.miaozhen.device.tools.vm.core.model.ModelProvider;

/**
 * jvm工厂类,可以注册ModelFactory实现{@link JvmProvider} 和 获取Jvm的实现{@link JvmImpl}
 * 此类是一个单例实现
 * @author jiangzhixiong
 *
 */
public final class JvmFactory extends ModelFactory<Jvm,Application> implements ModelProvider<Jvm,Application>{

	private static JvmFactory jvmFactory;

    private JvmFactory() {
    }
    
    public static synchronized JvmFactory getDefault() {
        if (jvmFactory == null) {
            jvmFactory = new JvmFactory();
            jvmFactory.registerProvider(jvmFactory);
        }
        return jvmFactory;
    }
    
    public static Jvm getJVMFor(Application app) {
        return getDefault().getModel(app);
    }
    
    public Jvm createModelFor(Application app) {
        return new DefaultJvm();
    }

}
