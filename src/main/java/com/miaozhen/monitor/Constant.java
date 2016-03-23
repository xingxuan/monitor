package com.miaozhen.monitor;

import org.springframework.beans.factory.annotation.Value;

import java.security.MessageDigest;
import java.util.logging.Logger;

/**
 * 常量类.
 */
public final class Constant {
	private static final Logger LOGGER = Logger.getLogger(Constant.class.getName());
	/**
	 * 默认字符集.
	 */
	public static final String CHARSET_DEFAULT = "utf-8";
	/**
	 * 默认erp.
	 */
	public static final String ERP = "jiangzhixiong";

	/**
	 * 所有redis缓存前缀
	 */
	public static String PREFIX = "jvm_";
	public static int CACHESTORETIME = 10 * 24 * 60 * 60;// 10天的seconds
	/**
	 * UMP监控的应用名.
	 */
	public static final String APP_NAME = "device";

	public static String TOKEN = "50e949d98c804e2ba2d5e7f141ad20ee";

	/**
	 * 禁止构造.
	 */
	private Constant() {
	}

	public void validate() {
		LOGGER.info("cache.data.prefix:" + PREFIX);
		LOGGER.info("cache.data.storetime:" + CACHESTORETIME);
		LOGGER.info("token:" + TOKEN);
	}

	/**
	 * 方法返回状态码
	 * 
	 * @author jiangzhixiong
	 *
	 */
	public static class ReturnState {
		public static final int SUCCESS = 0;
		public static final int EXCEPTION = -1;
	}

	/**
	 * 每一个erp和ip端口对应一个token验证
	 *
	 * @param args
	 * @return
	 */
	public static String token(String... args) {
		StringBuilder token = new StringBuilder(TOKEN);
		for (int i = 0; i < args.length; i++) {
			token.append(args[i]);
		}
		return md5(token.toString());
	}

	public static final String md5(String str) {
		String newstr = "";
		try {
			// 确定计算方法
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			// 加密后的字符串
			newstr = HexUtil.encode((md5.digest(str.getBytes(Constant.CHARSET_DEFAULT))));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newstr;
	}

	public String getTOKEN() {
		return TOKEN;
	}

	public void setTOKEN(String tOKEN) {
		TOKEN = tOKEN;
	}

	public void setCACHESTORETIME(int cACHESTORETIME) {
		CACHESTORETIME = cACHESTORETIME;
	}

	public int getCACHESTORETIME() {
		return CACHESTORETIME;
	}

	public String getPREFIX() {
		return PREFIX;
	}

	public void setPREFIX(String pREFIX) {
		PREFIX = pREFIX;
	}

	public static void main(String[] args) {
		System.out.println(token("jiangzhixiong", "127.0.0.1:1099"));
	}

}
