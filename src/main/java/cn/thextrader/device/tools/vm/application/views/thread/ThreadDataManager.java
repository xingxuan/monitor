package cn.thextrader.device.tools.vm.application.views.thread;

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.*;

import cn.thextrader.device.json.JSONUtil;
import cn.thextrader.device.json.ThreadDataJson;
import cn.thextrader.device.tools.vm.cache.CacheThreadData;
import cn.thextrader.device.tools.vm.jvm.CommonConstants;

/**
 * JVM线程信息处理
 * @author jiangzhixiong
 *
 */
public class ThreadDataManager {
	private ThreadMXBean threadBean;
	private String connstr;
	
	
	public ThreadDataManager(ThreadMXBean threadBean,String connstr) {
		this.threadBean = threadBean;
		this.connstr = connstr;
	}
	
	public void fillThreadData(Map<Long, String[]> threadIdsMap){
		if(threadBean == null) return;
		ThreadDataJson tdj = new ThreadDataJson();
		Map<Long, String[]> currentThreadIdsMap = new HashMap<Long, String[]>();
		
		long[] currentThreadIds = threadBean.getAllThreadIds();//当前活动线程ID
		//通过线程ID获取当前活动线程信息
		ThreadInfo[] threadInfos = threadBean.getThreadInfo(currentThreadIds, 1);

		List<ThreadInf> list = new ArrayList<ThreadInf>();
		//循环当前的所有活动线程
		for (int i = 0; i < currentThreadIds.length; i++) {
            ThreadInfo tinfo = threadInfos[i];
            long threadId = currentThreadIds[i];//线程ID
            Long threadIdLong;

            if (tinfo == null) {
                continue;
            }
            threadIdLong = Long.valueOf(threadId);
            byte state =  getState(tinfo);
            if(state == 5 ) state = 3;
            if( threadIdsMap.get(threadIdLong) == null ){//为新的线程
            	String[] ts = {"0","0","0","0",tinfo.getThreadName(),String.valueOf(threadId),String.valueOf(state)};
            	currentThreadIdsMap.put(threadIdLong, ts);//添加到map中
            }else {//原来的活动线程
            	String[] ts = threadIdsMap.get(threadIdLong);//从map中获取
            	switch(state){
	            	case CommonConstants.THREAD_STATUS_RUNNING:
						long t0 = Long.parseLong(ts[0]) + 1;//运行时间
						ts[0] = String.valueOf(t0);
						break;
					case CommonConstants.THREAD_STATUS_SLEEPING:
						long t1 = Long.parseLong(ts[1]) + 1;//休眠时间
						ts[1] = String.valueOf(t1);
						break;
					case CommonConstants.THREAD_STATUS_WAIT:
						//等待时间
						ts[2] = String.valueOf(Long.parseLong(ts[2]) + 1);
						break;
					case CommonConstants.THREAD_STATUS_MONITOR:
						//监视时间
						ts[3] = String.valueOf(Long.parseLong(ts[3]) + 1);
						break;
					default:
						System.out.println(state + "thread state is ignored");
				}
				ts[6] = String.valueOf(state);
				currentThreadIdsMap.put(threadIdLong, ts);//添加到map中
			}
        }
		threadIdsMap.clear();
		threadIdsMap.putAll(currentThreadIdsMap);
		for (Iterator it = currentThreadIdsMap.values().iterator(); it.hasNext();) {
			String[] ts = (String[])it.next();
			ThreadInf map = new ThreadInf();
			map.setName(ts[4]);
			map.setId(Integer.parseInt(ts[5]));
			map.setS1(Integer.parseInt(ts[0]));
			map.setS2(Integer.parseInt(ts[1]));
			map.setS3(Integer.parseInt(ts[2]));
			map.setS4(Integer.parseInt(ts[3]));
			map.setState(Integer.parseInt(ts[6]));
			list.add(map);
		}
		Collections.sort(list);
		tdj.setList(list);
		String jsonStr = JSONUtil.write2JsonStr(tdj);
		//System.out.println(threadIdsMap.size()+" threadjson:"+jsonStr);
		CacheThreadData.saveThreadDataJsonStr(connstr, jsonStr);
	}
	
	/**
	 * 获取线程状态
	 * @param threadInfo
	 * @return
	 */
	byte getState(ThreadInfo threadInfo) {
        Thread.State state = threadInfo.getThreadState();
        switch (state) {
            case BLOCKED:
                return CommonConstants.THREAD_STATUS_MONITOR;
            case RUNNABLE:
                return CommonConstants.THREAD_STATUS_RUNNING;
            case TIMED_WAITING:
            case WAITING:
                StackTraceElement[] stack = threadInfo.getStackTrace();
                if (stack.length>0) {
                    StackTraceElement el = stack[0];
                    if (isSleeping(el)) return CommonConstants.THREAD_STATUS_SLEEPING;
                    if (isParked(el)) return CommonConstants.THREAD_STATUS_PARK;
                }
                return CommonConstants.THREAD_STATUS_WAIT;
            case TERMINATED:
            case NEW:
                return CommonConstants.THREAD_STATUS_ZOMBIE;
        }
        return CommonConstants.THREAD_STATUS_UNKNOWN;
    }

	/**
	 * 判断线程是否为
	 * @param element
	 * @return
	 */
    boolean isSleeping(StackTraceElement element) {
        return Thread.class.getName().equals(element.getClassName()) &&
                "sleep".equals(element.getMethodName());
    }

    boolean isParked(StackTraceElement element) {
        return "sun.misc.Unsafe".equals(element.getClassName()) &&
                "park".equals(element.getMethodName());
    }
    
    public void setThreadBean(ThreadMXBean threadBean) {
		this.threadBean = threadBean;
	}

}
