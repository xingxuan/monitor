package cn.thextrader.device.tools.vm.core.datasource;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import cn.thextrader.device.tools.vm.jmx.impl.JmxApplication;
/**
 * 维护一个数据源容器
 * @author jiangzhixiong
 *
 */
public class DataSourceProvider {
	
	private final Set<DataSource> dataSources = Collections.synchronizedSet(new HashSet());
	
	DataSourceProvider(){}
	
	/**
	 * 返回被管理的DataSource
	 * @return
	 */
	public final Set<DataSource> getDataSources() {
		return new HashSet(Arrays.asList(dataSources.toArray()));
	}
	
	/**
	 * 从缓存中查询到此connstr的JmxApplication对象，没有则返回null
	 * @param connstr
	 * @return
	 */
	public final JmxApplication getDataSource(final String connstr) {
		JmxApplication jmxApplication = null;
		for(DataSource dataSource : dataSources){
			if(dataSource instanceof JmxApplication){
				jmxApplication = (JmxApplication)dataSource;
				if(jmxApplication.getConnstr().equals(connstr)){
					break;
				}else {
					jmxApplication = null;
				}
			}
		}
		return jmxApplication;
	}
	
	/**
	 * 添加单个
	 * @param added
	 */
	protected final void registerDataSource(DataSource added) {
        registerDataSources(Collections.singleton(added));
    }
	
	/**
	 * 添加多个
	 * @param added
	 */
	protected final void registerDataSources(final Set<? extends DataSource> added) {
		if (!added.isEmpty())
			registerDataSourcesImpl(checkAdded(added));
	}
	
	void registerDataSourcesImpl(Set<? extends DataSource> added) {
        dataSources.addAll(added);
        
    }
	
	protected final void unregisterDataSource(DataSource removed) {
        unregisterDataSources(Collections.singleton(removed));
    }
	
	protected final void unregisterDataSources(final Set<? extends DataSource> removed) {
		if (!removed.isEmpty())
			unregisterDataSourcesImpl(checkRemoved(removed));
	}
	
	void unregisterDataSourcesImpl(Set<? extends DataSource> removed) {
        dataSources.removeAll(removed);
        
    }
	
	private Set<? extends DataSource> checkAdded(Set<? extends DataSource> added) {
        Set<? extends DataSource> uniqueAdded = new HashSet(added);
        Iterator<? extends DataSource> it = uniqueAdded.iterator();

        while(it.hasNext()) {
            DataSource ds = it.next();
            if (dataSources.contains(ds)) {
                it.remove();
            }
        }
        return uniqueAdded;
    }
	
	private Set<? extends DataSource> checkRemoved(Set<? extends DataSource> removed) {
        Set<? extends DataSource> uniqueRemoved = new HashSet(removed);
        Iterator<? extends DataSource> it = uniqueRemoved.iterator();

        while(it.hasNext()) {
            DataSource ds = it.next();
            if (!dataSources.contains(ds)) {
                it.remove();
            }
        }

        return uniqueRemoved;
    }

}
