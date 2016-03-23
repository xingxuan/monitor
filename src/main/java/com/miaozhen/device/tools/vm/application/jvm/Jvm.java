package com.miaozhen.device.tools.vm.application.jvm;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import com.miaozhen.device.tools.vm.core.model.Model;

/**
 * Jvm的一个抽象
 * @author jiangzhixiong
 *
 */
public abstract class Jvm extends Model {
	protected static final Logger LOGGER = Logger.getLogger(Jvm.class.getName());
	
	public static final String PROPERTY_DUMP_OOME_ENABLED = "prop_oome";
	
	private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
	
	public abstract boolean is14();

    /**
     * Tests if target JVM is JRE 1.5.
     * @return <CODE>true</CODE> if JVM is JRE 1.5, <CODE>false</CODE> otherwise
     */
    public abstract boolean is15();

    /**
     * Tests if target JVM is JRE 1.6.
     * @return <CODE>true</CODE> if JVM is JRE 1.6, <CODE>false</CODE> otherwise
     */
    public abstract boolean is16();

    /**
     * Tests if target JVM is JRE 1.7.
     * @return <CODE>true</CODE> if JVM is JRE 1.7, <CODE>false</CODE> otherwise
     */
    public abstract boolean is17();

    /**
     * Tests if target JVM is JRE 1.8.
     * @return <CODE>true</CODE> if JVM is JRE 1.8, <CODE>false</CODE> otherwise
     * 
     * @since VisualVM 1.3.4
     */
    public boolean is18() {
        return false;
    }

    /**
     * Tests if target JVM is JRE 1.9.
     * @return <CODE>true</CODE> if JVM is JRE 1.9, <CODE>false</CODE> otherwise
     * 
     * @since VisualVM 1.3.8
     */
    public boolean is19() {
        return false;
    }


    public abstract boolean isAttachable();


    public abstract boolean isBasicInfoSupported();


    public abstract String getCommandLine();


    public abstract String getJvmArgs();


    public abstract String getJvmFlags();


    public abstract String getMainArgs();


    public abstract String getMainClass();

    public abstract String getVmVersion();


    public abstract String getJavaVersion();
    

    public abstract String getJavaHome();


    public abstract String getVmInfo();


    public abstract String getVmName();
    

    public abstract String getVmVendor();


    public abstract boolean isMonitoringSupported();
    
    /**
     * 测试目标JVM是否监控编译系统
     * @return
     */
    public abstract boolean isCompileMonitoringSupported();
    

    public abstract boolean isClassMonitoringSupported();


    public abstract boolean isThreadMonitoringSupported();


    public abstract boolean isMemoryMonitoringSupported();


    public abstract boolean isCpuMonitoringSupported();
    

    public abstract boolean isCollectionTimeSupported();


    public abstract void addMonitoredDataListener(MonitoredDataListener l);


    public abstract void removeMonitoredDataListener(MonitoredDataListener l);


    public abstract String[] getGenName();


    public abstract boolean isGetSystemPropertiesSupported();

    public abstract Properties getSystemProperties();


    public abstract boolean isDumpOnOOMEnabledSupported();


    public abstract boolean isDumpOnOOMEnabled();

    public abstract void setDumpOnOOMEnabled(boolean enabled);


    public abstract boolean isTakeHeapDumpSupported();


    public abstract File takeHeapDump() throws IOException;

    public abstract boolean isTakeThreadDumpSupported();


    public abstract File takeThreadDump() throws IOException;
    

    /*public HeapHistogram takeHeapHistogram() {
        // default implementation for backward compatibility
        return null;        
    }*/
     

    public abstract MonitoredData getMonitoredData();
    

    /**
     * 
     * @param listener
     */
    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * 
     */
    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }
    
    /**
     * 触发监听器
     * @param propertyName
     * @param oldValue
     * @param newValue
     */
    protected final void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

}
