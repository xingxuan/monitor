package org.jvm.monitor;

import java.util.List;

import org.springframework.stereotype.Repository;

/**
 * 
 * @author jiangzhixiong
 * @data 2016年3月23日
 */
public interface UserJmxDao {

    public List<String> getJmxurlByAppName(String appName);
    
    /**
     * 根据监控主机名，查询所有被监控应用
     * @param serverName
     * @return
     */
    public List<UserJmx> quaryByServerName(String serverName);

    public int addJmx(UserJmx userJmx);

    public  void updateJmx(UserJmx userJmx);
    
    public  void updateAllJmx(UserJmx userJmx);

    public void updateJmxDisconnectState(String jmxUrl);
    
    public void updateJmxConnectedState(String jmxUrl);

    public String checkJmxUrlExists(String jmxUrl);
    
    public int deleteJmx(String jmxUrl);
    
    public List<UserJmx> queryUserJmxPage(UserJmx userJmx);
    
    public int queryUserJmxCount(UserJmx userJmx);

    public List<String> getAppNames();

    public List<String> getMonitorIps();
    
    public UserJmx queryByJmxUrl(String jmxUrl);

}
