package com.miaozhen.device.tools.vm.application.jvm;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

class DefaultJvm extends Jvm {
	
	DefaultJvm() {

	}

    public boolean is14() {
        return false;
    }
    
    public boolean is15() {
        return false;
    }
    
    public boolean is16() {
        return false;
    }
    
    public boolean is17() {
        return false;
    }
        
    public boolean is18() {
        return false;
    }
    
    public boolean is19() {
        return false;
    }
    
    public boolean isAttachable() {
        return false;
    }
    
    public String getCommandLine() {
        throw new UnsupportedOperationException();
    }
    
    public String getJvmArgs() {
        throw new UnsupportedOperationException();
    }
    
    public String getJvmFlags() {
        throw new UnsupportedOperationException();
    }
    
    public String getMainArgs() {
        throw new UnsupportedOperationException();
    }
    
    public String getMainClass() {
        throw new UnsupportedOperationException();
    }
    
    public String getVmVersion() {
        throw new UnsupportedOperationException();
    }
    
    public String getJavaVersion() {
        throw new UnsupportedOperationException();
    }
    
    public String getJavaHome() {
        throw new UnsupportedOperationException();
    }
    
    public String getVmInfo() {
        throw new UnsupportedOperationException();
    }
    
    public String getVmName() {
        throw new UnsupportedOperationException();
    }
    
    public String getVmVendor() {
        throw new UnsupportedOperationException();
    }
    
    public Properties getSystemProperties() {
        throw new UnsupportedOperationException();
    }
    
    public synchronized void addMonitoredDataListener(MonitoredDataListener l) {
        throw new UnsupportedOperationException();
    }
    
    public synchronized void removeMonitoredDataListener(MonitoredDataListener l) {
        throw new UnsupportedOperationException();
    }
    
    public String[] getGenName() {
        throw new UnsupportedOperationException();
    }
    
    public boolean isDumpOnOOMEnabled() {
        throw new UnsupportedOperationException();
    }
    
    public void setDumpOnOOMEnabled(boolean enabled) {
        throw new UnsupportedOperationException();
    }
    
    public File takeHeapDump() throws IOException {
        throw new UnsupportedOperationException();
    }
    
    public File takeThreadDump() throws IOException {
        throw new UnsupportedOperationException();
    }
    
    /*public HeapHistogram takeHeapHistogram() {
        return null;
    }*/
    
    public boolean isBasicInfoSupported() {
        return false;
    }
    
    public boolean isMonitoringSupported() {
        return isClassMonitoringSupported() || isThreadMonitoringSupported() || isMemoryMonitoringSupported();
    }
    
    @Override
    public boolean isCompileMonitoringSupported() {
    	return false;
    }
    
    public boolean isClassMonitoringSupported() {
        return false;
    }
    
    public boolean isThreadMonitoringSupported() {
        return false;
    }
    
    public boolean isMemoryMonitoringSupported() {
        return false;
    }
    
    public boolean isGetSystemPropertiesSupported() {
        return false;
    }
    
    public boolean isDumpOnOOMEnabledSupported() {
        return false;
    }
    
    public boolean isTakeHeapDumpSupported() {
        return false;
    }
    
    public boolean isTakeThreadDumpSupported() {
        return false;
    }
    
    public boolean isCpuMonitoringSupported() {
        return false;
    }
    
    public boolean isCollectionTimeSupported() {
        return false;
    }
    
    public MonitoredData getMonitoredData() {
        return null;
    }

}
