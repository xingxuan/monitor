package cn.thextrader.device.tools.vm.jmx;

/**
 * 创建连接后连接状态
 * @author jiangzhixiong
 *
 */
public interface ConnectionResult {
	int SUCCESS = 0;
	int FAILURE = -1;
	int CONNECTED = 1;
	int DISCONNECTED = -2;
	int UNKNOW = -9;
	
	int CONNECTNUM = 5;//尝试重连次数
	long THREADSLEEP = 1000*30;

}
