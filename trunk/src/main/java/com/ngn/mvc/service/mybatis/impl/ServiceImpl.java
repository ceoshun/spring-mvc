package com.ngn.mvc.service.mybatis.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ngn.mvc.dao.mybatis.Dao;
import com.ngn.mvc.exception.DaoException;
import com.ngn.mvc.model.Pagination;
import com.ngn.mvc.model.PersistentObject;
import com.ngn.mvc.service.mybatis.Service;

/**
 * Service基类，实现了数据的CRUD
 * 
 * @param <DAO>
 * @param <T>
 * @param <ID>
 * @author cs
 */
public abstract class ServiceImpl<DAO extends Dao<T, ID>, T extends PersistentObject, ID extends Serializable>
		implements Service<T, ID> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceImpl.class);

	/**
	 * 由子类注入实体DAO
	 */
	protected DAO dao;

	public abstract void setDao(DAO dao);

	/**
	 * 查询所有分页
	 * 
	 * @param p
	 * @return
	 */
	public Pagination<T> findByAllPagination(Pagination<T> p) {
		dao.findByAll(p);
		return p;
	}

	/**
	 * 通过主键查询实体
	 * 
	 * @param PK
	 *            pk
	 * @return T
	 */
	public T get(ID pk) {
		return dao.get(pk);
	}

	/**
	 * 通过主键集合查询实体
	 * 
	 * @param List
	 *            <PK> pks
	 * @return List<T>
	 */
	public List<T> get(Collection<ID> pks) {
		List<T> list = new ArrayList<T>(pks.size());
		for (ID pk : pks) {
			list.add(get(pk));
		}
		return list;
	}
	
	/**
	 * 根据主键查询实体
	 * 
	 * @param T
	 *            t
	 */
	public T selectByPrimaryKey(T t){
		return dao.selectByPrimaryKey(t);
	}
	
	/**
	 * 根据实例 分页查询
	 * 
	 * @param T
	 *  t
	 */
	public Pagination<T> selectByPagination(T t,Pagination<T> p){
		t = (T) t.escapeStr(t);
		dao.selectByPagination(t, p);
		return p;
	}

	/**
	 * 插入/更新实体
	 * 
	 * @param T
	 *            t
	 * 
	 */
	public void save(T t) {
		if (t.getId() == null) {
			dao.insert(t);
		}
		else {
			if(t.getId() == 1){
				dao.updateByPrimaryKey(t);
			}else{
				dao.update(t);
			}
		}
	}

	/**
	 * 插入/更新实体集合
	 * 
	 * @param List
	 *            <T> ts
	 */
	public void save(Collection<T> ts) {
		for (T t : ts) {
			save(t);
		}
	}
	
	/**
	 * 插入实体 （可选参数）
	 * 
	 * @param T
	 *            t
	 */
	public int insertSelective(T t){
		return dao.insertSelective(t);
	}

	/**
	 * 更新实体
	 * 
	 * @param T
	 *            t
	 */
	public void update(T t) {
		verifyRows(dao.update(t), 1, "数据库更新失败");
	}

	/**
	 * 更新实体集合
	 * 
	 * @param List
	 *            <T> ts
	 */
	public void update(Collection<T> ts) {
		for (T t : ts) {
			update(t);
		}
	}
	
	/**
	 * 根据主键更新数据（全部更新）
	 * 
	 * @param T
	 *            t
	 */
	public int updateByPrimaryKey(T t){
		return dao.updateByPrimaryKey(t);
	}
	
	/**
	 * 根据主键更新数据 （可选参数）
	 * 
	 * @param T
	 *            t
	 */
	public int updateByPrimaryKeySelective(T t){
		return dao.updateByPrimaryKeySelective(t);
	}

	/**
	 * 删除实体
	 * 
	 * @param T
	 *            t
	 */
	@SuppressWarnings("unchecked")
	public void delete(T t) {
		deleteById((ID) t.getId());
	}

	/**
	 * 删除实体集合
	 * 
	 * @param List
	 *            <T> ts
	 */
	public void delete(Collection<T> ts) {
		for (T t : ts) {
			delete(t);
		}
	}

	/**
	 * 通过主键删除实体
	 * 
	 * @param PK
	 *            pk
	 * @return T
	 */
	public void deleteById(ID id) {
		deleteById(Arrays.asList(id));
	}

	/**
	 * 通过主键集合删除实体 注：这里别把List改为Collection，会导致覆盖方法的List<ID>调用不到
	 * 
	 * @param List
	 *            <PK> pks
	 * @return List<T>
	 */
	public void deleteById(List<ID> idList) {
		verifyRows(dao.deleteById(idList), idList.size(), "数据库删除失败");
	}
	
	/**
	 * 根据主键删除实体
	 * 
	 * @param T
	 *            t
	 */
	public int deleteByPrimaryKey(T t){
		return dao.deleteByPrimaryKey(t);
	}
	
	/**
	 * 根据主键删除实体 
	 * 
	 * @param T
	 *            t
	 */
	public int deleteByIdSelective(T t,List<ID> idList){
		return dao.deleteByIdSelective(t,idList);
	}

	/**
	 * 为高并发环境出现的更新和删除操作，验证更新数据库记录条数
	 * 
	 * @param updateRows
	 * @param rows
	 * @param message
	 */
	protected void verifyRows(int updateRows, int rows, String message) {
		if (updateRows != rows) {
			DaoException e = new DaoException(message);
			LOGGER.error("need update is {}, but real update rows is {}.", rows, updateRows, e);
			throw e;
		}
	}
	
	/**
	 * 实体计数
	 * 
	 * @param T
	 *            t
	 * 
	 */
	public int count(T t) {
		return dao.count(t);
	}
	
	/**
	 * 通过项目ID集合删除实体 注：这里别把List改为Collection，会导致覆盖方法的List<ID>调用不到
	 * 
	 * @param List
	 *            <PK> pks
	 * @return 
	 * @return List<T>
	 */
	public int deleteByProjectId(List<ID> idList){
		return dao.deleteByProjectId(idList);
	}
}
