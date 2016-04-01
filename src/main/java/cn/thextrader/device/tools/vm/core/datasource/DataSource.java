package cn.thextrader.device.tools.vm.core.datasource;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import cn.thextrader.device.tools.vm.core.datasupport.Stateful;

/**
 * applications,hosts等的一个抽象数据源
 * @author jiangzhixiong
 *
 */
public abstract class DataSource {
	
	public static final String PROPERTY_VISIBLE = "prop_visible";
	/**
	 * 属性变化的监听工具类
	 */
	private PropertyChangeSupport changeSupport;
	
	/*全局的根数据源*/
	public static final DataSource ROOT = new DataSource(){};
	
	/**
	 * 整个应用的DataSource的容器
	 */
	private static DataSourceContainer repository;
	/**
	 * 创建并获取PropertyChangeSupport实例
	 * @return
	 */
	protected final synchronized PropertyChangeSupport getChangeSupport() {
        if (changeSupport == null) changeSupport = new PropertyChangeSupport(this);
        return changeSupport;
    }
	
	/**
	 * {@link Stateful}
	 * @param propertyName
	 * @param listener
	 */
	public final void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        getChangeSupport().removePropertyChangeListener(propertyName, listener);
    }
	
	public final void removePropertyChangeListener(PropertyChangeListener listener) {
        getChangeSupport().removePropertyChangeListener(listener);
    }
	
	public final void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        getChangeSupport().addPropertyChangeListener(propertyName, listener);
    }
	
	/**
	 * 添加监听器
	 * @param listener
	 */
	public final void addPropertyChangeListener(PropertyChangeListener listener) {
        getChangeSupport().addPropertyChangeListener(listener);
    }
	
	/**
	 * 返回数据源仓库,如applications,hosts的仓库
	 * @return
	 */
	public final synchronized DataSourceContainer getRepository() {
        if (repository == null) repository = new DataSourceContainer(ROOT);
        return repository;
    }

}
