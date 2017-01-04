package org.jvm.device.tools.vm.jmx;

public final class JmxApplicationException extends Exception {
	
	public JmxApplicationException(String message) {
        super(message);
    }
	
	public JmxApplicationException(String message,Throwable cause) {
        super(message,cause);
    }

}
