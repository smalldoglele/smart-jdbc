package org.smart.jdbc;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;

/**
 * @since 2014-5-6 下午6:16:57
 * @author walden
 */
public interface ISmartJdbc {
    
    /**
     * 执行sql
     * @author walden
     */
    public void execute(CharSequence sql);
    
    /**
     * 根据sql和paramMap中的参数执行更新语句
     * @param sql
     * @param paramMap 命名参数集合
     * @return
     * @throws DataAccessException
     * @author walden
     */
    public int update(CharSequence sql, Map<String, ?> paramMap);
    
    /**
     * 根据sql和batchValues中的参数执行批量更新语句
     * @param sql
     * @param batchValues
     * @return
     * @throws DataAccessException
     * @author walden
     */
    public int[] batchUpdate(CharSequence sql, Map<String, ?>[] batchValues);
    
    /**
     * 根据sql获得一个结果，过多于一个结果不报错
     * @param sql
     * @param paramMap
     * @param domain
     * @return
     * @author walden
     */
    public <D> D get(CharSequence sql, Map<String, Object> paramMap, Class<D> domain);
    
    /**
     * 获得整形的值
     * @param sql
     * @param paramMap
     * @return
     * @author walden
     */
    public Integer getInteger(CharSequence sql, Map<String, Object> paramMap);
    
    /**
     * 获得长整型的值
     * @param sql
     * @param paramMap
     * @return
     * @author walden
     */
    public Long getLong(CharSequence sql, Map<String, Object> paramMap);
    
    /**
     * 获得浮点数数值
     * @param sql
     * @param paramMap
     * @return
     * @author walden
     */
    public Double getDouble(CharSequence sql, Map<String, Object> paramMap);
    
    /**
     * 根据sql查询返回值封装到List<D>
     * @param sql
     * @param paramMap
     * @param domain
     * @return
     * @author walden
     */
    public <D> List<D> findList(CharSequence sql, Map<String, Object> paramMap, Class<D> domain);
    
    /**
     * 根据sql查询单列返回值封装到List
     * @param sql
     * @param paramMap
     * @param domain
     * @return
     * @author walden
     */
    public <D> List<D> findColumnList(CharSequence sql, Map<String, Object> paramMap, Class<D> domain);
    
    /**
     * 查询分页列表
     * @param sql 必须带有order by的sql
     * @param paramMap 命名函数的参数
     * @param startRow 开始的索引(不包含)0,10,20,30
     * @param pageSize
     * @param domain 要的换类型
     * @return
     * @author walden
     */
    public <D> List<D> findPageList(CharSequence sql, Map<String, Object> paramMap, int startRow, int pageSize, Class<D> domain);
    
    /**
     * 查询前top条数据列表
     * @param sql 必须带有order by的sql
     * @param paramMap 命名函数的参数
     * @param top 数据的条数10,20,30
     * @return
     * @author walden
     */
    public <D> List<D> findTopList(CharSequence sql, Map<String, Object> paramMap, int top, Class<D> domain);
    
    /**
     * 查询分页信息
     * @param sql
     * @param map
     * @param page
     * @param domain
     * @return
     * @author walden
     */
    <D extends Serializable> IPage<D> getPage(CharSequence sql, Map<String, Object> paramMap, IPage<D> page, Class<D> domain);
    
}
