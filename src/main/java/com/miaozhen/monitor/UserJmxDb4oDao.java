package com.miaozhen.monitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.google.common.collect.Lists;

public class UserJmxDb4oDao implements InitializingBean {

	public static String dbFile;
	//@Value("${dbFile}")
	public String _dbFile;

	private static final int CONNECTED_STATUS = 0;
	private static final int DISCONNECT_STATUS = -2;

	public static List<String> getJmxurlByAppName(String appName) {
		ObjectContainer oc = Db4oEmbedded.openFile(dbFile);
		try {
			UserJmx param = new UserJmx();
			param.setApplicationName(appName);
			List<UserJmx> list = oc.queryByExample(param);

			if (CollectionUtils.isEmpty(list)) {
				return null;
			}

			List<String> rtnList = new ArrayList<>();
			for (UserJmx userJmx : list) {
				rtnList.add(userJmx.getJmxUrl());
			}
			return rtnList;
		} finally {
			oc.close();
		}

	}

	public static List<UserJmx> getJmxByAppName(String appName) {
		ObjectContainer oc = Db4oEmbedded.openFile(dbFile);

		try {
			UserJmx param = new UserJmx();
			param.setApplicationName(appName);

			ObjectSet<UserJmx> set = oc.queryByExample(param);
			if (CollectionUtils.isEmpty(set)) {
				return null;
			}

			List<UserJmx> list = Lists.newArrayList();
			list.addAll(set);
			return list;
		} finally {
			oc.close();
		}

	}

	public static List<UserJmx> quaryByServerName(String serverName) {
		ObjectContainer oc = Db4oEmbedded.openFile(dbFile);
		try {
			UserJmx param = new UserJmx();
			param.setServerName(serverName);
			ObjectSet<UserJmx> set = oc.queryByExample(param);
			if (CollectionUtils.isEmpty(set)) {
				return null;
			}

			List<UserJmx> list = Lists.newArrayList();
			list.addAll(set);
			return list;
		} finally {
			oc.close();
		}
	}

	public static int addJmx(UserJmx userJmx) {
		ObjectContainer oc = Db4oEmbedded.openFile(dbFile);
		try {
			oc.store(userJmx);
			oc.commit();
		} finally {
			oc.close();
		}
		return 0;
	}

	public static void updateJmx(UserJmx userJmx) {
		ObjectContainer oc = Db4oEmbedded.openFile(dbFile);
		try {
			oc.store(userJmx);
			oc.commit();
		} finally {
			oc.close();
		}
	}

	public static void updateAllJmx(UserJmx userJmx) {
		ObjectContainer oc = Db4oEmbedded.openFile(dbFile);
		try {
			List<UserJmx> list = oc.queryByExample(userJmx);

			if (CollectionUtils.isNotEmpty(list)) {
				UserJmx jmx = list.iterator().next();
				jmx.setMonitorIp(userJmx.getMonitorIp());
				jmx.setMonitorPort(userJmx.getMonitorPort());
				jmx.setServerName(userJmx.getServerName());
				oc.store(jmx);
				oc.commit();
			}
		} finally {
			oc.close();
		}
	}

	public static void updateJmxDisconnectState(String jmxUrl) {
		ObjectContainer oc = Db4oEmbedded.openFile(dbFile);
		try {
			UserJmx param = new UserJmx();
			param.setJmxUrl(jmxUrl);
			List<UserJmx> list = oc.queryByExample(param);

			if (CollectionUtils.isNotEmpty(list)) {
				UserJmx userJmx = list.iterator().next();
				userJmx.setState(DISCONNECT_STATUS);
				oc.store(userJmx);
				oc.commit();
			}
		} finally {
			oc.close();
		}

	}

	public static void updateJmxConnectedState(String jmxUrl) {
		ObjectContainer oc = Db4oEmbedded.openFile(dbFile);
		try {
			UserJmx param = new UserJmx();
			param.setJmxUrl(jmxUrl);
			List<UserJmx> list = oc.queryByExample(param);

			if (CollectionUtils.isNotEmpty(list)) {
				UserJmx userJmx = list.iterator().next();
				userJmx.setState(CONNECTED_STATUS);
				oc.store(userJmx);
				oc.commit();
			}
		} finally {
			oc.close();
		}
	}

	public static String checkJmxUrlExists(String jmxUrl) {
		ObjectContainer oc = Db4oEmbedded.openFile(dbFile);

		try {
			UserJmx param = new UserJmx();
			param.setJmxUrl(jmxUrl);
			List<UserJmx> list = oc.queryByExample(param);

			if (CollectionUtils.isEmpty(list)) {
				return null;
			}
			UserJmx userJmx = list.get(0);
			return userJmx.getJmxUrl();
		} finally {
			oc.close();
		}
	}

	public static int deleteJmx(String jmxUrl) {
		ObjectContainer oc = Db4oEmbedded.openFile(dbFile);
		try {
			UserJmx param = new UserJmx();
			param.setJmxUrl(jmxUrl);
			List<UserJmx> list = oc.queryByExample(param);

			if (CollectionUtils.isNotEmpty(list)) {
				UserJmx userJmx = list.iterator().next();
				oc.delete(userJmx);
				oc.commit();
			}
		} finally {
			oc.close();
		}
		return 0;
	}

	public static List<UserJmx> queryUserJmxPage(UserJmx userJmx) {
		ObjectContainer oc = Db4oEmbedded.openFile(dbFile);

		try {
			ObjectSet<UserJmx> set = oc.queryByExample(userJmx);
			if (CollectionUtils.isEmpty(set)) {
				return null;
			}

			List<UserJmx> list = Lists.newArrayList();
			list.addAll(set);
			return list;
		} finally {
			oc.close();
		}
	}

	public static int queryUserJmxCount(UserJmx userJmx) {
		ObjectContainer oc = Db4oEmbedded.openFile(dbFile);

		try {
			List<UserJmx> list = oc.queryByExample(userJmx);

			if (CollectionUtils.isEmpty(list)) {
				return 0;
			}
			int size = list.size();
			return size;
		} finally {
			oc.close();
		}
	}

	public static List<String> getAppNames() {
		ObjectContainer oc = Db4oEmbedded.openFile(dbFile);

		try {
			List<UserJmx> list = oc.query().execute();

			if (CollectionUtils.isEmpty(list)) {
				return new ArrayList<String>();
			}

			Set<String> set = new HashSet<>();
			for (UserJmx userJmx : list) {
				set.add(userJmx.getApplicationName());
			}
			return new ArrayList<String>(set);
		} finally {
			oc.close();
		}
	}

	public static Set<String> getConnectedAppNames() {
		ObjectContainer oc = Db4oEmbedded.openFile(dbFile);

		try {
			UserJmx param = new UserJmx();
			param.setState(CONNECTED_STATUS);
			List<UserJmx> list = oc.queryByExample(param);

			if (CollectionUtils.isEmpty(list)) {
				return null;
			}

			Set<String> set = new HashSet<>();
			for (UserJmx userJmx : list) {
				set.add(userJmx.getApplicationName());
			}
			return set;
		} finally {
			oc.close();
		}
	}

	public static Set<String> getDisconnectAppNames() {
		ObjectContainer oc = Db4oEmbedded.openFile(dbFile);

		try {
			UserJmx param = new UserJmx();
			param.setState(DISCONNECT_STATUS);
			List<UserJmx> list = oc.queryByExample(param);

			if (CollectionUtils.isEmpty(list)) {
				return null;
			}

			Set<String> set = new HashSet<>();
			for (UserJmx userJmx : list) {
				set.add(userJmx.getApplicationName());
			}
			return set;
		} finally {
			oc.close();
		}
	}

	public static List<UserJmx> getConnectedJmx() {
		ObjectContainer oc = Db4oEmbedded.openFile(dbFile);
		try {
			UserJmx param = new UserJmx();
			param.setState(CONNECTED_STATUS);

			ObjectSet<UserJmx> set = oc.queryByExample(param);
			if (CollectionUtils.isEmpty(set)) {
				return null;
			}

			List<UserJmx> list = Lists.newArrayList();
			list.addAll(set);
			return list;
		} finally {
			oc.close();
		}
	}

	public static List<UserJmx> getJmxByErp(String erp) {
		ObjectContainer oc = Db4oEmbedded.openFile(dbFile);

		try {
			UserJmx param = new UserJmx();
			param.setErp(erp);
			ObjectSet<UserJmx> set = oc.queryByExample(param);
			if (CollectionUtils.isEmpty(set)) {
				return null;
			}

			List<UserJmx> list = Lists.newArrayList();
			list.addAll(set);
			return list;
		} finally {
			oc.close();
		}
	}

	public static List<UserJmx> getDisconnectJmx() {
		ObjectContainer oc = Db4oEmbedded.openFile(dbFile);

		try {
			UserJmx param = new UserJmx();
			param.setState(DISCONNECT_STATUS);
			ObjectSet<UserJmx> set = oc.queryByExample(param);
			if (CollectionUtils.isEmpty(set)) {
				return null;
			}

			List<UserJmx> list = Lists.newArrayList();
			list.addAll(set);
			return list;
		} finally {
			oc.close();
		}
	}

	public static List<String> getMonitorIps() {
		ObjectContainer oc = Db4oEmbedded.openFile(dbFile);

		try {
			List<UserJmx> list = oc.query().execute();

			if (CollectionUtils.isEmpty(list)) {
				return null;
			}

			List<String> rtnList = new ArrayList<>();
			for (UserJmx userJmx : list) {
				rtnList.add(userJmx.getMonitorIp());
			}
			return rtnList;
		} finally {
			oc.close();
		}
	}

	public static UserJmx queryByJmxUrl(String jmxUrl) {
		ObjectContainer oc = Db4oEmbedded.openFile(dbFile);
		try {
			UserJmx param = new UserJmx();
			param.setJmxUrl(jmxUrl);
			List<UserJmx> list = oc.queryByExample(param);

			if (CollectionUtils.isEmpty(list)) {
				return null;
			}
			UserJmx userJmx = list.get(0);
			return userJmx;
		} finally {
			oc.close();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.dbFile = _dbFile;
	}

}
