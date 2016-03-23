package com.miaozhen.device.tools.vm.core.model;

import com.miaozhen.device.tools.vm.core.datasource.DataSource;

/**
 * 提供Model实现类的对象的抽象类
 * @author jiangzhixiong
 *
 * @param <M> Model
 * @param <B> DataSource
 */
public abstract class AbstractModelProvider<M extends Model,B extends DataSource> implements ModelProvider<M,B> {

	public abstract M createModelFor(B app);
	
	public int priority() {
        int depth = 1;
        Class cls = getClass();
        
        for (;!cls.equals(AbstractModelProvider.class);cls=cls.getSuperclass()) {
            depth++;
        }
        return depth;
    }
	
}
