package com.miaozhen.device.tools.vm.tools.jmx;

import javax.management.*;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class CachedMBeanServerConnectionFactory {
	private static final Map<Integer, Map<MBeanServerConnection, WeakReference<CachedMBeanServerConnection>>> snapshots = new HashMap<Integer, Map<MBeanServerConnection, WeakReference<CachedMBeanServerConnection>>>();

	private CachedMBeanServerConnectionFactory() {
	}

	public static CachedMBeanServerConnection getCachedMBeanServerConnection(
			MBeanServerConnection mbsc) {
		return getCachedMBeanServerConnection(mbsc, 0);
	}

	public static CachedMBeanServerConnection getCachedMBeanServerConnection(
			MBeanServerConnection mbsc, int interval)
			throws IllegalArgumentException {
		if (interval < 0) {
			throw new IllegalArgumentException("interval cannot be negative");
		}
		return retrieveCachedMBeanServerConnection(mbsc, interval);
	}

	public static CachedMBeanServerConnection getCachedMBeanServerConnection(
			JmxModel jmx) {
		return getCachedMBeanServerConnection(jmx.getMBeanServerConnection(), 0);
	}

	public static CachedMBeanServerConnection getCachedMBeanServerConnection(
			JmxModel jmx, int interval) throws IllegalArgumentException {
		return getCachedMBeanServerConnection(jmx.getMBeanServerConnection(),
				interval);
	}

	private static synchronized CachedMBeanServerConnection retrieveCachedMBeanServerConnection(
			MBeanServerConnection mbsc, int interval) {
		Map<MBeanServerConnection, WeakReference<CachedMBeanServerConnection>> mbscMap = snapshots
				.get(interval);
		if (mbscMap == null) {
			CachedMBeanServerConnection cmbsc = Snapshot.newSnapshot(mbsc,
					interval);
			Map<MBeanServerConnection, WeakReference<CachedMBeanServerConnection>> mbscMapNew = new WeakHashMap<MBeanServerConnection, WeakReference<CachedMBeanServerConnection>>();
			mbscMapNew.put(mbsc,
					new WeakReference<CachedMBeanServerConnection>(cmbsc));
			snapshots.put(interval, mbscMapNew);
			return cmbsc;
		} else {
			WeakReference<CachedMBeanServerConnection> cmbscRef = mbscMap
					.get(mbsc);
			CachedMBeanServerConnection cmbsc = (cmbscRef == null) ? null
					: cmbscRef.get();
			if (cmbsc == null) {
				cmbsc = Snapshot.newSnapshot(mbsc, interval);
				mbscMap.put(mbsc,
						new WeakReference<CachedMBeanServerConnection>(cmbsc));
			}
			return cmbsc;
		}
	}

	static class Snapshot {

		private Snapshot() {
		}

		public static CachedMBeanServerConnection newSnapshot(
				MBeanServerConnection mbsc, int interval) {
			final InvocationHandler ih = new SnapshotInvocationHandler(mbsc,
					interval);
			return (CachedMBeanServerConnection) Proxy.newProxyInstance(
					Snapshot.class.getClassLoader(),
					new Class[] { CachedMBeanServerConnection.class }, ih);
		}
	}

	static class SnapshotInvocationHandler implements InvocationHandler {

		private final MBeanServerConnection conn;
		private final int interval;
		private Timer timer = null;
		private Map<ObjectName, NameValueMap> cachedValues = newMap();
		private Map<ObjectName, Set<String>> cachedNames = newMap();
		private List<MBeanCacheListener> listenerList = new CopyOnWriteArrayList<MBeanCacheListener>();
		private volatile boolean flushRunning;

		@SuppressWarnings("serial")
		private static final class NameValueMap extends HashMap<String, Object> {
		}

		SnapshotInvocationHandler(MBeanServerConnection conn, int interval) {
			this.conn = conn;
			this.interval = interval;
			if (interval > 0) {
				timer = new Timer(interval, new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						intervalElapsed();
					}
				});
				timer.setCoalesce(true);
				timer.start();
			}
		}

		void intervalElapsed() {
			if (flushRunning)
				return;
			flushRunning = true;
		}

		void notifyListeners() {
			for (MBeanCacheListener listener : listenerList) {
				listener.flushed();
			}
		}

		/*private void connectionPinger() {
			try {
				conn.getDefaultDomain();
			} catch (Exception e) {
				timer.stop();
				listenerList.clear();
				cachedValues.clear();
				cachedNames.clear();
				Collection<Map<MBeanServerConnection, WeakReference<CachedMBeanServerConnection>>> values = snapshots
						.values();
				for (Map<MBeanServerConnection, WeakReference<CachedMBeanServerConnection>> value : values) {
					value.remove(conn);
				}
			}
		}*/

		synchronized void flush() {
			cachedValues = newMap();
		}

		int getInterval() {
			return interval;
		}

		void addMBeanCacheListener(MBeanCacheListener listener) {
			listenerList.add(listener);
		}

		void removeMBeanCacheListener(MBeanCacheListener listener) {
			listenerList.remove(listener);
		}

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			final String methodName = method.getName();
			if (methodName.equals("getAttribute")) { // 
				return getAttribute((ObjectName) args[0], (String) args[1]);
			} else if (methodName.equals("getAttributes")) { // 
				return getAttributes((ObjectName) args[0], (String[]) args[1]);
			} else if (methodName.equals("flush")) { // 
				flush();
				return null;
			} else if (methodName.equals("getInterval")) { // 
				return getInterval();
			} else if (methodName.equals("addMBeanCacheListener")) { // 
				addMBeanCacheListener((MBeanCacheListener) args[0]);
				return null;
			} else if (methodName.equals("removeMBeanCacheListener")) { // 
				removeMBeanCacheListener((MBeanCacheListener) args[0]);
				return null;
			} else {
				try {
					return method.invoke(conn, args);
				} catch (InvocationTargetException e) {
					throw e.getCause();
				}
			}
		}

		private Object getAttribute(ObjectName objName, String attrName)
				throws MBeanException, InstanceNotFoundException,
				AttributeNotFoundException, ReflectionException, IOException {
			final NameValueMap values = getCachedAttributes(objName,
					Collections.singleton(attrName));
			Object value = values.get(attrName);
			if (value != null || values.containsKey(attrName)) {
				return value;
			}
			// Not in cache, presumably because it was omitted from the
			// getAttributes result because of an exception. Following
			// call will probably provoke the same exception.
			return conn.getAttribute(objName, attrName);
		}

		private AttributeList getAttributes(ObjectName objName,
				String[] attrNames) throws InstanceNotFoundException,
				ReflectionException, IOException {
			final NameValueMap values = getCachedAttributes(objName,
					new TreeSet<String>(Arrays.asList(attrNames)));
			final AttributeList list = new AttributeList();
			for (String attrName : attrNames) {
				final Object value = values.get(attrName);
				if (value != null || values.containsKey(attrName)) {
					list.add(new Attribute(attrName, value));
				}
			}
			return list;
		}

		private synchronized NameValueMap getCachedAttributes(
				ObjectName objName, Set<String> attrNames)
				throws InstanceNotFoundException, ReflectionException,
				IOException {
			NameValueMap values = cachedValues.get(objName);
			if (values != null && values.keySet().containsAll(attrNames)) {
				return values;
			}
			attrNames = new TreeSet<String>(attrNames);
			Set<String> oldNames = cachedNames.get(objName);
			if (oldNames != null) {
				attrNames.addAll(oldNames);
			}
			values = new NameValueMap();
			final AttributeList attrs = conn.getAttributes(objName,
					attrNames.toArray(new String[attrNames.size()]));
			for (Attribute attr : attrs.asList()) {
				values.put(attr.getName(), attr.getValue());
			}
			cachedValues.put(objName, values);
			cachedNames.put(objName, attrNames);
			return values;
		}

		private static <K, V> Map<K, V> newMap() {
			return new HashMap<K, V>();
		}
	}

}
