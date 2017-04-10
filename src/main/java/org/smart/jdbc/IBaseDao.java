package org.smart.jdbc;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IBaseDao<T extends Serializable, ID extends Serializable> extends ISmartJdbc {
    
    /**
     * 保存一个对象到数据库
     * @param entity
     * @return 1/0
     * @author walden
     */
    public int save(T entity);
    
    /**
     * 保存一个对象到数据库
     * <p>
     * 如果skipNullValue=false,相当于save(T entity); 使用这个函数的时候skipNullValue为true,表示不将空值插入到数据库。
     * </p>
     * @param entity
     * @param skipNullValue 是否跳过null值
     * @return 1/0
     * @author walden
     */
    public int save(T entity, boolean skipNullValue);
    
    /**
     * 使用批量保存一个集合中的对象到数据库
     * @param collection
     * @return [1/0,1/0,1/0,...]
     * @author walden
     */
    public int[] save(Collection<T> collection);
    
    /**
     * 根据ID删除一个对象
     * @param id
     * @return 1/0
     * @author walden
     */
    public int delete(ID id);
    
    /**
     * 根据example的值删除对应的数据库记录。
     * 
     * <pre>
     *   如果一个对象有下面的值：
     *   example.setCtype("新闻"); 
     *   example.setCtitle("我爱北京");
     *   会生成一下SQL：delete from {tableName}  where ctype='新闻' and ctitle='我爱北京';
     *   如果example的id被使用了调用delete(String id)
     * </pre>
     * @param example
     * @return
     * @author walden
     */
    public int deleteByExample(T example);
    
    /**
     * 更新一个对象到数据库
     * @param entity
     * @return 1/0
     * @author walden
     */
    public int update(T entity);
    
    /**
     * 更新一个对象到数据库
     * <p>
     * 是否跳过null值,如果skipNullValue=false,相当于update(T entity), 使用这个函数的时候skipNullValue为true,表示不将空值更新到数据库。
     * </p>
     * @param entity
     * @param skipNullValue
     * @return
     * @author walden
     */
    public int update(T entity, boolean skipNullValue);
    
    /**
     * 使用example的非空值做条件更新entity的非空值到数据库中去
     * 
     * <pre>
     *   如果一个对象有下面的值：
     *   
     *   entity.setCtype("新闻栏目");
     *   entity.setCdesc("XXYYZZ");
     *   
     *   example.setCtype("新闻"); 
     *   example.setCtitle("我爱北京");
     *   会生成一下SQL：update {tableName} set ctype='新闻栏目',cdesc='XXYYZZ'  where ctype='新闻' and ctitle='我爱北京';
     *  
     *   如果example的id被使用了调用update(T entity)
     * </pre>
     * @param entity
     * @param example
     * @return
     * @author walden
     */
    public int update(T entity, T example);
    
    /**
     * 使用批量更新对象集合到数据库
     * @param collection
     * @return [1/0,1/0,1/0,...]
     * @author walden
     */
    public int[] update(Collection<T> collection);
    
    /**
     * 根据ID的值从数据库获得一个对象
     * @param id
     * @return T
     * @author walden
     */
    public T get(ID id);
    
    /**
     * 根据paramMap中键值对的值生成where条件，查询出符合条件的一个T对象
     * 
     * <pre>
     * 1,paramMap中的键必须是T中的字段。
     * 2,例如:
     *  T中有字段cname
     *  paramMap={"cname":'walden',"cnamexx",'walden'}
     *  生成sql:select * from {tableName} where cname='walden';
     *  如果cname不是T的字段 将被忽略，不拼接到where条件中,如cnamexx。
     * </pre>
     * @param paramMap
     * @return
     * @author walden
     */
    public T get(Map<String, Object> paramMap);
    
    /**
     * 根据example中的值生成where条件，查询出符合条件的对象
     * 
     * <pre>
     *   如果一个对象有下面的值：
     *   example.setCtype("新闻"); 
     *   example.setCtitle("我爱北京");
     *   会生成一下SQL：select * from {T.tableName}  where ctype='新闻' and ctitle='我爱北京';
     *   如果example的主键不为空,直接根据主键值,调用get(String id);
     * </pre>
     * @param example
     * @return
     * @author walden
     */
    public T getByExample(T example);
    
    /**
     * 根据paramMap中键值对的值生成where条件，查询出符合条件的T对象列表
     * 
     * <pre>
     * 1,paramMap中的键必须是T中的字段。
     * 2,例如:
     *  T中有字段cname
     *  paramMap={"cname":'walden',"cnamexx",'walden'}
     *  生成sql:select * from {tableName} where cname='walden';
     *  如果cname不是T的字段 将被忽略，不拼接到where条件中,如cnamexx。
     * </pre>
     * @param paramMap
     * @return
     * @author walden
     */
    public List<T> findList(Map<String, Object> paramMap);
    
    /**
     * 根据example中的值生成where条件，查询出符合条件的T对象列表
     * 
     * <pre>
     *   例如一个对象有下面的值： 
     *    example.setCtype("新闻"); 
     *    example.setCtitle("我爱北京");
     *   生成一下sql：select * from {T.tableName} where ctype='新闻' and ctitle='我爱北京';
     *   如果example的主键不为空,直接根据主键值,调用get(String id),返回一个只有一个值的列表;
     * </pre>
     * @param example
     * @return
     * @author walden
     */
    public List<T> findListByExample(T example);
    
    /**
     * 根据sql查询返回值封装到List<T>
     * @param sql
     * @param paramMap
     * @return
     * @author walden
     */
    public List<T> findList(CharSequence sql, Map<String, Object> paramMap);
    
    /**
     * 查询前top条数据列表
     * @param sql 必须带有order by的sql
     * @param paramMap 命名函数的参数
     * @param top 数据的条数10,20,30
     * @return
     * @author walden
     */
    public List<T> findTopList(CharSequence sql, Map<String, Object> paramMap, int top);
    
    /**
     * 查询分页列表
     * @param sql 必须带有order by的sql
     * @param paramMap 命名函数的参数
     * @param startRow 开始的索引(不包含)0,10,20,30
     * @param pageSize
     * @return
     * @author walden
     */
    public List<T> findPageList(CharSequence sql, Map<String, Object> paramMap, int startRow, int pageSize);
    
    /* ==================================== 以下两个变参方法=========================================== */
    /**
     * 根据可变参数对生成where条件，查询一个T对象
     * 
     * <pre>
     * 1,params必须是且key1,value1,key2,value2...keyN,valueN键值对的形式,且key=String,value=Objcet;key必须为T中的字段。
     * 2,例如:getByColumn("cname",walden,"password",123456)
     * 生成sql：select * from {T.tableName} where cname='walden' and password='123456';
     * </pre>
     * @param params
     * @return
     * @author walden
     */
    public T getByColumn(Object... params);
    
    /**
     * 根据可变参数对生成where条件，查询T对象列表
     * 
     * <pre>
     * 1,params必须是且key1,value1,key2,value2...keyN,valueN键值对的形式,且key=String,value=Objcet;key必须为T中的字段。
     * 2,例如:findListByColumn("cname",walden,"password",123456)
     * 生成sql：select * from {T.tableName} where cname='walden' and password='123456';
     * </pre>
     * @param params
     * @return
     * @author walden
     */
    public List<T> findListByColumn(Object... params);
    
    /* ==================================== 以下实现在baseDao中=========================================== */
    /**
     * 获得一个对象
     * @param sql
     * @param paramMap
     * @return
     * @author walden
     */
    T get(CharSequence sql, Map<String, Object> paramMap);
    
    /**
     * 查询分页信息
     * @param sql
     * @param paramMap
     * @param page
     * @return
     * @author walden
     */
    IPage<T> getPage(CharSequence sql, Map<String, Object> paramMap, IPage<T> page);
    
}
