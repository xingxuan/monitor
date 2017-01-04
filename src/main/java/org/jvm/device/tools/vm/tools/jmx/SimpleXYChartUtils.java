package org.jvm.device.tools.vm.tools.jmx;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SimpleXYChartUtils {
	private static final NumberFormat PERCENT_FORMATTER;
	private static final NumberFormat GENERAL_FORMATTER;
	public static final String DEFAULT_DOUBLE = "0.000";
	public static final BigDecimal d1024 = BigDecimal.valueOf(1024);
	private static SimpleDateFormat sdf;
	
	static{
		PERCENT_FORMATTER = NumberFormat.getPercentInstance();
        PERCENT_FORMATTER.setMinimumFractionDigits(1);
        PERCENT_FORMATTER.setMaximumIntegerDigits(3);
        
        GENERAL_FORMATTER = NumberFormat.getNumberInstance();
        GENERAL_FORMATTER.setMinimumFractionDigits(3);
        GENERAL_FORMATTER.setMaximumIntegerDigits(3);
        
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        
	}
	
	public static String formatPercent(double value) {
        return PERCENT_FORMATTER.format(value / 100);
    }
	
	public static String formatDouble(long value,double chartFactor) {
		String result = formatDouble(value * chartFactor);
		if(result != null && "0".equals(result)) result = DEFAULT_DOUBLE;
		return result;
    }
	public static String formatDouble(float value,double chartFactor) {
		String result = formatDouble(value * chartFactor);
		if(result != null && "0".equals(result)) result = DEFAULT_DOUBLE;
		return result;
    }
	
	public static String formatDouble(double value) {
        return GENERAL_FORMATTER.format(value);
    }
	
	
	public static String formatPercent(long value,double chartFactor) {
        return formatPercent(value * chartFactor);
    }
	
	public static String formatPercent(float value,double chartFactor) {
        return formatPercent(value * chartFactor);
    }
	
	/**
	 * 将字节转换成单位
	 * @param b
	 * @param scale
	 * @return
	 */
	public static double formatMemSize(long b,int scale){
		double d = BigDecimal.valueOf(b).divide(d1024).divide(d1024, scale, BigDecimal.ROUND_HALF_DOWN).doubleValue();
		return d;
	}
	
	public static String getTodayStr() {
		String date = sdf.format(Calendar.getInstance().getTime());
		return date;

	}
	
	

}
