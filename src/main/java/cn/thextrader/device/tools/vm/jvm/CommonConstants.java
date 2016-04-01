package cn.thextrader.device.tools.vm.jvm;

public interface CommonConstants {
	public static final byte THREAD_STATUS_UNKNOWN = -1;//线程未知状态
    public static final byte THREAD_STATUS_ZOMBIE = 0;//僵尸线程
    public static final byte THREAD_STATUS_RUNNING = 1;//线程运行
    public static final byte THREAD_STATUS_SLEEPING = 2;//线程休眠
    public static final byte THREAD_STATUS_MONITOR = 4;//线程监视
    public static final byte THREAD_STATUS_WAIT = 3;//线程等待
    public static final byte THREAD_STATUS_PARK = 5;//线程挂起

}
