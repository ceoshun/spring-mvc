package com.ngn.mvc.interceptor.mybatis;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ngn.mvc.model.PageParams;
import com.ngn.mvc.model.Pagination;

/**
 *
 * @author ykzhen2015
 */
@Intercepts({ @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class }),
	@Signature(type = ResultSetHandler.class, method = "handleResultSets", args = { Statement.class })})
public class PagingPlugin implements Interceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(PagingPlugin.class);
	private final ThreadLocal<Pagination<?>> localPagination = new ThreadLocal<Pagination<?>>();

	
	
	/**
	 * 插件默认参数，可配置
	 */
    private Integer defaultPage; //默认页码
    private Integer defaultPageSize;//默认每页条数
    private Boolean defaultUseFlag; //默认是否启用插件
    private Boolean defaultCheckFlag; //默认是否检测页码参数
    private Boolean defaultCleanOrderBy; //默认是否清除最后一个order by 后的语句

    private static final String DB_TYPE_MYSQL = "mysql";
    private static final String DB_TYPE_ORACLE = "oracle";

    /**
     * 插件实现方法
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
		if (invocation.getTarget() instanceof StatementHandler) {
			StatementHandler stmtHandler = (StatementHandler) getUnProxyObject(invocation.getTarget());

			MetaObject metaStatementHandler = SystemMetaObject.forObject(stmtHandler);
			String sql = (String) metaStatementHandler.getValue("delegate.boundSql.sql");

			MappedStatement mappedStatement = (MappedStatement) metaStatementHandler
					.getValue("delegate.mappedStatement");
			String dbType = this.getDataSourceType(mappedStatement);

			// 不是select语句.
			if (!this.checkSelect(sql)) {
				return invocation.proceed();
			}
			BoundSql boundSql = (BoundSql) metaStatementHandler.getValue("delegate.boundSql");

			Pagination<?> pagination = findPaginationParameter(stmtHandler.getBoundSql().getParameterObject());
			if (pagination == null)
				return invocation.proceed();
			else {
				localPagination.set(pagination);
			}

			// 获取相关配置的参数.
			Integer pageNum = pagination.getPageNo();
			Integer pageSize = pagination.getPageSize();
			Boolean checkFlag = false;
			Boolean cleanOrderBy = false;
			int total = this.getTotal(invocation, metaStatementHandler, boundSql, cleanOrderBy, dbType);

			pagination.setRowCount(total);
			// 计算总页数.
			int totalPage = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
			// 检查当前页码的有效性.
			this.checkPage(checkFlag, pageNum, totalPage);
			// 修改sql
			return this.preparedSQL(invocation, metaStatementHandler, boundSql, pageNum, pageSize, dbType);
		} else if (invocation.getTarget() instanceof ResultSetHandler) {
			Pagination<?> pagination = localPagination.get();
			if (pagination == null)
				return invocation.proceed();
			try {
				Object result = invocation.proceed();
				pagination.setList((List) result);
				return result;
			} finally {
				localPagination.remove();
			}
		}
		return null;
    }


    /***
     * 分离出分页参数.
     * @param parameterObject --执行参数
     * @return 分页参数
     * @throws Exception
     */
    public PageParams getPageParamsForParamObj(Object parameterObject) throws Exception {
    	PageParams pageParams = null;
    	if (parameterObject == null) {
    		return null;
    	}
    	//处理map参数和@Param注解参数，都是MAP
    	if (parameterObject instanceof Map) {
    		@SuppressWarnings("unchecked")
			Map<String, Object> paramMap = (Map<String, Object>) parameterObject;
    		Set<String> keySet = paramMap.keySet();
    		Iterator<String> iterator = keySet.iterator();
    		while(iterator.hasNext()) {
    			String key = iterator.next();
    			Object value = paramMap.get(key);
    			if (value instanceof PageParams) {
    				return (PageParams)value;
    			}
    		}
        } else if (parameterObject instanceof PageParams) { //参数POJO继承了PageParams
        	return (PageParams) parameterObject;
        } else { //从POJO尝试读取分页参数.
        	Field[] fields = parameterObject.getClass().getDeclaredFields();
        	//尝试从POJO中获得类型为PageParams的属性
        	for (Field field : fields) {
        		if (field.getType() == PageParams.class) {
        			PropertyDescriptor pd = new PropertyDescriptor (field.getName(), parameterObject.getClass());
        			Method method = pd.getReadMethod();
        			return (PageParams) method.invoke(parameterObject);
        		}
        	}
        }
        return pageParams;
    }
    
    private Pagination<?> findPaginationParameter(Object param) {
		if (param instanceof Pagination<?>) {
			return (Pagination<?>) param;
		}
		else if (param instanceof Map) {
			for (Object val : ((Map<?, ?>) param).values()) {
				if (val instanceof Pagination<?>) {
					return (Pagination<?>) val;
				}
			}
		}
		return null;
	}


    /**
     * 判断是否sql语句.
     * @param sql
     * @return
     */
    private boolean checkSelect(String sql) {
        String trimSql = sql.trim();
        int idx = trimSql.toLowerCase().indexOf("select");
        return idx == 0;
    }

    /**
     * 检查当前页码的有效性.
     * @param checkFlag
     * @param pageNum
     * @param pageTotal
     * @throws Throwable
     */
    private void checkPage(Boolean checkFlag, Integer pageNum, Integer pageTotal) throws Throwable  {
        if (checkFlag) {
            //检查页码page是否合法.
            if (pageNum > pageTotal) {
                throw new Exception("查询失败，查询页码【" + pageNum + "】大于总总页【" + pageTotal + "】！！");
            }
        }
    }


    /**
     * 预编译改写后的SQL，并设置分页参数
     * @param invocation
     * @param metaStatementHandler
     * @param boundSql
     * @param pageNum
     * @param pageSize
     * @param dbType
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private Object preparedSQL(Invocation invocation, MetaObject metaStatementHandler, BoundSql boundSql, int pageNum, int pageSize, String dbType) throws Exception {
        //获取当前需要执行的SQL
        String sql = boundSql.getSql();
        String newSql = this.getPageDataSQL(sql, dbType);
        //修改当前需要执行的SQL
        metaStatementHandler.setValue("delegate.boundSql.sql", newSql);
        //执行编译，这里相当于StatementHandler执行了prepared()方法，这个时候，就剩下2个分页参数没有设置。
        Object statementObj = invocation.proceed();
        //设置两个分页参数。
        this.preparePageDataParams((PreparedStatement)statementObj, pageNum, pageSize, dbType);
        return statementObj;
    }



    /**
     * 获取综述.
     *
     * @param ivt Invocation
     * @param metaStatementHandler statementHandler
     * @param boundSql sql
     * @param cleanOrderBy 是否清除order by语句
     * * @param dbType
     * @return sql查询总数.
     * @throws Throwable 异常.
     */
    private int getTotal(Invocation ivt, MetaObject metaStatementHandler, BoundSql boundSql, Boolean cleanOrderBy, String dbType) throws Throwable {
        //获取当前的mappedStatement
        MappedStatement mappedStatement = (MappedStatement) metaStatementHandler.getValue("delegate.mappedStatement");
        //配置对象
        Configuration cfg = mappedStatement.getConfiguration();
        //当前需要执行的SQL
        String sql = (String) metaStatementHandler.getValue("delegate.boundSql.sql");
        //去掉最后的order by语句
        if (cleanOrderBy) {
            sql = this.cleanOrderByForSql(sql);
        }
        String countSql = this.getTotalSQL(sql, dbType);
        //获取拦截方法参数，根据插件签名，知道是Connection对象.
        Connection connection = (Connection) ivt.getArgs()[0];
        PreparedStatement ps = null;
        int total = 0;
        try {
            //预编译统计总数SQL
            ps = connection.prepareStatement(countSql);
            //构建统计总数SQL
            BoundSql countBoundSql = new BoundSql(cfg, countSql, boundSql.getParameterMappings(), boundSql.getParameterObject());
            //构建MyBatis的ParameterHandler用来设置总数Sql的参数。
            ParameterHandler handler = new DefaultParameterHandler(mappedStatement, boundSql.getParameterObject(), countBoundSql);
            //设置总数SQL参数
            handler.setParameters(ps);
            //执行查询.
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                total = rs.getInt("total");
            }
        } finally {
            //这里不能关闭Connection否则后续的SQL就没法继续了。
            if (ps != null && ps.isClosed()) {
                ps.close();
            }
        }
        return total;
    }

    private String cleanOrderByForSql(String sql) {
    	StringBuilder sb = new StringBuilder(sql);
    	String newSql = sql.toLowerCase();
    	//如果没有order语句,直接返回
    	if (newSql.indexOf("order") == -1) {
    		return sql;
    	}
    	int idx = newSql.lastIndexOf("order");
    	return sb.substring(0, idx).toString();
    }

    /**
     * 从代理对象中分离出真实对象.
     *
     * @param ivt --Invocation
     * @return 非代理StatementHandler对象
     */
    private Object getUnProxyObject(Object target) {
        MetaObject metaStatementHandler = SystemMetaObject.forObject(target);
        // 分离代理对象链(由于目标类可能被多个拦截器拦截，从而形成多次代理，通过循环可以分离出最原始的的目标类)
        Object object = null;
        while (metaStatementHandler.hasGetter("h")) {
            object = metaStatementHandler.getValue("h");
        }
        if (object == null) {
            return target;
        }
        return object;
    }

    /**
     * 生成代理对象
     * @param statementHandler 原始对象
     * @return 代理对象
     */
    @Override
    public Object plugin(Object statementHandler) {
        return Plugin.wrap(statementHandler, this);
    }

    /**
     * 设置插件配置参数。
     * @param props
     */
    @Override
    public void setProperties(Properties props) {
        String strDefaultPage = props.getProperty("default.page", "1");
        String strDefaultPageSize = props.getProperty("default.pageSize", "50");
        String strDefaultUseFlag = props.getProperty("default.useFlag", "false");
        String strDefaultCheckFlag = props.getProperty("default.checkFlag", "false");
        String StringDefaultCleanOrderBy = props.getProperty("default.cleanOrderBy", "false");

        this.defaultPage = Integer.parseInt(strDefaultPage);
        this.defaultPageSize = Integer.parseInt(strDefaultPageSize);
        this.defaultUseFlag = Boolean.parseBoolean(strDefaultUseFlag);
        this.defaultCheckFlag = Boolean.parseBoolean(strDefaultCheckFlag);
        this.defaultCleanOrderBy = Boolean.parseBoolean(StringDefaultCleanOrderBy);
    }

    /**
     * TODO
     * 计算总数的SQL,
     * 这里需要根据数据库的类型改写SQL，目前支持MySQL和Oracle
     * @param currSql —— 当前执行的SQL
     * @return 改写后的SQL
     * @throws NotSupportedException
     */
    private String getTotalSQL(String currSql, String dbType) {
    	if (DB_TYPE_MYSQL.equals(dbType)) {
    	    return  "select count(*) as total from (" + currSql + ") $_paging";
    	} else if (DB_TYPE_ORACLE.equals(dbType)) {
    		return "select count(*) as total from (" + currSql +")";
    	} else {
    		LOGGER.error("当前插件未支持此类型数据库");
    		return "";
    	}
    }

    /**
     * TODO 需要使用其他数据库需要改写
     * 分页获取参数的SQL
     * 这里需要根据数据库的类型改写SQL，目前支持MySQL和Oracle
     * @param currSql —— 当前执行的SQL
     * @return 改写后的SQL
     * @throws NotSupportedException
     */
    private String getPageDataSQL(String currSql, String dbType) {
    	if (DB_TYPE_MYSQL.equals(dbType)) {
    	    return "select * from (" + currSql + ") $_paging_table limit ?, ?";
    	} else if (DB_TYPE_ORACLE.equals(dbType)) {
    		return " select * from (select cur_sql_result.*, rownum rn from (" + currSql + ") cur_sql_result  where rownum <= ?) where rn > ?";
    	} else {
    		LOGGER.error("当前插件未支持此类型数据库");
    		return "";
    	}
    }

    /**
     * TODO 需要使用其他数据库需要改写
     * 使用PreparedStatement预编译两个分页参数，如果数据库的规则不一样，需要改写设置的参数规则。目前支持MySQL和Oracle
     * @throws SQLException
     * @throws NotSupportedException
     *
     */
    private void preparePageDataParams(PreparedStatement ps, int pageNum, int pageSize, String dbType) throws SQLException  {
    	//prepared()方法编译SQL，由于MyBatis上下文没有我们分页参数的信息，所以这里需要设置这两个参数.
        	//获取需要设置的参数个数，由于我们的参数是最后的两个，所以很容易得到其位置
        	int idx = ps.getParameterMetaData().getParameterCount();
        	if (DB_TYPE_MYSQL.equals(dbType)) {
	        	//最后两个是我们的分页参数.
	        	ps.setInt(idx -1, (pageNum - 1) * pageSize);//开始行
	        	ps.setInt(idx, pageSize); //限制条数
        	} else if (DB_TYPE_ORACLE.equals(dbType)) {
        		ps.setInt(idx -1, pageNum * pageSize);//开始行
        		ps.setInt(idx, (pageNum - 1) * pageSize); //限制条数
        	} else {
        		LOGGER.error("当前插件未支持此类型数据库");
        	}
    }

    /**
     *
     * TODO 需要使用其他数据库需要改写
     * 目前支持MySQL和Oracle
     * @param mappedStatement
     * @return
     * @throws Exception
     */
    private String getDataSourceType(MappedStatement mappedStatement) throws Exception {
    	return DB_TYPE_ORACLE;
    	/*String dbConnectionStr = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection().toString();
    	dbConnectionStr = dbConnectionStr.toLowerCase();
        if (dbConnectionStr.contains(DB_TYPE_MYSQL)) {
        	return DB_TYPE_MYSQL;
        } else if (dbConnectionStr.contains(DB_TYPE_ORACLE)) {
        	return DB_TYPE_ORACLE;
        } else {
        	LOGGER.error("当前插件未支持此类型数据库");
    		return "";
        }*/
    }
}