package com.miaozhen.device.tools.vm.core.datasupport;

import java.util.Comparator;

public final class ClassNameComparator implements Comparator {
	
	public static final ClassNameComparator INSTANCE = new ClassNameComparator();

    public int compare(Object o1, Object o2) {
        return o1.getClass().getName().compareTo(o2.getClass().getName());
    }
    
    private ClassNameComparator() {}

}
