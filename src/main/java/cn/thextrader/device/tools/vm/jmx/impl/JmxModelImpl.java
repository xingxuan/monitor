package cn.thextrader.device.tools.vm.jmx.impl;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.security.sasl.SaslException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.thextrader.device.tools.vm.application.Application;
import cn.thextrader.device.tools.vm.core.datasupport.DataRemovedListener;
import cn.thextrader.device.tools.vm.core.datasupport.Stateful;
import cn.thextrader.device.tools.vm.host.Host;
import cn.thextrader.device.tools.vm.jmx.ConnectionResult;
import cn.thextrader.device.tools.vm.jmx.EnvironmentProvider;
import cn.thextrader.device.tools.vm.tools.jmx.JmxModel;
import cn.thextrader.device.tools.vm.tools.jmx.JmxModelFactory;
import cn.thextrader.monitor.SpringContextUtils;
import cn.thextrader.monitor.UserJmx;
import cn.thextrader.monitor.UserJmxDao;

/**
 * <p>
 * 这个具体类封装了目标java应用程序的JMX功能.
 * </p>
 * <p>
 * 可以通过调用{@link JmxModelFactory#getJmxModelFor(Application)} 来获得一个
 * {@link JmxModel}类型的实例.
 * </p>
 * 
 * <p>
 * 一般可以按以下方式来使用</>
 * <pre>
 * JmxModel jmx = JmxModelFactory.getJmxModelFor(application); if (jmx == null
 * || jmx.getConnectionState() != JmxModel.ConnectionState.CONNECTED) { // JMX
 * connection not available... } else { MBeanServerConnection mbsc =
 * jmx.getMBeanServerConnection(); if (mbsc != null) { // Invoke JMX
 * operations... } }
 * </pre>
 * 
 * <p>
 * 假使Jmx连接还没有建立,你能在{@code JmxModel}上注册一个监听ConnectionState属性变化的监听器.
 * 当ConnectionState属性变成 CONNECTED and
 * DISCONNECTED时,JmxModel会通知任何一个PropertyChangeListeners的监听器 JmxModel是所有事件发生的源
 * </p>
 * 
 * @author jiangzhixiong
 *
 */
public class JmxModelImpl extends JmxModel {
	private final static Log LOGGER = LogFactory.getLog(JmxModelImpl.class);

	private ProxyClient client;
	private ApplicationRemovedListener removedListener;
	private ApplicationAvailabilityListener availabilityListener;
	private JmxSupport jmxSupport;
	private final Object jmxSupportLock = new Object();

	private static UserJmxDao userJmxDao;

	/**
	 * 通过{@link JmxApplication}对象来创建一个JmxModelImpl实例
	 * 
	 * @param application
	 */
	public JmxModelImpl(JmxApplication application) {
		try {
			final ProxyClient proxyClient = new ProxyClient(this, application);
			client = proxyClient;
			removedListener = new ApplicationRemovedListener();
			availabilityListener = new ApplicationAvailabilityListener();
			connect(application, proxyClient, removedListener, availabilityListener);
			userJmxDao = (UserJmxDao) SpringContextUtils.getBeanById("userJmxDao");
		} catch (Exception e) {
			LOGGER.info("JmxModelImpl<init>", e);
			client = null;
		}
	}

	/**
	 * 重连接,只要去尝试连接，连接成功则从CacheHostDisconnected中删除，再添加到CacheHostConnected中
	 * <p>
	 * <NOTE>重连接失败,一定不能有添加到CacheHostDisconnected的操作</NOTE>
	 * </p>
	 * 
	 * @param application
	 */
	public int reConnect(JmxApplication application) {
		int r = 0;
		try {
			final ProxyClient proxyClient = new ProxyClient(this, application);
			client = proxyClient;
			removedListener = new ApplicationRemovedListener();
			availabilityListener = new ApplicationAvailabilityListener();
			r = connect(application, proxyClient, removedListener, availabilityListener);
			// 如果连接成功，则从重连接列表中删除，再添加到连接成功的列表中
			if (r == 0) {
				String connstr = application.getConnstr();
				userJmxDao.updateJmxConnectedState(connstr);
			}
		} catch (Exception e) {
			r = ConnectionResult.FAILURE;
			LOGGER.info("JmxModelImpl<init>", e);
			client = null;
		}
		return r;
	}

	/**
	 * 0连接成功，-1连接失败
	 * 
	 * @param application
	 * @param proxyClient
	 */
	private int connect(Application application, ProxyClient proxyClient, ApplicationRemovedListener listener, ApplicationAvailabilityListener aListener) {
		int st = 0;
		// while(true){
		try {
			proxyClient.connect();
			// 监听应用是否有效
			application.addPropertyChangeListener(Stateful.PROPERTY_STATE, aListener);
			// break;
		} catch (SecurityException e) {
			// e.printStackTrace();
			st = ConnectionResult.FAILURE;
			LOGGER.info("connect", e);
			client = null;
			// break;
		}
		// }

		return st;
	}

	/**
	 * 获取当前连接的状态
	 */
	public ConnectionState getConnectionState() {
		if (client != null) {
			return client.getConnectionState();
		}
		return ConnectionState.DISCONNECTED;
	}

	/**
	 * @author jiangzhixiong
	 *
	 */
	private class ApplicationRemovedListener implements DataRemovedListener<Application> {

		public void dataRemoved(Application dataSource) {
			client.markAsDead();
			removedListener = null;
		}

	}

	/**
	 * 应用程序有效性监听
	 * 
	 * @author jiangzhixiong
	 *
	 */
	private class ApplicationAvailabilityListener implements PropertyChangeListener {

		public void propertyChange(PropertyChangeEvent evt) {
			// 如果应用程序状态不是有效的 则删除此监听器
			if (!evt.getNewValue().equals(Stateful.STATE_AVAILABLE)) {
				((Application) evt.getSource()).removePropertyChangeListener(Stateful.PROPERTY_STATE, this);
				client.disconnectImpl(false);
				availabilityListener = null;
			}
		}
	}

	/**
	 * ProxyClient实例对jmx代理服务器建立连接的实现,同时这个对象也是一个监听器
	 * 
	 * @author jiangzhixiong
	 *
	 */
	private static class ProxyClient implements NotificationListener {

		private static final int MODE_SELF = 0;
		// private static final int MODE_LOCAL = 1;
		private static final int MODE_GENERIC = 2;

		private final int mode;

		private ConnectionState connectionState = ConnectionState.DISCONNECTED;
		private volatile boolean isDead = true;
		private String userName = null;
		private String password = null;
		// private LocalVirtualMachine lvm;
		private JMXServiceURL jmxUrl = null;

		private Application app;
		private EnvironmentProvider envProvider = null;
		private MBeanServerConnection conn = null;
		private JMXConnector jmxc = null;// JMX客户端
		private static final SslRMIClientSocketFactory sslRMIClientSocketFactory = new SslRMIClientSocketFactory();
		private final JmxModelImpl model;// JmxModel实现
		private boolean insecure;
		private boolean checkSSLStub;

		public ProxyClient(JmxModelImpl model) throws IOException {
			this.mode = MODE_SELF;
			this.model = model;
		}

		/*
		 * public ProxyClient(JmxModelImpl model, LocalVirtualMachine lvm)
		 * throws IOException { this.mode = MODE_LOCAL; this.model = model;
		 * this.lvm = lvm; }
		 */
		public ProxyClient(JmxModelImpl model, Host host, int port) throws IOException {
			this(model, new JMXServiceURL("rmi", "", 0, createUrl(host.getHostName(), port)), null, null);
		}

		public ProxyClient(JmxModelImpl model, String url) throws IOException {
			this(model, new JMXServiceURL(url), null, null);
		}

		/**
		 * 通过JmxMoldelImpl和JmxApplication创建一个ProxClient实例
		 * 
		 * @param model
		 * @param jmxApp
		 * @throws IOException
		 */
		public ProxyClient(JmxModelImpl model, JmxApplication jmxApp) throws IOException {
			this(model, jmxApp.getJMXServiceURL(), jmxApp, jmxApp.getEnvironmentProvider());
		}

		private ProxyClient(JmxModelImpl model, JMXServiceURL url, Application app, EnvironmentProvider envProvider) throws IOException {
			this.mode = MODE_GENERIC;
			this.model = model;
			this.jmxUrl = url;
			this.app = app;
			this.envProvider = envProvider;
		}

		public void setCredentials(String userName, String password) {
			this.userName = userName;
			this.password = password;
		}

		boolean hasSSLStubCheck() {
			return checkSSLStub;
		}

		void setInsecure() {
			insecure = true;
		}

		boolean isInsecure() {
			return insecure;
		}

		private static String createUrl(String hostName, int port) {
			return "/jndi/rmi://" + hostName + ":" + port + "/jmxrmi";
		}

		/**
		 * 设置当前的连接状态,当连接状态变化时，将触发JmxModel.propertyChangeSupport中的监听器
		 * JmxApplicationProvider.addJmxApplication添加监听器
		 * 
		 * @param state
		 */
		private void setConnectionState(ConnectionState state) {
			ConnectionState oldState = connectionState;
			connectionState = state;
			model.propertyChangeSupport.firePropertyChange(JmxModelImpl.CONNECTION_STATE_PROPERTY, oldState, state);
		}

		public ConnectionState getConnectionState() {
			return connectionState;
		}

		/**
		 * 尝试去建立连接
		 */
		void connect() {
			setConnectionState(ConnectionState.CONNECTING);
			try {
				tryConnect();
				setConnectionState(ConnectionState.CONNECTED);
			} catch (SecurityException e) {
				setConnectionState(ConnectionState.DISCONNECTED);
				throw e;
			} catch (SaslException e) {
				setConnectionState(ConnectionState.DISCONNECTED);
				throw new SecurityException(e);
			} catch (Exception e) {
				setConnectionState(ConnectionState.DISCONNECTED);
				if (e.toString().contains("com.sun.enterprise.security.LoginException")) {
					throw new SecurityException("Authentication failed! Invalid username or password");
				}
				final String param = ((jmxUrl != null) ? jmxUrl.toString() : "");
				LOGGER.error("connect(" + param + ")", e);
				throw new SecurityException("jmx Connection refused", e);
			}
		}

		/**
		 * 尝试建立JMX连接
		 * 
		 * @throws IOException
		 */
		private void tryConnect() throws IOException {
			if (mode == MODE_SELF) {
				jmxc = null;
				conn = ManagementFactory.getPlatformMBeanServer();
			} else {
				/*
				 * if (mode == MODE_LOCAL) { if (!lvm.isManageable()) {
				 * lvm.startManagementAgent(); if (!lvm.isManageable()) { throw
				 * new IOException(lvm + " not manageable"); } } if (jmxUrl ==
				 * null) { jmxUrl = new JMXServiceURL(lvm.connectorAddress()); }
				 * }
				 */
				Map<String, Object> env = new HashMap<String, Object>();
				if (envProvider != null) {
					userName = "".equals(envProvider.getUsername()) ? null : envProvider.getUsername();
					password = "".equals(envProvider.getPassword()) ? null : envProvider.getPassword();
				}
				if (userName != null || password != null) {
					env.put(JMXConnector.CREDENTIALS, new String[] { userName, password });
				}
				/*
				 * if (!insecure && mode != MODE_LOCAL &&
				 * env.get(JMXConnector.CREDENTIALS) != null) {
				 * env.put("jmx.remote.x.check.stub", "true"); checkSSLStub =
				 * true; } else { checkSSLStub = false; }
				 */
				jmxc = JMXConnectorFactory.newJMXConnector(jmxUrl, env);
				// 将ProxyClient监听器(ProxyClient实现了NotificationListener)添加到JMXConnector的通知监听器中
				jmxc.addConnectionNotificationListener(this, null, null);

				LOGGER.info("建立jmx connection:" + jmxUrl);

				try {
					jmxc.connect(env);// 建立连接

					MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
					conn = Checker.newChecker(this, mbsc);// 通过动态代理来获得一个MBeanServerConnection
					LOGGER.info("connect success:" + jmxUrl.getURLPath());
				} catch (java.io.IOException e) {
					throw e;
				}

			}
			isDead = false;
		}

		/**
		 * 获取JMX连接
		 * 
		 * @return
		 */
		public MBeanServerConnection getMBeanServerConnection() {
			return conn;
		}

		public JMXServiceURL getUrl() {
			return jmxUrl;
		}

		/**
		 */
		public void disconnect() {
			disconnectImpl(true);
		}

		/**
		 * 断开连接
		 * 
		 * @param sendClose
		 */
		private synchronized void disconnectImpl(boolean sendClose) {
			if (jmxc != null) {
				try {
					jmxc.removeConnectionNotificationListener(this);
					model.jmxSupport.disconnected();
					if (sendClose)
						jmxc.close();
				} catch (IOException e) {
					// Ignore...
				} catch (ListenerNotFoundException e) {
					LOGGER.info("disconnectImpl", e);
				}
				jmxc = null;
			}
			// 设置连接状态为无连接
			if (!isDead) {
				isDead = true;
				setConnectionState(ConnectionState.DISCONNECTED);
			}
		}

		public synchronized void markAsDead() {
			disconnect();
		}

		public boolean isDead() {
			return isDead;
		}

		boolean isConnected() {
			return !isDead();
		}

		/**
		 * JMX连接状态变化时触发此方法
		 */
		public void handleNotification(Notification n, Object hb) {
			if (n instanceof JMXConnectionNotification) {
				if (JMXConnectionNotification.FAILED.equals(n.getType()) || JMXConnectionNotification.CLOSED.equals(n.getType())) {
					markAsDead();
					String connstr = ((JmxApplication) app).getConnstr();
					// 添加到失去连接的缓存set集合中
					userJmxDao.updateJmxDisconnectState(connstr);
					LOGGER.info("the jmx connection is closed..." + connstr);
				}
			}

		}

	}
	// ------------ProxyClient class end

	/**
	 * 通过真实MBeanServerConnection连接获取一个动态代理的MBeanServerConnection实例
	 * 当调用代理的连接实例时,方法会初始拦截
	 * 
	 * @author jiangzhixiong
	 *
	 */
	private static class Checker {

		private Checker() {
		}

		/**
		 * @param client
		 * @param mbsc
		 * @return
		 */
		public static MBeanServerConnection newChecker(ProxyClient client, MBeanServerConnection mbsc) {
			final InvocationHandler ih = new CheckerInvocationHandler(mbsc);
			return (MBeanServerConnection) Proxy.newProxyInstance(Checker.class.getClassLoader(), new Class[] { MBeanServerConnection.class }, ih);
		}
	}

	/**
	 * 
	 */
	private static class CheckerInvocationHandler implements InvocationHandler {
		/**
		 * 真实的MBeanServerConnection连接实例
		 */
		private final MBeanServerConnection conn;

		CheckerInvocationHandler(MBeanServerConnection conn) {
			this.conn = conn;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (LOGGER.isFatalEnabled()) {
				if (EventQueue.isDispatchThread()) {
					Throwable thrwbl = new Throwable();
					LOGGER.info(createTracedMessage("MBeanServerConnection call " + "performed on Event Dispatch Thread!", thrwbl));
				}
			}
			try {
				return method.invoke(conn, args);
			} catch (InvocationTargetException e) {
				throw e.getCause();
			}
		}

		private String createTracedMessage(String message, Throwable thrwbl) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintWriter pw = new PrintWriter(baos);
			pw.println(message);
			thrwbl.printStackTrace(pw);
			pw.flush();
			return baos.toString();
		}
	}
	// -----------CheckerInvocationHandler end

	// ------------LocalVirtualMachine class start
	/*
	 * private static class LocalVirtualMachine {
	 * 
	 * private static final String ENABLE_LOCAL_AGENT_JCMD =
	 * "ManagementAgent.start_local";
	 * 
	 * private int vmid; private boolean isAttachSupported; private String
	 * javaHome;
	 * 
	 * 
	 * volatile private String address;
	 * 
	 * public LocalVirtualMachine(int vmid, boolean canAttach, String
	 * connectorAddress, String home) { this.vmid = vmid; this.address =
	 * connectorAddress; this.isAttachSupported = canAttach; this.javaHome =
	 * home; }
	 * 
	 * public int vmid() { return vmid; }
	 * 
	 * public synchronized boolean isManageable() { return (address != null); }
	 * 
	 * public boolean isAttachable() { return isAttachSupported; }
	 * 
	 * public synchronized void startManagementAgent() throws IOException { if
	 * (address != null) { // already started return; }
	 * 
	 * if (!isAttachable()) { throw new IOException("This virtual machine \"" +
	 * vmid + "\" does not support dynamic attach."); // }
	 * 
	 * loadManagementAgent(); if (address == null) { throw new IOException(
	 * "Fails to find connector address"); } }
	 * 
	 * public synchronized String connectorAddress() { // return null if not
	 * available or no JMX agent return address; } private static final String
	 * LOCAL_CONNECTOR_ADDRESS_PROP =
	 * "com.sun.management.jmxremote.localConnectorAddress";
	 * 
	 * private synchronized void loadManagementAgent() throws IOException {
	 * VirtualMachine vm = null;// String name = String.valueOf(vmid); try { vm
	 * = VirtualMachine.attach(name); } catch (AttachNotSupportedException x) {
	 * throw new IOException(x); } // try to enable local JMX via jcmd command
	 * 
	 * if (!loadManagementAgentViaJcmd(vm)) { // load the management agent into
	 * the target VM loadManagementAgentViaJar(vm); }
	 * 
	 * // get the connector address Properties agentProps =
	 * vm.getAgentProperties(); address = (String)
	 * agentProps.get(LOCAL_CONNECTOR_ADDRESS_PROP);
	 * 
	 * vm.detach(); }
	 * 
	 * private void loadManagementAgentViaJar(VirtualMachine vm) throws
	 * IOException { // Normally in ${java.home}/jre/lib/management-agent.jar
	 * but might // be in ${java.home}/lib in build environments.
	 * 
	 * String agent = javaHome + File.separator + "jre" + File.separator + "lib"
	 * + File.separator + "management-agent.jar"; File f = new File(agent); if
	 * (!f.exists()) { agent = javaHome + File.separator + "lib" +
	 * File.separator + "management-agent.jar"; // f = new File(agent); if
	 * (!f.exists()) { throw new IOException("Management agent not found"); } }
	 * 
	 * agent = f.getCanonicalPath(); try { vm.loadAgent(agent,
	 * "com.sun.management.jmxremote"); } catch (AgentLoadException x) { throw
	 * new IOException(x); } catch (AgentInitializationException x) { throw new
	 * IOException(x); } }
	 * 
	 * 
	 * private boolean loadManagementAgentViaJcmd(VirtualMachine vm) throws
	 * IOException { if (vm instanceof HotSpotVirtualMachine) {
	 * HotSpotVirtualMachine hsvm = (HotSpotVirtualMachine) vm; InputStream in =
	 * null; try { byte b[] = new byte[256]; int n; //in =
	 * hsvm.executeJCmd(ENABLE_LOCAL_AGENT_JCMD); do { n = in.read(b); if (n >
	 * 0) { String s = new String(b, 0, n, "UTF-8"); // System.out.print(s); } }
	 * while (n > 0); return true; } catch (IOException ex) {
	 * LOGGER.log(Level.INFO, "jcmd command \""+ENABLE_LOCAL_AGENT_JCMD+
	 * "\" for PID "+vmid+" failed", ex); // } finally { if (in != null) {
	 * in.close(); } } } return false; }
	 * 
	 * }
	 */
	// ------------LocalVirtualMachine class end

	@Override
	public MBeanServerConnection getMBeanServerConnection() {
		if (client != null) {
			return client.getMBeanServerConnection();
		}
		return null;
	}

	@Override
	public JMXServiceURL getJMXServiceURL() {
		if (client != null) {
			return client.getUrl();
		}
		return null;
	}

	@Override
	public Properties getSystemProperties() {
		return getJmxSupport().getSystemProperties();
	}

	@Override
	public boolean isTakeHeapDumpSupported() {
		return false;
	}

	@Override
	public boolean takeHeapDump(String fileName) {
		return false;
	}

	@Override
	public boolean isTakeThreadDumpSupported() {
		JmxSupport support = getJmxSupport();
		return support.getThreadBean() != null && !support.isReadOnlyConnection();
	}

	@Override
	public String takeThreadDump() {
		return this.jmxSupport.takeThreadDump();
	}

	@Override
	public String takeThreadDump(long[] threadIds) {
		return null;
	}

	@Override
	public String getFlagValue(String name) {
		return null;
	}

	@Override
	public void setFlagValue(String name, String value) {

	}

	private JmxSupport getJmxSupport() {
		synchronized (jmxSupportLock) {
			if (jmxSupport == null) {
				jmxSupport = new JmxSupport(this);
			}
			return jmxSupport;
		}
	}

	public void markAsDead() {
		if (client != null) {
			client.markAsDead();
		}
	}

}
