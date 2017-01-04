package org.jvm.device.tools.vm.core.model;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import org.jvm.device.tools.vm.core.datasource.DataSource;
import org.jvm.device.tools.vm.core.datasupport.ClassNameComparator;
import org.jvm.device.tools.vm.jmx.impl.JmxModelImpl;
import org.jvm.device.tools.vm.jmx.impl.JmxModelProvider;
import org.jvm.device.tools.vm.jvm.JVMImpl;
import org.jvm.device.tools.vm.jvm.JvmProvider;

/**
 * Model抽象工厂类,用于存放已经注册的{@link ModelProvider}实例 和 缓存创建的Model实例
 * @author jiangzhixiong
 *
 * @param <M> Model
 * @param <D> DataSource
 */
public abstract class ModelFactory<M extends Model,D extends DataSource> {
	final protected static Logger LOGGER = Logger.getLogger(ModelFactory.class.getName());
	private ReadWriteLock providersLock;
	private final Reference<M> NULL_MODEL;
	/** 存储ModelProvider的实现对象 如:{@link JvmProvider} */
	private SortedSet<ModelProvider<M, D>> providers;
	/** 弱引用的Model缓存 */
	private Map<DataSourceKey<D>,Reference<M>> modelCache;
	
	/**
	 * 构造函数
	 */
	protected ModelFactory() {
		NULL_MODEL = new SoftReference(null);
		providersLock = new ReentrantReadWriteLock();
		providers = new TreeSet(new ModelProviderComparator());
		modelCache = Collections.synchronizedMap(new HashMap());
	}
	
	/**
	 * 获取Model对象,当缓存中已经存在时，从缓存中取，没有则创建一个新对象并放到缓存
	 * ({@link JmxModelImpl},{@link JVMImpl})
	 * @param dataSource
	 * @return
	 */
	public final M getModel(D dataSource) {
		Lock rlock = providersLock.readLock();
		rlock.lock();
		try {
			synchronized (dataSource) {
				DataSourceKey<D> key = new DataSourceKey(dataSource);
				Reference<M> modelRef = modelCache.get(key);
				M model = null;
				
				if (modelRef != null) {
					if (modelRef == NULL_MODEL) {
                        return null;
                    }
					model = modelRef.get();
					if (model != null) {
	                    return model;
	                }
				}
				
				//
				for (ModelProvider<M, D> factory : providers) {
					model = factory.createModelFor(dataSource);
					if (model != null) {
						modelCache.put(key,new SoftReference(model));
						break;
					}
				}
				if (model == null) {
                    modelCache.put(key,NULL_MODEL);
                }
				return model;
			}
		} finally {
			rlock.unlock();
		}
	}
	
	/**
	 * 将{@link ModelProvider}注册到集合中的方法,实现如:{@link JvmProvider} , {@link JmxModelProvider}
	 * @param newProvider
	 * @return <CODE>true</CODE> 
	 */
	public final boolean registerProvider(ModelProvider<M, D> newProvider) {
		Lock wlock = providersLock.writeLock();
        wlock.lock();
        try {
            LOGGER.finer("Registering " + newProvider.getClass().getName());    // 
            boolean added = providers.add(newProvider);
            if (added) {
            	clearCache();
            }
            return added;
        } finally {
            wlock.unlock();
        }
	}
	
	/**
	 * @param oldProvider
	 * @return
	 */
	public final boolean unregisterProvider(ModelProvider<M, D> oldProvider) {

        Lock wlock = providersLock.writeLock();
        wlock.lock();
        try {
            LOGGER.finer("Unregistering " + oldProvider.getClass().getName());  //
            boolean removed = providers.remove(oldProvider);

            return removed;
         } finally {
            wlock.unlock();
         }
    }
	
	public int priority() {
        return -1;
    }
	
	private void clearCache() {
        modelCache.clear();
    }

	private class ModelProviderComparator implements Comparator<ModelProvider<M, D>> {

		public int compare(ModelProvider<M, D> provider1,
				ModelProvider<M, D> provider2) {
			int thisVal = provider1.priority();
			int anotherVal = provider2.priority();

			if (thisVal < anotherVal) {
				return 1;
			}
			if (thisVal > anotherVal) {
				return -1;
			}
			// same depth -> use class name to create artifical ordering
			return ClassNameComparator.INSTANCE.compare(provider1, provider2);
		}
	}
	
	/**
	 * @author jiangzhixiong
	 *
	 * @param <D>
	 */
	private static class DataSourceKey<D extends DataSource>  {
        Reference<D> weakReference;
        
        DataSourceKey(D ds) {
            weakReference = new WeakReference(ds);
        }
        
        public int hashCode() {
            D ds = weakReference.get();
            if (ds != null) {
                return ds.hashCode();
            }
            return 0;
        }
        
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj instanceof DataSourceKey) {
                D ds = weakReference.get();
                D otherDs = ((DataSourceKey<D>)obj).weakReference.get();
                
                return ds != null && ds == otherDs;
            }
            throw new IllegalArgumentException(obj.getClass().getName());
        }
        
        public String toString() {
            DataSource ds = weakReference.get();
			return "DataSourceKey for " + System.identityHashCode(this) + " for " + ds;
		}
	}
}
