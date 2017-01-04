package org.jvm.device.tools.vm.jmx.impl;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jvm.device.tools.vm.jmx.ConnectionResult;
import org.jvm.device.tools.vm.jvm.ConnectionConstants;
import org.jvm.monitor.UserJmx;
import org.jvm.monitor.UserJmxDao;

/**
 * 在项目启动时，创建与被监控的JMX连接 并启动一个线程，处理重连接
 * 
 * @author jiangzhixiong
 *
 */
public class JmxConnectionHandler {
	private UserJmxDao userJmxDao;
	private static final Log LOG = LogFactory.getLog(DisConnectedHandlerThread.class);
	private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

	/**
	 * 在应用部署，或重启时，对本机监控的应用进行重新连接.
	 */
	public void start() {
		EXECUTOR_SERVICE.execute(new DisConnectedHandlerThread());

		// 查询所有之前正在被本机器监控的应用
		UserJmx param = new UserJmx();
		param.setState(ConnectionConstants.CONNECTED_STATUS);
		List<UserJmx> jmxs = userJmxDao.queryUserJmxPage(param);
		// 如果没有则路过
		if (jmxs == null)
			return;
		for (int i = 0; i < jmxs.size(); i++) {
			UserJmx jmx = jmxs.get(i);
			int result = OpenJmxApplication.process(jmx.getJmxUrl(), jmx.getJmxUsername(), jmx.getJmxPwd());
			// 如果连接失败，则让其开始重连
			if (result != ConnectionResult.SUCCESS) {
				userJmxDao.updateJmxDisconnectState(jmx.getJmxUrl());
			} else {
				LOG.info("connect" + jmx.getJmxUrl() + "fail");
			}
		}
	}

	/**
	 * 处理jmx重连接的线程,每隔30秒去尝试重连失去连接的jvm
	 * 
	 * @author jiangzhixiong
	 *
	 */
	class DisConnectedHandlerThread implements Runnable {
		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(ConnectionResult.THREADSLEEP);
				} catch (Exception e) {
					e.printStackTrace();
				}
				UserJmx param = new UserJmx();
				param.setState(ConnectionConstants.DISCONNECT_STATUS);
				List<UserJmx> list = userJmxDao.queryUserJmxPage(param);
				if (list == null)
					continue;
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					UserJmx jmx = (UserJmx) iterator.next();
					try {
						int r = OpenJmxApplication.process(jmx.getJmxUrl(), jmx.getJmxUsername(), jmx.getJmxPwd());
						if (r == ConnectionResult.SUCCESS || r == ConnectionResult.CONNECTED) {// 如果启动成功
							jmx.setState(ConnectionConstants.CONNECTED_STATUS);
							jmx.setReconnNum(0);
						} else if (r == ConnectionResult.DISCONNECTED) {// 之前连接成功，被监控应用断开连接时，尝试重连接
							int st = OpenJmxApplication.reconnect(jmx.getJmxUrl());
							if (st == 0) {// 如果连接成功则跳过此循环
								jmx.setState(0);
								jmx.setReconnNum(0);
								continue;
							}
							int rn = jmx.getReconnNum();
							if (rn == ConnectionResult.CONNECTNUM) {
								jmx.setReconnNum(0);
								iterator.remove();
							} else {
								jmx.setReconnNum(rn + 1);
							}
						} else {
							int rn = jmx.getReconnNum();
							if (rn == ConnectionResult.CONNECTNUM) {
								jmx.setReconnNum(0);
								iterator.remove();
							} else {
								jmx.setReconnNum(rn + 1);
							}
						}
					} catch (Exception e) {
						LOG.error(e);
					}

					userJmxDao.updateJmx(jmx);
				}

			}
		}

	}

	public UserJmxDao getUserJmxDao() {
		return userJmxDao;
	}

	public void setUserJmxDao(UserJmxDao userJmxDao) {
		this.userJmxDao = userJmxDao;
	}
}
