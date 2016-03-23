package com.miaozhen.device.tools.vm.jmx.impl;


import java.lang.management.MemoryMXBean;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.miaozhen.device.json.HttpUtil;
import com.miaozhen.device.tools.vm.application.jvm.Jvm;
import com.miaozhen.device.tools.vm.application.jvm.JvmFactory;
import com.miaozhen.device.tools.vm.application.views.monitor.ApplicationMonitorModel;
import com.miaozhen.device.tools.vm.application.views.monitor.ApplicationMonitorView;
import com.miaozhen.device.tools.vm.core.datasource.DataSource;
import com.miaozhen.device.tools.vm.jmx.ConnectionResult;
import com.miaozhen.device.tools.vm.jmx.JmxApplicationsSupport;
import com.miaozhen.device.tools.vm.jvm.JVMImpl;
import com.miaozhen.device.tools.vm.jvm.JvmProvider;
import com.miaozhen.device.tools.vm.tools.jmx.JmxModel;
import com.miaozhen.device.tools.vm.tools.jmx.JmxModel.ConnectionState;
import com.miaozhen.device.tools.vm.tools.jmx.JmxModelFactory;
import com.miaozhen.device.tools.vm.tools.jmx.JvmMXBeans;
import com.miaozhen.device.tools.vm.tools.jmx.JvmMXBeansFactory;
import com.miaozhen.monitor.Constant.ReturnState;
import com.miaozhen.monitor.UserJmx;
import com.miaozhen.monitor.UserJmxDao;

/**
 * 打开一个Jmx对应用的连接
 */
public class OpenJmxApplication {
	protected static UserJmxDao userJmxDao;
	
	@Resource
	public void setUserJmxDao(UserJmxDao userJmxDao) {
		this.userJmxDao = userJmxDao;
	}

	private static final Log LOG = LogFactory.getLog(OpenJmxApplication.class);

	/**
	 * 执行并建立jmx连接
	 * @param connectionStrings
	 * @return 0(成功),1(已经连接),-1(失败),-2(连接已经断开),-9(连接未知)
	 */
	protected static int process(String connectionStrings,String username,String password){
		return openJmxApplication(connectionStrings,username,password);
	}
	
	/**
	 * jmx重连接 0连接成功，-1连接失败
	 * @param connectionStrings
	 * @return
	 */
	protected static int reconnect(String connectionStrings){
		int r = ConnectionResult.FAILURE;
		JmxApplication application = DataSource.ROOT.getRepository().getDataSource(connectionStrings);
		if(application!=null){
			JmxModel model = JmxModelFactory.getJmxModelFor(application);
			r = ((JmxModelImpl)model).reConnect(application);
			if(r == ConnectionResult.SUCCESS ){
				Jvm jvm = JvmFactory.getJVMFor(application);
				JVMImpl jvmImpl = (JVMImpl)jvm;
				jvmImpl.updateMBeanServerConnection();
			}
		}
		return r;
	}
	
	/**
	 * 通过connectionString连接到jmx服务器，连接成功会开启一个定时器，定时的去获取jvm监控数据
	 * @param connectionString
	 * @return int 返回结果码 0(成功),1(已经连接),-1(失败),-2(连接已经断开),-9(连接未知)
	 */
	private static int openJmxApplication(final String connectionString,final String username,final String password) {
		JmxApplication application = DataSource.ROOT.getRepository().getDataSource(connectionString);
		if(application == null) {
			//1.注册JmxModelImpl
			JmxModelFactory.getDefault().registerProvider(new JmxModelProvider());
			//2.注册JvmImpl
			JvmFactory.getDefault().registerProvider(new JvmProvider());
			
			application = (JmxApplication)JmxApplicationsSupport.getInstance().createJmxApplicationInteractive(connectionString, null, 
					username, password);
			if(application != null){
				ApplicationMonitorView view = new ApplicationMonitorView(ApplicationMonitorModel.create(application, true));
				view.init();
				view.monitorView(connectionString);
				return ConnectionResult.SUCCESS;
			}else {
				return ConnectionResult.FAILURE;
			}
		}else {
			JmxModel model = JmxModelFactory.getJmxModelFor(application);
			if(model.getConnectionState() == ConnectionState.CONNECTED) {
				return ConnectionResult.CONNECTED;
			}else if(model.getConnectionState() == ConnectionState.DISCONNECTED){
				return ConnectionResult.DISCONNECTED;
			}else {
				return ConnectionResult.UNKNOW;
			}
			
		}
		
	}
	
	/**
	 * 垃圾收集操作
	 * @param connectionStrings
	 * @param servletRequest
	 */
	public static int gc(String connectionStrings,HttpServletRequest servletRequest){
		JmxApplication application = DataSource.ROOT.getRepository().getDataSource(connectionStrings);
		if(application!=null){
			JmxModel model = JmxModelFactory.getJmxModelFor(application);
			if(model.getConnectionState() == ConnectionState.CONNECTED) {
				JvmMXBeans mxbeans = JvmMXBeansFactory.getJvmMXBeans(model);
				MemoryMXBean memoryMXBean = mxbeans.getMemoryMXBean();
				memoryMXBean.gc();
				return ReturnState.SUCCESS;
			}else {
				return ReturnState.EXCEPTION;
			}
		}else {
			UserJmx jmx = userJmxDao.queryByJmxUrl(connectionStrings);
			if(jmx != null ){
				String result = HttpUtil.gc(jmx,servletRequest);
				if(HttpUtil.ERROR.equals(result)){
					return ReturnState.EXCEPTION;
				}
			}
			return ReturnState.SUCCESS;
		}
	}
	
	/**
	 * 获取线程dump
	 * @param connectionStrings
	 * @param servletRequest
	 * @return
	 */
	protected static String threadStack(String connectionStrings,HttpServletRequest servletRequest){
		String stacks = "";
		JmxApplication application = DataSource.ROOT.getRepository().getDataSource(connectionStrings);
		if(application!=null){
			LOG.info("get threaddump content from local monitor");
			JmxModel model = JmxModelFactory.getJmxModelFor(application);
			if(model.getConnectionState() == ConnectionState.CONNECTED) {
				stacks = model.takeThreadDump();
			}
			if(stacks == null) stacks = "";
			if(stacks.length() > 20){
				LOG.info("local get threaddump content :" + stacks.substring(0, 19));
			}else {
				LOG.info("local get threaddump content :" + stacks);
			}
		}else {
			LOG.info("get threaddump content from remote monitor");
			UserJmx jmx = userJmxDao.queryByJmxUrl(connectionStrings);
			if(jmx != null ){
				stacks = HttpUtil.get(jmx,servletRequest);
			}else {
				stacks = "get thread dump error,ApplicationJson is null";
			}
			
		}
		return stacks;
	}
	
	/**
	 * 删除目标监控应用
	 * @param connectionStrings
	 */
	protected static void removeApplication(String connectionStrings, String appName,HttpServletRequest servletRequest){
		JmxApplication application = DataSource.ROOT.getRepository().getDataSource(connectionStrings);
		//如果是本机在监控，则直接删除
		if(application != null){
			JmxModel model = JmxModelFactory.getJmxModelFor(application);
			((JmxModelImpl)model).markAsDead();
			Jvm jvm = JvmFactory.getJVMFor(application);
			((JVMImpl)jvm).disableTimer();
			model.shutdown();
			DataSource.ROOT.getRepository().removeDataSource(application);
			model = null;
			jvm = null;
			application = null;
			
			 userJmxDao.deleteJmx(connectionStrings);
		}
	}
}
