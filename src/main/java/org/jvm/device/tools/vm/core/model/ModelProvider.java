package org.jvm.device.tools.vm.core.model;

import org.jvm.device.tools.vm.core.datasource.DataSource;

/**
 * Model的提供者接口
 * @author jiangzhixiong
 *
 * @param <M> Model
 * @param <B> DataSource
 */
public interface ModelProvider<M extends Model,B extends DataSource> {
	
	M createModelFor(B dataSource);
	
	int priority();

}
