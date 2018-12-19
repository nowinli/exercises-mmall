package com.mmall.common;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

//本地缓存
public class TokenCache {
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(TokenCache.class);

	public static final String TOKEN_PREFIX = "token_";

	//构建本地缓存，调用链的方式 ,1000是设置缓存的初始化容量，maximumSize是设置缓存最大容量，
	// 当超过了最大容量，guava将使用LRU算法（最少使用算法），来移除缓存项
	//expireAfterAccess(12,TimeUnit.HOURS)设置缓存有效期为12个小时
	private static LoadingCache<String, String> localCache = CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS)
			.build(new CacheLoader<String, String>() {
				//默认打得数据加载实现，当调用get取值的时候，就调用这个方法加载
				@Override
				public String load(String s) throws Exception {
					return "null";
				}
			});
	//添加本队缓存
	public static  void setKey(String key,String value){
		localCache.put(key,value);
	}
	//得到本地缓存
	public static String getKey(String key){
		String value = null;
		try {
			value = localCache.get(key);
			if ("null".equals(value)){
				return null;
			}
			return value;
		}catch (Exception e){
			logger.error("locakCache get error",e);
		}
		return null;
	}
}
