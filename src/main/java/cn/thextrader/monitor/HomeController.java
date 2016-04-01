package cn.thextrader.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import cn.thextrader.device.json.JSONUtil;
import cn.thextrader.device.json.ThreadDataJson;
import cn.thextrader.device.tools.vm.application.views.thread.ThreadInf;
import cn.thextrader.device.tools.vm.cache.CacheThreadData;
import cn.thextrader.device.tools.vm.cache.CacheVisualGC;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	@Resource
	private UserJmxDao userJmxDao;
	@RequestMapping(value = "/")
	public ModelAndView home(@ModelAttribute("str") String str) {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("home");
		List<String> appNames = userJmxDao.getAppNames();
		List<Map<String, Object>> rtnList = new ArrayList<Map<String, Object>>();
		if (CollectionUtils.isEmpty(appNames)) {
            Map<String, Object> map3 = new HashMap<String, Object>();
            map3.put("name", "");
			map3.put("list", new ArrayList<Map<String, String>>());
			rtnList.add(map3);
			modelAndView.addObject("listMapList", rtnList);
			return modelAndView;
		}

		for (String appName : appNames) {
            Map<String, Object> map3 = new HashMap<String, Object>();
			map3.put("name", appName);
			List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();
			UserJmx param = new UserJmx();
			param.setApplicationName(appName);
			List<UserJmx> jmxs = userJmxDao.queryUserJmxPage(param);
			for (UserJmx jmx : jmxs) {
				Map<String,String> map = new HashMap<String,String>();
				map.put("ip", jmx.getJmxUrl());
				map.put("to", jmx.getServerName());
				mapList.add(map);
			}
			map3.put("list", mapList);
            rtnList.add(map3);
		}
		modelAndView.addObject("listMapList", rtnList);
		return modelAndView;
	}

	@RequestMapping(value = "/view/visualGc")
	public ModelAndView dynamic(@ModelAttribute("ip") String ip, @ModelAttribute("to") String to) {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("visualGc");
		modelAndView.addObject("shortHistory", CacheVisualGC.getShortHistory(ip));
		return modelAndView;
	}

	@RequestMapping(value = "/view/overview")
	public ModelAndView overview(@ModelAttribute("ip") String ip, @ModelAttribute("to") String to) {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("overview");
		return modelAndView;
	}

	@RequestMapping(value = "/view/right")
	public ModelAndView right(@ModelAttribute("ip") String ip, @ModelAttribute("to") String to) {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("right");
		return modelAndView;
	}

	@RequestMapping(value = "/view/monitor")
	public ModelAndView monitor(@ModelAttribute("ip") String ip, @ModelAttribute("to") String to) {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("monitor");
		modelAndView.addObject("shortHistory", CacheVisualGC.getShortHistory(ip));
		return modelAndView;
	}

	@RequestMapping(value = "/view/thread")
	public ModelAndView thread(@ModelAttribute("ip") String ip, @ModelAttribute("to") String to) {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("thread");
		String jsonStr = CacheThreadData.getThreadDataJsonStr(ip);
		if (jsonStr != null) {
			ThreadDataJson object = (ThreadDataJson) JSONUtil.json2Object(jsonStr, ThreadDataJson.class);
			modelAndView.addObject("threadList", object.getList());
			modelAndView.addObject("threadJson", jsonStr);
		} else {
			modelAndView.addObject("threadList", new ArrayList<ThreadInf>(0));
		}
		return modelAndView;
	}

	@RequestMapping(value = "/view/history")
	public ModelAndView history(@ModelAttribute("ip") String ip, @ModelAttribute("to") String to) {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("history");
		return modelAndView;
	}
}
