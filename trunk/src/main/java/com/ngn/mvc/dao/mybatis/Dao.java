package com.ngn.mvc.dao.mybatis;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.ngn.mvc.model.Pagination;

/**
 * Dao接口
 * 
 * @author cs
 */
public interface Dao<T, ID extends Serializable> {

	/**
	 * 查询所有分页
	 * 
	 * @param p
	 * @return
	 */
	public List<T> findByAll(Pagination<T> p);
	
	/**
	 * 根据主键查询实体
	 * 
	 * @param T
	 *            t
	 */
	public T selectByPrimaryKey(T t);
	
	/**
	 * 根据实例 分页查询
	 * 
	 * @param T
	 *            t
	 */
	public List<T> selectByPagination(@Param("record") T t,Pagination<T> p);
	
	/**
	 * 通过主键查询实体
	 * 
	 * @param PK
	 *            pk
	 * @return T
	 */
	public T get(ID pk);

	/**
	 * 插入实体
	 * 
	 * @param T
	 *            t
	 */
	public int insert(T t);
	
	/**
	 * 插入实体 （可选参数）
	 * 
	 * @param T
	 *            t
	 */
	public int insertSelective(T t);

	/**
	 * 更新实体
	 * 
	 * @param T
	 *            t
	 */
	public int update(T t);
	
	/**
	 * 实体计数
	 * 
	 * @param T
	 *            t
	 */
	public int count(T t);
	
	/**
	 * 删除实体
	 * 
	 * @param T
	 *            t
	 */
	public int deleteById(Collection<ID> idList);
	
	/**
	 * 通过ID 删除实体 带有筛选
	 * 
	 * @param T
	 *            t
	 */
	public int deleteByIdSelective(@Param("record") T t,@Param("idList") Collection<ID> idList);
	
	/**
	 * 根据主键删除实体
	 * 
	 * @param T
	 *            t
	 */
	public int deleteByPrimaryKey(T t);

	/**
	 * 根据主键更新数据（全部更新）
	 * 
	 * @param T
	 *            t
	 */
	public int updateByPrimaryKey(T t);
	/**
	 * 根据主键更新数据 （可选参数）
	 * 
	 * @param T
	 *            t
	 */
	public int updateByPrimaryKeySelective(T t);

	/**
	 * 根据项目ID删除数据
	 * 
	 * @param T
	 *            t
	 */
	public int deleteByProjectId(Collection<ID> idList);
}
