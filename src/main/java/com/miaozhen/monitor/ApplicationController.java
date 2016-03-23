package com.miaozhen.monitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.miaozhen.device.json.JSONUtil;
import com.miaozhen.device.json.ThreadDataJson;
import com.miaozhen.device.tools.vm.application.views.thread.ThreadInf;
import com.miaozhen.device.tools.vm.cache.CacheOverview;
import com.miaozhen.device.tools.vm.cache.CacheThreadData;
import com.miaozhen.device.tools.vm.cache.CacheVisualGC;
import com.miaozhen.device.tools.vm.jmx.ConnectionResult;
import com.miaozhen.device.tools.vm.jmx.impl.OpenJmxApplication;

/**
 * 被监控应用Controller
 * 
 * @author jiangzhixiong
 *
 */
@Controller
public class ApplicationController extends OpenJmxApplication {
	private final static Log LOGGER = LogFactory.getLog(ApplicationController.class);
	/**
	 * 添加jmx链接
	 */
	@RequestMapping(value = "/addjmx")
	public @ResponseBody Map<String, String> addjmx(UserJmx userjmx, HttpServletRequest request) {
		Map<String, String> map = new HashMap<String, String>();
		try {
			if (StringUtils.isEmpty(userjmx.getApplicationName())) {
				map.put("msg", "jone's application is null");
				return map;
			}
			userjmx.setErp(Constant.ERP);

			String[] serverNames = userjmx.getServerName().split(",");
			String[] ips = userjmx.getJmxUrl().split(",");
			for (int i = 0; i < ips.length; i++) {
				userjmx.setJmxUrl(ips[i]);
				userjmx.setServerName(serverNames[i]);
				if (!StringUtils.isEmpty(userjmx.getJmxUrl())) {
                    String jmxUrl = userJmxDao.checkJmxUrlExists(userjmx.getJmxUrl());
					if (StringUtils.isEmpty(jmxUrl)) {
						int localPort = request.getLocalPort();
						// 建立jmx连接
                        LOGGER.info("建立jmx连接:" + userjmx.getJmxUrl());
						int r = OpenJmxApplication.process(userjmx.getJmxUrl(), userjmx.getJmxUsername(), userjmx.getJmxPwd());

						// 在连接成功时，才添加到数据库
                        LOGGER.info("获取建立连接状态:" + r);
						switch (r) {
						case ConnectionResult.SUCCESS:
							userjmx.setMonitorPort(String.valueOf(localPort));
							userJmxDao.addJmx(userjmx);
							// 判断数据库中是否已经存在此jmx的ip和端口
							map.put("msg", "add jmx success");
							break;
						case ConnectionResult.DISCONNECTED:
							OpenJmxApplication.reconnect(userjmx.getJmxUrl());
							map.put("msg", "app is reconnecting");
							break;
						case ConnectionResult.FAILURE:
							map.put("msg", String.format("FAILURE,connect %s failure", userjmx.getJmxUrl()));
							break;
						default:
							map.put("msg", "Error,After the 30 seconds to retry");
							break;
						}
					} else {
						map.put("msg", "jmx existing");
					}
				}
			}
		} catch (Exception e) {
			map.put("msg", "add jmx error");
			e.printStackTrace();
		}
		return map;
	}

	/**
	 * 显示监控的应用
	 *
	 * @param page
	 * @return
	 */
	@RequestMapping(value = "/applist")
	public ModelAndView applist(@ModelAttribute("page") String page, @ModelAttribute("to") String to, @ModelAttribute("ips") String ips) {
		ModelAndView modelAndView = null;
		if (!StringUtils.isEmpty(to) && to.equals(Constant.TOKEN)) {
			int appNum = userJmxDao.getAppNames().size();
			UserJmx param = new UserJmx();
			param.setErp(Constant.ERP);
			List<UserJmx> jmxs = userJmxDao.queryUserJmxPage(param);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("appSets", jmxs);
			map.put("appNum", appNum);
			modelAndView = new ModelAndView("applist", map);
		}
		return modelAndView;
	}

	/**
	 * 显数据库中监控的应用
	 *
	 * @param page
	 * @return
	 */
	@RequestMapping(value = "/applistfromdb")
	public ModelAndView applistFromDB(@ModelAttribute("page") String page, @ModelAttribute("to") String to, @ModelAttribute UserJmx userjmx) {
		ModelAndView modelAndView = null;
		if (!StringUtils.isEmpty(to) && to.equals(Constant.TOKEN)) {
			int appNum = userJmxDao.getAppNames().size();
			UserJmx param = new UserJmx();
			param.setErp(Constant.ERP);
			List<UserJmx> jmxs = userJmxDao.queryUserJmxPage(param);
			List<String> appNames = userJmxDao.getAppNames();
			List<String> ips = userJmxDao.getMonitorIps();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("appSets", jmxs);
			map.put("appNum", appNum);
			modelAndView = new ModelAndView("applistfromdb", map);
			modelAndView.addObject("appNames", appNames);
			modelAndView.addObject("monitorIps", ips);
		}
		return modelAndView;
	}

	/**
	 * 手工建立jmx连接
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/appopen")
	public @ResponseBody Map<String, Integer> appopen(HttpServletRequest request) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		try {
			String connstr = request.getParameter("connstr");
			UserJmx jmx = userJmxDao.queryByJmxUrl(connstr);
			if (jmx != null) {
				// 如果已经连接，则不再去连接
				if (jmx.getState() == 0) {
					map.put("msg", 1);
					return map;
				}
				// 将应用放到原来监控机中的重连缓存池中，让原来的监控机来启动此应用
				map.put("msg", 0);
			}
		} catch (Exception e) {
			map.put("msg", ConnectionResult.UNKNOW);
			e.printStackTrace();
		}
		return map;
	}

	/**
	 * 删除被监控的应用
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/appdel")
	public @ResponseBody Map<String, Integer> appdel(HttpServletRequest request) {
		String connstr = request.getParameter("ip");
		String app = request.getParameter("app");
		Map<String, Integer> map = new HashMap<String, Integer>();
		try {
			OpenJmxApplication.removeApplication(connstr, app, request);
			map.put("msg", 0);
		} catch (Exception e) {
			map.put("msg", ConnectionResult.UNKNOW);
			e.printStackTrace();
		}
		return map;
	}

	/**
	 * 修改应用的信息
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/appupdate")
	public @ResponseBody Map<String, Integer> appupdate(HttpServletRequest request) {
		String connstr = request.getParameter("ips");
		String mip = request.getParameter("mip");
		String mport = request.getParameter("mpt");
		String appname = request.getParameter("apn");
		Map<String, Integer> map = new HashMap<String, Integer>();
		try {
			UserJmx jmx = userJmxDao.queryByJmxUrl(connstr);
			if (jmx != null) {
				if (mip != null && mip.trim().length() > 0) {
					jmx.setMonitorIp(mip);
				}
				if (mport != null && mport.trim().length() > 0) {
					jmx.setMonitorPort(mport);
				}
				if (appname != null && appname.trim().length() > 0) {
					jmx.setApplicationName(appname);
				}
				userJmxDao.updateJmx(jmx);
			}
			map.put("msg", Constant.ReturnState.SUCCESS);
		} catch (Exception e) {
			map.put("msg", Constant.ReturnState.EXCEPTION);
		}
		return map;
	}
	@RequestMapping(value = "/dumpthread")
	public ModelAndView tackDumpThread(@ModelAttribute("ip") String ip, HttpServletResponse response, HttpServletRequest request) {
		PrintWriter out = null;
		if (ip != null && ip.trim().length() > 0) {
			String result = OpenJmxApplication.threadStack(ip, request);
			// result = result.replace("\n", "\r\n");
			try {
				out = response.getWriter();
				if (result.startsWith("<pre>")) {
					out.println(result);
				} else {
					out.println("<pre>");
					out.println(result);
					out.println("</pre>");
				}
				out.flush();
			} catch (IOException e) {
			} finally {
				if (out != null) {
					out.close();
				}
			}
		}

		return null;
	}

	/**
	 * 执行垃圾回收操作
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/appgc")
	public @ResponseBody Map<String, Integer> appgc(HttpServletRequest request) {
		String connstr = request.getParameter("ip");
		Map<String, Integer> map = new HashMap<String, Integer>();
		try {
			int state = OpenJmxApplication.gc(connstr, request);
			map.put("msg", state);
		} catch (Exception e) {
			map.put("msg", Constant.ReturnState.EXCEPTION);
		}
		return map;
	}

	@RequestMapping(value = "/servlet/cachedData")
	public ModelAndView cachedData(HttpServletRequest request, HttpServletResponse response) {
		String jsonResult = null;// 返回json信息
		String ip = request.getParameter("ip");
		String key = request.getParameter("key");
		if ("lastGcInfo".equals(key)) {
			jsonResult = CacheVisualGC.getVisualGCJsonStr(ip);
		} else if ("overview".equals(key)) {
			jsonResult = CacheOverview.getOverviewJsonStr(ip);
		} else if ("threadInfo".equals(key)) {
			String monitor = request.getParameter("monitor");
			jsonResult = CacheThreadData.getThreadDataJsonStr(ip);
			if (!StringUtils.isEmpty(monitor)) {
				ThreadDataJson object = (ThreadDataJson) JSONUtil.json2Object(jsonResult, ThreadDataJson.class);
				String[] split = monitor.split(",");
				if (object != null && object.getList() != null) {
					List<ThreadInf> list = new ArrayList<ThreadInf>();
					for (int i = 0; i < split.length; i++) {
						for (ThreadInf threadInf : object.getList()) {
							if (threadInf.getId() == Integer.parseInt(split[i])) { // 找到线程加入返回列表
								list.add(threadInf);
								break;
							}
						}
						if (list.size() <= i) { // 线程不存活，补空位
							list.add(new ThreadInf());
						}
					}
					object.setList(list);
					jsonResult = JSONUtil.write2JsonStr(object);
					System.out.println(jsonResult);
				}
			}
		} else if ("history".equals(key)) {
			String date = request.getParameter("date");
			Map map = new HashMap();
			map.put("eden", CacheVisualGC.getGcHistory(ip, date));
			map.put("survivor", CacheVisualGC.getSurvivorSpaceJsonStr(ip, date));
			map.put("old", CacheVisualGC.getOldGenSpaceJsonStr(ip, date));
			map.put("perm", CacheVisualGC.getPermGenSpaceJsonStr(ip, date));
			map.put("gcInfo", CacheVisualGC.getVisualGCJsonStr(ip));
			map.put("yonggc", CacheVisualGC.getYGCTHistory(ip, date));
			map.put("oldgc", CacheVisualGC.getOGCTHistory(ip, date));
			map.put("cpu", CacheVisualGC.getCpuUseHistory(ip, date));
			map.put("thread", CacheVisualGC.getTotalThreadHistory(ip, date));
			map.put("clazz", CacheVisualGC.getClazzHistory(ip, date));
			jsonResult = JSONUtil.write2JsonStr(map);
		}
		write(jsonResult, response);
		return null;
	}

	private String write(String res, HttpServletResponse response) {
		PrintWriter writer = null;
		try {
			if (res == null) {
				return "";
			}
			response.setContentType("text/html;charset=utf-8");
			writer = response.getWriter();
			writer.write(res);
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return "";
	}

}
