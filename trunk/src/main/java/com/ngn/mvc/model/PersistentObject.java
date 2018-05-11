package com.ngn.mvc.model;

import java.io.Serializable;
import java.lang.reflect.Field;


/**
 * 持久化基类
 * 
 * @author cs
 */
public class PersistentObject implements Serializable{

	private static final long serialVersionUID = 1472145516693079043L;
	
	/** 主键ID */
	private Integer id;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * 覆盖原equals方法，只要对象类型相同并且主键相同，那么返回true，表示两个对象相等
	 * 
	 * @param Object
	 *            o
	 * @return boolean
	 */
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || !(o instanceof PersistentObject)) {
			return false;
		}
		PersistentObject other = (PersistentObject) o;
		return id.equals(other.getId());
	}

	/**
	 * 覆盖原hashCode方法，用主键的hashcode代替原来对象的hashcode，可以简化持久化对象的比较
	 * 
	 * @return int
	 */
	public int hashCode() {
		if (id == null) {
			return Integer.valueOf(0);
		}
		return id.hashCode();
	}

	/**
	 * 覆盖原toString方法，打印更详细信息
	 * 
	 * @return String
	 */
	public String toString() {
		return this.getClass().getName() + "[id=" + id + "]";
	}
	
	/**
	 * string数组拆分
	 * 
	 * @return String
	 */
	public String[] getAjaxStrIds(final String str, final String separator) {
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
	
	/**
	 * 处理转义字符%和_,针对ORACLE数据库
	 * @param str
	 * @return
	 */
	public static String _escapeStr(String str){
		String temp = "";
		for(int i=0;i<str.length();i++){
			if(str.charAt(i) == '%' || str.charAt(i) == '_'){
				temp += "\\" + str.charAt(i);
			}else{
				temp += str.charAt(i);
			}
		}
		return temp;
	}
	
	/**
	 * 处理转义字符%和_,针对ORACLE数据库
	 * @param str
	 * @return
	 */
	public Object escapeStr(Object obj){
		Field[] fields = obj.getClass().getDeclaredFields();
		for(int i = 0,len = fields.length;i<len;i++){
			String varName = fields[i].getName();
			try{
				boolean accessFlag = fields[i].isAccessible();
				fields[i].setAccessible(true);
				
				String type = fields[i].getGenericType().toString();
				if(type.equals("class java.lang.String")){
					String _f = fields[i].get(obj).toString();
					_f = _escapeStr(_f);
					fields[i].set(varName, _f);
				}
				
				fields[i].setAccessible(accessFlag);
			} catch (Exception e){
			}
		}
		return obj;
	}
}