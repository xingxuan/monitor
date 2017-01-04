package org.jvm.device.json;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jvm.device.tools.vm.cache.CacheSupport;
import org.jvm.monitor.UserJmx;
/**
 * httpclient工具类
 * @author jiangzhixiong
 *
 */
public class HttpUtil {
	private static final Log LOG = LogFactory.getLog(HttpUtil.class);
	private static CloseableHttpClient httpClient = HttpClients.createDefault();
	public static final int SUCCESS = 200;
	public static final String ERROR = "error";
	private static final int CONNECTTIMEOUT = 5000;

	/**
	 * 通过httpclient去访问真实监控机的dumpthread方法
	 * @param jmx
	 * @return
	 */
	public static String get(UserJmx jmx,HttpServletRequest servletRequest) {
		StringBuilder body = new StringBuilder();
		if(jmx.getMonitorIp().equals(CacheSupport.getLocalIp())) return ERROR;//防止死循环
		String cookieValue = getCookieValue(servletRequest);
		try {
			URI uri = new URIBuilder()
			.setScheme("http")
			.setHost(jmx.getMonitorIp())
			.setPort(Integer.valueOf(jmx.getMonitorPort()))
			.setPath("/dumpthread")
			.setParameter("to", servletRequest.getParameter("to"))
			.setParameter("ip", jmx.getJmxUrl())
			.build();
			
			HttpGet httpget = new HttpGet(uri);
			httpget.setHeader("Host", jmx.getMonitorIp()+":"+jmx.getMonitorPort());
			httpget.setHeader("Cookie", cookieValue);
			
			LOG.info("httpclient get threaddump url:"+httpget.getURI());
			CloseableHttpResponse response = httpClient.execute(httpget);
			HttpEntity entity = response.getEntity();
			try{
				if (entity != null) {
					InputStream is = entity.getContent();
					InputStreamReader bis = null;
					BufferedReader br = null;
					try {
						bis = new InputStreamReader(is);
						br = new BufferedReader(bis);
						String line = br.readLine();
						while(line != null){
							body.append(line).append("\r\n");
							line = br.readLine();
						}
			        } finally {
			        	if(is != null) is.close();
			        	if(br != null) br.close();
			        	if(bis != null) bis.close();
			        }
				}else {
					LOG.info("httpclient get threaddump HttpEntity is null");
				}
			}finally {
				response.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("httpclient get threaddump exception"+e.getMessage());
		}
		return body.toString();
	}
	
	/**
	 * 删除被监控的应用
	 * @param jmx
	 * @param servletRequest
	 */
	public static String deleteApplication(UserJmx jmx,HttpServletRequest servletRequest){
		StringBuilder body = new StringBuilder();
		if(jmx.getMonitorIp().equals(CacheSupport.getLocalIp())) return ERROR;//防止死循环
		String cookieValue = getCookieValue(servletRequest);
		try {
			URI uri = new URIBuilder()
			.setScheme("http")
			.setHost(jmx.getMonitorIp())
			.setPort(Integer.valueOf(jmx.getMonitorPort()))
			.setPath("/appdel")
			.setParameter("to", servletRequest.getParameter("to"))
			.setParameter("ip", jmx.getJmxUrl())
			.setParameter("app", servletRequest.getParameter("app"))
			.setParameter("monitorName", servletRequest.getParameter("monitorName"))
			.build();
			
			HttpGet httpget = new HttpGet(uri);
			httpget.setHeader("Host", jmx.getMonitorIp()+":"+jmx.getMonitorPort());
			httpget.setHeader("Cookie", cookieValue);
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(CONNECTTIMEOUT).build();
			httpget.setConfig(requestConfig);
			
			LOG.info("httpclient delete application:"+httpget.getURI());
			CloseableHttpResponse response = httpClient.execute(httpget);
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode != SUCCESS) return ERROR;
			HttpEntity entity = response.getEntity();
			try{
				if (entity != null) {
					InputStream is = entity.getContent();
					InputStreamReader bis = null;
					BufferedReader br = null;
					try {
						bis = new InputStreamReader(is);
						br = new BufferedReader(bis);
						String line = br.readLine();
						while(line != null){
							body.append(line);
							line = br.readLine();
						}
			        } finally {
			        	if(is != null) is.close();
			        	if(br != null) br.close();
			        	if(bis != null) bis.close();
			        }
				}else {
					LOG.info("httpclient delete application HttpEntity is null");
				}
			}finally {
				response.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("httpclient delete application exception"+e.getMessage());
			return ERROR;
		}
		return body.toString();
	}
	
	/**
	 * 执行垃圾回收操作
	 * @param jmx
	 * @param servletRequest
	 * @return
	 */
	public static String gc(UserJmx jmx,HttpServletRequest servletRequest){
		StringBuilder body = new StringBuilder();
		if(jmx.getMonitorIp().equals(CacheSupport.getLocalIp())) return ERROR;//防止死循环
		String cookieValue = getCookieValue(servletRequest);
		try {
			URI uri = new URIBuilder()
			.setScheme("http")
			.setHost(jmx.getMonitorIp())
			.setPort(Integer.valueOf(jmx.getMonitorPort()))
			.setPath("/appgc")
			.setParameter("to", servletRequest.getParameter("to"))
			.setParameter("ip", jmx.getJmxUrl())
			.build();
			
			HttpGet httpget = new HttpGet(uri);
			httpget.setHeader("Host", jmx.getMonitorIp()+":"+jmx.getMonitorPort());
			httpget.setHeader("Cookie", cookieValue);
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(CONNECTTIMEOUT).build();
			httpget.setConfig(requestConfig);
			
			LOG.info("httpclient gc operation:"+httpget.getURI());
			CloseableHttpResponse response = httpClient.execute(httpget);
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode != SUCCESS) return ERROR;
			HttpEntity entity = response.getEntity();
			try{
				if (entity != null) {
					InputStream is = entity.getContent();
					InputStreamReader bis = null;
					BufferedReader br = null;
					try {
						bis = new InputStreamReader(is);
						br = new BufferedReader(bis);
						String line = br.readLine();
						while(line != null){
							body.append(line);
							line = br.readLine();
						}
			        } finally {
			        	if(is != null) is.close();
			        	if(br != null) br.close();
			        	if(bis != null) bis.close();
			        }
				}else {
					LOG.info("httpclient gc operation HttpEntity is null");
				}
			}finally {
				response.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info("httpclient gc operation exception"+e.getMessage());
			return ERROR;
		}
		return body.toString();
	}
	
	/**
	 * 获取用户请求中的指定cookie值
	 */
	public static String getCookieValue(HttpServletRequest servletRequest) {
		Cookie[] cookies = servletRequest.getCookies();
        StringBuilder cookieValue = new StringBuilder();
        if (cookies != null && cookies.length > 0) {
        	int i = 0;
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                String value = cookie.getValue();
                if(i != 0){
                	cookieValue.append(";");
                }
                cookieValue.append(name).append("=").append(value);
                i++;
            }
        }
        return cookieValue.toString();
    }
}
