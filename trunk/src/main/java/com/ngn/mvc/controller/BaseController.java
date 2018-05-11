package com.ngn.mvc.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import javax.servlet.http.HttpServletRequest;

import com.ngn.mvc.util.StringUtils;

/**
 * Controller基类
 * 
 * @author cs
 */
public class BaseController {

	private Integer[] getAjaxIds(final String str, final String separator) {
		Integer[] ids = null;
		if (str != null) {
			String[] strs = str.split(separator);
			ids = new Integer[strs.length];
			for (int i = 0, length = strs.length; i < length; i++) {
				ids[i] = Integer.valueOf(strs[i]);
			}
		}
		return ids;
	}

	protected List<Integer> getAjaxIds(final String ids) {
		return StringUtils.isBlank(ids) ? new ArrayList<Integer>(0) : Arrays.asList(getAjaxIds(ids, ","));
	}
	
	private String[] getAjaxStrIds(final String str, final String separator) {
		String[] ids = null;
		if (str != null) {
			String[] strs = str.split(separator);
			ids = new String[strs.length];
			for (int i = 0, length = strs.length; i < length; i++) {
				ids[i] = String.valueOf(strs[i]);
			}
		}
		return ids;
	}

	protected List<String> getAjaxStrIds(final String ids) {
		return StringUtils.isBlank(ids) ? new ArrayList<String>(0) : Arrays.asList(getAjaxStrIds(ids, ","));
	}
	
	/**
	 * 获取IP地址
	 * @param request
	 * @return
	 */
	protected String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("X-Real-IP");
		if (!StringUtils.isBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
			return ip;
		}
		ip = request.getHeader("X-Forwarded-For");
		if (!StringUtils.isBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
			// 多次反向代理后会有多个IP值，第一个为真实IP。
			int index = ip.indexOf(',');
			if (index != -1) {
				return ip.substring(0, index);
			}
			else {
				return ip;
			}
		}
		else {
			return request.getRemoteAddr();
		}
	}
	
	protected String encode(String str) {
		Encoder encoder = Base64.getUrlEncoder();
		return new String(encoder.encode(str.getBytes()));
	}

	protected String decode(String str) {
		Decoder decoder = Base64.getUrlDecoder();
		return new String(decoder.decode(str.getBytes()));
	}
}