package com.ngn.mvc.service.mybatis;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.ngn.mvc.model.Pagination;
import com.ngn.mvc.model.PersistentObject;

/**
 * Service接口
 * 
 * @param <DAO>
 * @param <T>
 * @param <ID>
 * @author cs extends PersistentObject
 */
public interface Service<T extends PersistentObject,ID extends Serializable> {

	/**
	 * 查询所有分页
	 * 
	 * @param p
	 * @return
	 */
	public Pagination<T> findByAllPagination(Pagination<T> p);
	
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
	public Pagination<T> selectByPagination(T t,Pagination<T> p);

	/**
	 * 通过主键查询实体
	 * 
	 * @param PK
	 *            pk
	 * @return T
	 */
	public T get(ID pk);

	/**
	 * 通过主键集合查询实体
	 * 
	 * @param List
	 *            <PK> pks
	 * @return List<T>
	 */
	public List<T> get(Collection<ID> pks);

	/**
	 * 插入/更新实体
	 * 
	 * @param T
	 *            t
	 */
	public void save(T t);

	/**
	 * 插入/更新实体集合
	 * 
	 * @param List
	 *            <T> ts
	 */
	public void save(Collection<T> ts);
	
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
	public void update(T t);

	/**
	 * 更新实体集合
	 * 
	 * @param List
	 *            <T> ts
	 */
	public void update(Collection<T> ts);
	
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
	 * 删除实体
	 * 
	 * @param T
	 *            t
	 */
	public void delete(T t);

	/**
	 * 删除实体集合
	 * 
	 * @param List
	 *            <T> ts
	 */
	public void delete(Collection<T> ts);

	/**
	 * 通过主键删除实体
	 * 
	 * @param PK
	 *            pk
	 * @return T
	 */
	public void deleteById(ID id);

	/**
	 * 通过主键集合删除实体 注：这里别把List改为Collection，会导致覆盖方法的List<ID>调用不到
	 * 
	 * @param List
	 *            <PK> pks
	 * @return List<T>
	 */
	public void deleteById(List<ID> idList);
	

	/**
	 * 根据主键删除实体
	 * 
	 * @param T
	 *            t
	 */
	public int deleteByPrimaryKey(T t);
	
	/**
	 * 根据主键删除实体 有筛选
	 * 
	 * @param T
	 *            t
	 */
	public int deleteByIdSelective(T t,List<ID> idList);
	
	/**
	 * 计数
	 * @param List
	 *            <PK> pks
	 * @return List<T>
	 */
	public int count(T t);
	
	/**
	 * 通过项目ID集合删除实体 注：这里别把List改为Collection，会导致覆盖方法的List<ID>调用不到
	 * 
	 * @param List
	 *            <PK> pks
	 * @return List<T>
	 */
	public int deleteByProjectId(List<ID> idList);
}
