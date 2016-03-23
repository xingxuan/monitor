package com.miaozhen.device.tools.vm.cache;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Repository;

import com.miaozhen.monitor.Constant;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
/**
 * 缓存的工具类,所有缓存key前缀都应该加上CacheSupport#PREFIX,以减少在redis中key冲突
 * @author jiangzhixiong
 *
 */
@Repository
public class CacheSupport {
	/**
	 * 项目中所有缓存数据的key的前缀
	 */
	private static final String PREFIX = Constant.PREFIX;
	private static final CacheSupport INSTANCE = new CacheSupport();
    @Resource
    private JedisPool pool;


    /**
     * 返回CacheSupport实例
     *
     * @return
     */
    public static CacheSupport getInstance(){
		return INSTANCE;
	}

	/**
	 * 获取本应用的机器名
	 * @return
	 */
	public static String getLocalName(){
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return "unknow";
		}
	}
	/**
	 * 获取本应用机器IP
	 * @return
	 */
	public static String getLocalIp() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            String ip = addr.getHostAddress(); //获得本机IP
            return ip;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "";
    }
	
	/**
	 * 从缓存获取多个key的值
	 * @param keys 这里的key应该传入完整的key字符串
	 * @return
	 */
	List<String> getValues(String keys[]) {
        Jedis jedis = null;
        List<Response<String>> responses = new ArrayList<Response<String>>();
        List<String> values = new ArrayList<String>();
        try {
            jedis = pool.getResource();
            Pipeline pipeline = jedis.pipelined();
            for (String key : keys) {
                Response<String> resp = pipeline.get(key);
                responses.add(resp);
            }
            pipeline.sync();
            for (Response<String> value : responses) {
                values.add(value.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return values;
    }
	
	/**
	 * 保存内容到redis
	 * @param key 不允许为null或空字符
	 * @param value
	 */
	void set(String key, String value) {
		if(key==null ||key.length()==0) return;
		String mkey = PREFIX+key;
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.set(mkey, value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
	/**
	 * 
	 * @param key 不允许为null或空字符
	 * @param value
	 * @return
	 */
	long append(String key, String value) {
		if(key==null ||key.length()==0) return 0;
		String mkey = PREFIX+key;
        Jedis jedis = null;
        long r = 0;
        try {
            jedis = pool.getResource();
            r = jedis.append(mkey, value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return r;
    }

	/**
	 * 设置缓存有效期
	 * @param key 不允许为null或空字符
	 * @param seconds
	 * @param value
	 */
	void setex(String key,int seconds,String value){
		if(key==null ||key.length()==0) return;
		String mkey = PREFIX+key;
		Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.setex(mkey, seconds, value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
	}
	
	/**
	 * 返回一个key的value
	 * @param key 不允许为null或空字符
	 * @return
	 */
	String get(String key){
		Jedis jedis = null;
		String value = null;
		if(key==null ||key.length()==0) return value;
		String mkey = PREFIX+key;
        try {
            jedis = pool.getResource();
            value = jedis.get(mkey);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return value;
	}
	
	/**
	 * 若 key 存在返回 1 , 否则返回 0
	 * @param key
	 * @return
	 */
	boolean exists(String key){
		Jedis jedis = null;
		boolean value = false;
		if(key==null ||key.length()==0) return value;
		String mkey = PREFIX+key;
        try {
            jedis = pool.getResource();
            value = jedis.exists(mkey);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return value;
	}
	
	/**
	 * 给key设置过期日间
	 * @param key
	 * @param seconds
	 * @return
	 */
	Long expire(String key,int seconds){
		Jedis jedis = null;
		long r = 0;
		if(key==null ||key.length()==0) return r;
		String mkey = PREFIX+key;
        try {
            jedis = pool.getResource();
            r =jedis.expire(mkey, seconds);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return r;
	}
	/**
	 * 删除一个key
	 * @param key
	 * @return
	 */
	Long delKey(String key){
		Jedis jedis = null;
		long r = 0;
		if(key==null ||key.length()==0) return r;
		String mkey = PREFIX+key;
        try {
            jedis = pool.getResource();
            r =jedis.del(mkey);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return r;
	}
	
	//----------------------------set type
	
	/**
	 * 添加一个多个元素到set集合中
	 * @param key
	 * @param members
	 * @return
	 */
	Long sadd(String key,String ...members){
		Jedis jedis = null;
		long r = 0;
		if(key==null ||key.length()==0) return r;
		String mkey = PREFIX+key;
        try {
            jedis = pool.getResource();
            r =jedis.sadd(mkey, members);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return r;
	}
	
	/**
	 * 删除一个或多个set元素
	 * @param key
	 * @param members
	 * @return
	 */
	Long srem(String key,String ...members){
		Jedis jedis = null;
		long r = 0;
		if(key==null ||key.length()==0) return r;
		String mkey = PREFIX+key;
        try {
            jedis = pool.getResource();
            r =jedis.srem(mkey, members);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return r;
	}
	/**
	 * 检查member是否在key的集合中
	 * @param key
	 * @param member
	 * @return
	 */
	boolean sismember(String key,String member){
		Jedis jedis = null;
		boolean bln = false;
		if(key==null ||key.length()==0) return bln;
		String mkey = PREFIX+key;
        try {
            jedis = pool.getResource();
            bln = jedis.sismember(mkey, member);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return bln;
	}
	/**
	 * 返回所有set元素
	 * @param key
	 * @return
	 */
	Set<String> smembers(String key){
		Jedis jedis = null;
		Set<String> sets = null;
		if(key==null ||key.length()==0) return sets;
		String mkey = PREFIX+key;
        try {
            jedis = pool.getResource();
            sets = jedis.smembers(mkey);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return sets;
	}
	
	//----SortedSet 有序集合
	/**
	 * 添加一个元素到有序集合中
	 */
	Long zadd(String key,double score,String member){
		Jedis jedis = null;
		long r = 0;
		if(key==null ||key.length()==0) return r;
		String mkey = PREFIX+key;
        try {
            jedis = pool.getResource();
            r = jedis.zadd(mkey, score, member);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return r;
	}
	/**
	 * 返回集合中元素的总数
	 * @param key
	 * @return
	 */
	Long zcard(String key){
		Jedis jedis = null;
		long r = 0;
		if(key==null ||key.length()==0) return r;
		String mkey = PREFIX+key;
        try {
            jedis = pool.getResource();
            r = jedis.zcard(mkey);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return r;
	}
	/**
	 * 查询出指定范围中的元素
	 * @param key
	 * @param start 从0开始为第一个元素
	 * @param end
	 * @return
	 */
	Set<String> zrange(String key,long start,long end){
		Jedis jedis = null;
		Set<String> sets = null;
		if(key==null ||key.length()==0) return sets;
		String mkey = PREFIX+key;
        try {
            jedis = pool.getResource();
            sets = jedis.zrange(mkey, start, end);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return sets;
	}
	/**
	 * 从有序集合中删除一个元素
	 * @param key
	 * @param member
	 * @return
	 */
	Long zrem(String key,String member){
		Jedis jedis = null;
		long r = 0;
		if(key==null ||key.length()==0) return r;
		String mkey = PREFIX+key;
        try {
            jedis = pool.getResource();
            r = jedis.zrem(mkey, member);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return r;
	}
	
	//---list
	/**
	 * 在表头添加元素
	 */
	Long lpush(String key,String ...members){
		Jedis jedis = null;
		long r = 0;
		if(key==null ||key.length()==0) return r;
		String mkey = PREFIX+key;
        try {
            jedis = pool.getResource();
            r =jedis.lpush(mkey, members);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return r;
	}
	
	/**
	 * 弹出表头的一个元素
	 * @param key
	 * @return
	 */
	String lpop(String key){
		Jedis jedis = null;
		String r = null;
		if(key==null ||key.length()==0) return r;
		String mkey = PREFIX+key;
        try {
            jedis = pool.getResource();
            r = jedis.lpop(mkey);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return r;
	}
	/**
	 * 获取list中的元素
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	List<String> lrange(String key,long start,long end){
		Jedis jedis = null;
		List<String> r = null;
		if(key==null ||key.length()==0) return r;
		String mkey = PREFIX+key;
        try {
            jedis = pool.getResource();
            r = jedis.lrange(mkey,start,end);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return r;
	}
	/**
	 * 在表尾添加元素
	 * @param key
	 * @param members
	 * @return
	 */
	Long rpush(String key,String ...members){
		Jedis jedis = null;
		long r = 0;
		if(key==null ||key.length()==0) return r;
		String mkey = PREFIX+key;
        try {
            jedis = pool.getResource();
            r =jedis.rpush(mkey, members);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return r;
	}
	
	Long llen(String key){
		Jedis jedis = null;
		long r = 0;
		if(key==null ||key.length()==0) return r;
		String mkey = PREFIX+key;
        try {
            jedis = pool.getResource();
            r =jedis.llen(mkey);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return r;
    }
	
	//--------- hash operation
	/**
	 * 把一个field,value添加到key的hash中
	 */
	Long hset(String key,String field,String value){
		Jedis jedis = null;
		long r = 0;
		if(key==null ||key.length()==0) return r;
		String mkey = PREFIX+key;
        try {
            jedis = pool.getResource();
            r = jedis.hset(mkey, field, value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return r;
    }
	/**
	 * 删除一项
	 * @param key
	 * @param field
	 * @return
	 */
	Long hdel(String key,String field){
		Jedis jedis = null;
		long r = 0;
		if(key==null ||key.length()==0) return r;
		String mkey = PREFIX+key;
        try {
            jedis = pool.getResource();
            r = jedis.hdel(mkey, field);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return r;
    }
	
	/**
	 * 在hash中获取field的值
	 * @param key
	 * @param field
	 * @return
	 */
	String hget(String key,String field){
		Jedis jedis = null;
		String result = null;
		if(key==null ||key.length()==0) return result;
		String mkey = PREFIX+key;
        try {
            jedis = pool.getResource();
            result = jedis.hget(mkey, field);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return result;
    }
	/**
	 * 返回多个key的值
	 * @param key
	 * @param fields
	 * @return
	 */
	List<String> hmget(String key,String... fields){
		Jedis jedis = null;
		List<String> result = null;
		if(key==null ||key.length()==0) return result;
		String mkey = PREFIX+key;
        try {
            jedis = pool.getResource();
            result = jedis.hmget(mkey, fields);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return result;
    }
}
