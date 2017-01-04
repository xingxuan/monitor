package org.jvm.device.tools.vm.core.datasupport;

import java.beans.PropertyChangeListener;

/**
 * 指定实体状态的通用接口
 * @author jiangzhixiong
 *
 */
public interface Stateful {
	
	/**
     */
    public static final String PROPERTY_STATE = "prop_state";
    
    /**
     * 状态不能确定
     */
    public static final int STATE_UNKNOWN = -1;
    /**
     * 不可用状态
     */
    public static final int STATE_UNAVAILABLE = 0;
    /**
     * 可用状态
     */
    public static final int STATE_AVAILABLE = 1;
    
    /** 返回一个实例的当前状态 */
    public int getState();
    
    public void addPropertyChangeListener(PropertyChangeListener listener);
    
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);
    
    public void removePropertyChangeListener(PropertyChangeListener listener);
    
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

}
