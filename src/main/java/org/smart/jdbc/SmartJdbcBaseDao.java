package org.smart.jdbc;

import java.io.Serializable;
import java.util.Map;

import org.smart.jdbc.support.AbstractBaseDaoSupport;

public class SmartJdbcBaseDao<T extends Serializable, ID extends Serializable> extends AbstractBaseDaoSupport<T, ID> {
    
    /**
     * 根据sql和paramMap中的查询参数返回一个对象
     * @param sql
     * @param paramMap
     * @return
     * @author walden
     */
    public T get(CharSequence sql, Map<String, Object> paramMap) {
        return getJdbcTemplate().get(sql, paramMap, getEntityClass());
    }
    
    /**
     * 根据sql和paramMap中的查询参数返回一个类型为<D>的domain对象
     * @param sql
     * @param paramMap
     * @param domain
     * @return
     * @author walden
     */
    public <D> D get(CharSequence sql, Map<String, Object> paramMap, Class<D> domain) {
        return getJdbcTemplate().get(sql, paramMap, domain);
    }
    
    /**
     * 根据sql,paramMap,page中的查询参数返回一个分页对象
     * <p>
     * 该分页对象的totalCount为符合条件的总条数，results是泛型&lt;T&gt;对象的一个列表,列表的大小为page.pageSize
     * </p>
     * @param sql
     * @param paramMap
     * @param page 其中 currentPage,paseSize和pageIndexSize等查询参数需要初始化
     * @return
     * @author walden
     */
    public IPage<T> getPage(CharSequence sql, Map<String, Object> paramMap, IPage<T> page) {
        return getPage(sql, paramMap, page, getEntityClass());
    }
    
    /**
     * 根据sql,paramMap,page中的查询参数返回一个分页对象
     * <p>
     * 该分页对象的totalCount为符合条件的总条数，results是D对象的一个列表，size为pageSize
     * </p>
     * @param sql
     * @param paramMap
     * @param page 其中 currentPage,paseSize和pageIndexSize等查询参数需要初始化
     * @param domain
     * @return
     * @author walden
     */
    public <D extends Serializable> IPage<D> getPage(CharSequence sql, Map<String, Object> paramMap, IPage<D> page, Class<D> domain) {
        return getJdbcTemplate().getPage(sql, paramMap, page, domain);
    }
}
