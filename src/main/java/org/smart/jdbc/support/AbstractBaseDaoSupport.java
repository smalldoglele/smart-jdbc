package org.smart.jdbc.support;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.smart.jdbc.ILoader;
import org.smart.jdbc.SmartJdbcTemplate;
import org.smart.jdbc.object.ColumnDefined;
import org.smart.jdbc.object.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;


public abstract class AbstractBaseDaoSupport<T extends Serializable, ID extends Serializable> {

    private Log logger = LogFactory.getLog(getClass());

    private ILoader<Class<?>, EntityManager> loader = EntityLoaderSingleton.getInstance();

    @Autowired
    private SmartJdbcTemplate smartJdbcTemplate;

    private Class<T> entityClass;

    @SuppressWarnings("unchecked")
    public AbstractBaseDaoSupport() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * 根据sql和paramMap中的参数执行更新语句
     *
     * @param sql
     * @param paramMap 命名参数集合
     * @return 0/1
     * @throws DataAccessException
     * @author walden
     */
    protected int update(CharSequence sql, Map<String, ?> paramMap) throws DataAccessException {
        return getJdbcTemplate().update(sql, paramMap);
    }

    /**
     * 根据sql和batchValues中的参数执行批量更新语句
     *
     * @param sql
     * @param batchValues
     * @return 0/1 数组
     * @throws DataAccessException
     * @author walden
     */
    public int[] batchUpdate(CharSequence sql, Map<String, ?>[] batchValues) throws DataAccessException {
        return getJdbcTemplate().batchUpdate(sql, batchValues);
    }

    /**
     * 保存一个对象到数据库
     *
     * @param entity
     * @return 1/0
     * @author walden
     */
    public int save(T entity) {
        EntityManager entityManager = loader.load(getEntityClass());
        String sql = entityManager.getInsertDefaultSql();
        Map<Field, ColumnDefined> fcMap = entityManager.getFieldColumnMap();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        for (Field field : fcMap.keySet()) {
            ColumnDefined columnDefined = fcMap.get(field);
            String columnName = columnDefined.getName();
            paramMap.put(columnName, getValueFromObjectByField(field, entity));
        }
        return update(sql, paramMap);
    }

    /**
     * 保存一个对象到数据库,并将id回写到
     *
     * @param entity
     * @return
     */
    public Number saveReId(T entity) {
        EntityManager entityManager = loader.load(getEntityClass());
        String sql = entityManager.getInsertDefaultSql();
        Map<Field, ColumnDefined> fcMap = entityManager.getFieldColumnMap();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        for (Field field : fcMap.keySet()) {
            ColumnDefined columnDefined = fcMap.get(field);
            String columnName = columnDefined.getName();
            paramMap.put(columnName, getValueFromObjectByField(field, entity));
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getJdbcTemplate().update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        //回写到实体中
        setValue2ObjectByField(entityManager.getIdField(), entity, keyHolder.getKey());
        return keyHolder.getKey();
    }

    /**
     * 有选择行的将一个对象的非空值保存到数据库
     *
     * @param entity
     * @return
     */
    public Number saveSelectiveReId(T entity) {
        EntityManager entityManager = loader.load(getEntityClass());
        String template = "insert into %s(%s) values(%s)";
        String tableName = entityManager.getTableDefined().getName();
        List<String> columnList = new ArrayList<String>();
        List<String> namedParameterList = new ArrayList<String>();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        Map<Field, ColumnDefined> fcMap = entityManager.getFieldColumnMap();
        genarateNamedSqlAndMap(entity, columnList, namedParameterList, paramMap, fcMap);
        String sql = String.format(template, tableName, entityManager.getListString(columnList), entityManager.getListString(namedParameterList));
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getJdbcTemplate().update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        //回写到实体中
        setValue2ObjectByField(entityManager.getIdField(), entity, keyHolder.getKey());
        return keyHolder.getKey();
    }

    /**
     * 生成命名sql和命名参数map
     *
     * @param entity
     * @param columnList
     * @param namedParameterList
     * @param paramMap
     * @param fcMap
     */
    private void genarateNamedSqlAndMap(T entity, List<String> columnList, List<String> namedParameterList,
                                        Map<String, Object> paramMap, Map<Field, ColumnDefined> fcMap) {
        for (Field field : fcMap.keySet()) {
            ColumnDefined columnDefined = fcMap.get(field);
            String columnName = columnDefined.getName();
            Object fieldValue = null;
            fieldValue = getValueFromObjectByField(field, entity);
            if (fieldValue != null) {
                columnList.add(columnName);
                namedParameterList.add(":" + columnName);
                paramMap.put(columnName, fieldValue);
            }
        }
    }

    /**
     * 保存一个对象到数据库
     * <p>
     * 如果skipNullValue=false,相当于save(T entity); 使用这个函数的时候skipNullValue为true,表示不将空值插入到数据库。
     * </p>
     * 如果保存的时候 有选择行的保存请使用 ,否则请使用@see save
     *
     * @param entity
     * @param skipNullValue 是否跳过null值，如果为true,表示不将空值插入到数据库。
     * @return 1/0
     * @author walden
     * @see AbstractBaseDaoSupport#save(T entity)
     * @see AbstractBaseDaoSupport#saveSelective(T entity)
     */
    @Deprecated
    public int save(T entity, boolean skipNullValue) {
        if (skipNullValue) {
            return saveSelective(entity);
        } else {
            return save(entity);
        }
    }

    /**
     * 有选择行的将一个对象的非空值保存到数据库
     *
     * @param entity
     * @return
     */
    public int saveSelective(T entity) {
        EntityManager entityManager = loader.load(getEntityClass());
        String template = "insert into %s(%s) values(%s)";
        String tableName = entityManager.getTableDefined().getName();
        List<String> columnList = new ArrayList<String>();
        List<String> namedParameterList = new ArrayList<String>();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        Map<Field, ColumnDefined> fcMap = entityManager.getFieldColumnMap();
        genarateNamedSqlAndMap(entity, columnList, namedParameterList, paramMap, fcMap);
        String sql = String.format(template, tableName, entityManager.getListString(columnList), entityManager.getListString(namedParameterList));
        return update(sql, paramMap);
    }

    /**
     * 保存对象集合到数据库
     *
     * @param collection
     * @return [1/0,1/0,1/0,...]
     * @author walden
     */
    public int[] save(Collection<T> collection) {
        if (collection == null || collection.size() == 0) return null;
        EntityManager entityManager = loader.load(getEntityClass());
        String sql = entityManager.getInsertDefaultSql();
        Map<Field, ColumnDefined> fcMap = entityManager.getFieldColumnMap();
        @SuppressWarnings("unchecked")
        Map<String, Object>[] paramMaps = new HashMap[collection.size()];
        Iterator<T> iterator = collection.iterator();
        Map<String, Object> paramMap = null;
        for (int index = 0; iterator.hasNext(); index++) {
            paramMap = new HashMap<String, Object>();
            T entity = iterator.next();
            for (Field field : fcMap.keySet()) {
                ColumnDefined columnDefined = fcMap.get(field);
                String columnName = columnDefined.getName();
                paramMap.put(columnName, getValueFromObjectByField(field, entity));
            }
            paramMaps[index] = paramMap;
        }
        return batchUpdate(sql, paramMaps);
    }

    /**
     * 根据ID删除一个对象
     *
     * @param id
     * @return 1/0
     * @author walden
     */
    public int delete(ID id) {
        EntityManager entityManager = loader.load(getEntityClass());
        String sql = entityManager.getDeleteDefaultSql();
        String idColumnName = entityManager.getIdColumnName();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put(idColumnName, id);
        return update(sql, paramMap);
    }

    /**
     * 根据example的值删除对应的数据库记录。
     * <p/>
     * <pre>
     *   如果一个对象有下面的值：
     *   example.setCtype("新闻");
     *   example.setCtitle("我爱北京");
     *   会生成一下SQL：delete from {tableName}  where ctype='新闻' and ctitle='我爱北京';
     *   如果example的id被使用了调用delete(String id)
     * </pre>
     *
     * @param example
     * @return 0/n
     * @author walden
     */
    public int deleteByExample(T example) {
        EntityManager entityManager = loader.load(getEntityClass());
        String template = "delete from %s %s";
        String tableName = entityManager.getTableDefined().getName();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        String whereSql = buildWhere4ByExample(example, entityManager, paramMap);
        String sql = String.format(template, tableName, whereSql);
        return update(sql, paramMap);

    }

    /**
     * 更新一个对象到数据库
     *
     * @param entity
     * @return 1/n
     * @author walden
     */
    public int update(T entity) {
        EntityManager entityManager = loader.load(getEntityClass());
        String sql = entityManager.getUpdateDefaultSql();
        Map<Field, ColumnDefined> fcMap = entityManager.getFieldColumnMap();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        for (Field field : fcMap.keySet()) {
            ColumnDefined columnDefined = fcMap.get(field);
            String columnName = columnDefined.getName();
            paramMap.put(columnName, getValueFromObjectByField(field, entity));
        }
        return update(sql.toString(), paramMap);
    }

    /**
     * 更新对象集合到数据库
     *
     * @param collection
     * @return [1/0,1/0,1/0,...]
     * @author walden
     */
    public int[] update(Collection<T> collection) {
        if (collection == null || collection.size() == 0) return null;
        EntityManager entityManager = loader.load(getEntityClass());
        String sql = entityManager.getUpdateDefaultSql();
        Map<Field, ColumnDefined> fcMap = entityManager.getFieldColumnMap();
        Map<String, Object>[] paramMaps = new HashMap[collection.size()];
        Iterator<T> iterator = collection.iterator();

        for (int index = 0; iterator.hasNext(); index++) {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            T entity = iterator.next();
            for (Field field : fcMap.keySet()) {
                ColumnDefined columnDefined = fcMap.get(field);
                String columnName = columnDefined.getName();
                paramMap.put(columnName, getValueFromObjectByField(field, entity));
            }
            paramMaps[index] = paramMap;
        }
        return batchUpdate(sql, paramMaps);
    }

    /**
     * 更新一个对象到数据库
     * <p>
     * 是否跳过null值,如果skipNullValue=false,相当于update(T entity), 使用这个函数的时候skipNullValue为true,相当于updateSelective(T entity)表示不将空值更新到数据库。
     * </p>
     *
     * @param entity
     * @param skipNullValue 为true,表示不将空值更新到数据库。
     * @return
     * @author walden
     * @see AbstractBaseDaoSupport#update(T entity)
     * @see AbstractBaseDaoSupport#updateSelective(T entity)
     */
    @Deprecated
    public int update(T entity, boolean skipNullValue) {
        if (skipNullValue) {
            return updateSelective(entity);
        } else {
            return update(entity);
        }
    }

    /**
     * 有选择行的将一个对象的非空值更新到数据库
     *
     * @param entity
     * @return
     */
    public int updateSelective(T entity) {
        EntityManager entityManager = loader.load(getEntityClass());
        String template = "update %s set %s where %s=:%s";
        Field idField = entityManager.getIdField();
        String idColumnName = entityManager.getIdColumnName();
        String tableName = entityManager.getTableDefined().getName();
        List<String> updateSetItemList = new ArrayList<String>();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        Map<Field, ColumnDefined> fcMap = entityManager.getFieldColumnMap();
        for (Field field : fcMap.keySet()) {
            ColumnDefined columnDefined = fcMap.get(field);
            String columnName = columnDefined.getName();
            Object fieldValue = getValueFromObjectByField(field, entity);
            if (fieldValue != null) {
                if (field != idField) updateSetItemList.add(columnName + "=:" + columnName);
                paramMap.put(columnName, fieldValue);
            }
        }
        String sql = String.format(template, tableName, entityManager.getListString(updateSetItemList), idColumnName, idColumnName);
        return update(sql, paramMap);

    }

    /**
     * 使用example的非空值做条件更新entity的非空值到数据库中去
     * <p/>
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
     *
     * @param entity
     * @param example
     * @return
     * @author walden
     */
    public int update(T entity, T example) {
        if (entity != null && example != null) {
            EntityManager entityManager = loader.load(getEntityClass());
            Map<Field, ColumnDefined> fcMap = entityManager.getFieldColumnMap();
            String template = "update %s set %s %s";
            List<String> setItems = new ArrayList<String>();
            StringBuffer whereSql = new StringBuffer();
            Map<String, Object> paramMap = new HashMap<String, Object>();
            boolean whereUseId = false;// 如果使用uid做条件不在拼接其他条件
            Field idField = entityManager.getIdField();
            String idFieldName = entityManager.getIdColumnName();
            Object idValue = getValueFromObjectByField(idField, example);
            if (idValue != null) {
                whereSql.append(" and " + idFieldName + "=:" + idFieldName + "_2");
                paramMap.put(idFieldName + "_2", idFieldName);
                whereUseId = true;
            }

            for (Field field : fcMap.keySet()) {
                ColumnDefined columnDefined = fcMap.get(field);
                String columnName = columnDefined.getName();
                Object setValue = getValueFromObjectByField(field, entity);
                if (setValue != null) {
                    setItems.add(columnName + "=:" + columnName + "_1");
                    paramMap.put(columnName + "_1", setValue);
                }
                if (!whereUseId) {
                    Object whereValue = getValueFromObjectByField(field, example);
                    if (whereValue != null) {
                        whereSql.append(" and " + columnName + "=:" + columnName + "_2");
                        paramMap.put(columnName + "_2", whereValue);
                    }
                }
            }
            if (setItems.size() == 0) {
                logger.warn("entity 为空 不能生成有效的setitems项,sql执行失败!");
                return 0;
            } else {
                String where = whereSql.length() > 0 ? whereSql.toString().replaceFirst(" and", "where") : "";
                String sql = String.format(template, entityManager.getTableDefined().getName(), entityManager.getListString(setItems), where);
                return update(sql, paramMap);
            }
        } else {
            logger.warn("entityd和example都不能为空!");
            return 0;
        }
    }

    /**
     * 执行sql
     *
     * @author walden
     */
    protected void execute(CharSequence sql) {
        getJdbcTemplate().execute(sql);
    }

    /**
     * 根据ID的值从数据库获得一个对象
     *
     * @param id
     * @return T
     * @author walden
     */
    public T get(ID id) {
        EntityManager entityManager = loader.load(getEntityClass());
        String sql = entityManager.getSelectDefaultSql();
        String idColumnName = entityManager.getIdColumnName();
        Map<String, Object> paramMap = new HashMap<String, Object>(1);
        paramMap.put(idColumnName, id);
        return getJdbcTemplate().get(sql, paramMap, getEntityClass());
    }

    /**
     * 根据example中的值生成where条件，查询出符合条件的对象
     * <p/>
     * <pre>
     *   如果一个对象有下面的值：
     *   example.setCtype("新闻");
     *   example.setCtitle("我爱北京");
     *   会生成一下SQL：select * from {T.tableName}  where ctype='新闻' and ctitle='我爱北京';
     *   如果example的主键不为空,直接根据主键值,调用get(String id);
     * </pre>
     *
     * @param example
     * @return
     * @author walden
     */
    public T getByExample(T example) {
        return getJdbcTemplate().getSingleResult(findListByExample(example));
    }

    /**
     * 根据paramMap中键值对的值生成where条件，查询出符合条件的一个T对象
     * <p/>
     * <pre>
     * 1,paramMap中的键必须是T中的字段。
     * 2,例如:
     *  T中有字段cname
     *  paramMap={"cname":'walden',"cnamexx",'walden'}
     *  生成sql:select * from {tableName} where cname='walden';
     *  如果cname不是T的字段 将被忽略，不拼接到where条件中,如cnamexx。
     * </pre>
     *
     * @param paramMap
     * @return
     * @author walden
     */
    public T get(Map<String, Object> paramMap) {
        return getJdbcTemplate().getSingleResult(findList(paramMap));
    }

    /**
     * 查询一个整数
     *
     * @param sql
     * @param paramMap
     * @return
     * @author walden
     */
    protected Integer getInteger(CharSequence sql, Map<String, Object> paramMap) {
        return getJdbcTemplate().getInteger(sql, paramMap);
    }

    /**
     * 查询一个大整数
     *
     * @param sql
     * @param paramMap
     * @return
     * @author walden
     */
    protected Long getLong(CharSequence sql, Map<String, Object> paramMap) {
        return getJdbcTemplate().getLong(sql, paramMap);
    }

    /**
     * 查询一个浮点型的结果集
     *
     * @param sql
     * @param paramMap
     * @return
     * @author walden
     */
    protected Double getDouble(CharSequence sql, Map<String, Object> paramMap) {
        return getJdbcTemplate().getDouble(sql, paramMap);
    }

    /**
     * 根据example中的值生成where条件，查询出符合条件的T对象列表
     * <p/>
     * <pre>
     *   例如一个对象有下面的值：
     *    example.setCtype("新闻");
     *    example.setCtitle("我爱北京");
     *   生成一下sql：select * from {T.tableName} where ctype='新闻' and ctitle='我爱北京';
     *   如果example的主键不为空,直接根据主键值,调用get(String id),返回一个只有一个值的列表;
     * </pre>
     *
     * @param example
     * @return
     * @author walden
     */
    public List<T> findListByExample(T example) {
        EntityManager entityManager = loader.load(getEntityClass());
        Field idField = entityManager.getIdField();
        @SuppressWarnings("unchecked")
/*        ID idValue = (ID) getValueFromObjectByField(idField, example);
        if (idValue != null) {
            String sql = entityManager.getSelectDefaultSql();
            String idColumnName = entityManager.getIdColumnName();
            Map<String, Object> paramMap = new HashMap<String, Object>(1);
            paramMap.put(idColumnName, idValue);
            return getJdbcTemplate().findList(sql, paramMap, getEntityClass());
        } else {*/
                String template = "select * from %s %s";
        String tableName = entityManager.getTableDefined().getName();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        String whereSql = buildWhere4ByExample(example, entityManager, paramMap);
        String sql = String.format(template, tableName, whereSql);
        return getJdbcTemplate().findList(sql, paramMap, getEntityClass());
        /*}*/
    }

    /**
     * 创建where SQL并将param放入的paramMap中
     *
     * @param example
     * @param entityManager
     * @param paramMap
     * @return
     */
    private String buildWhere4ByExample(T example, EntityManager entityManager, Map<String, Object> paramMap) {
        StringBuffer whereSql = new StringBuffer();
        Map<Field, ColumnDefined> fcMap = entityManager.getFieldColumnMap();
        for (Field field : fcMap.keySet()) {
            ColumnDefined columnDefined = fcMap.get(field);
            String columnName = columnDefined.getName();
            Object fieldValue = getValueFromObjectByField(field, example);
            if (fieldValue != null) {
                whereSql.append(String.format(" and %s=:%s", columnName, columnName));
                paramMap.put(columnName, fieldValue);
            }
        }
        if (whereSql.length() > 0) whereSql.replace(0, 4, " where");
        return whereSql.toString();
    }

    /**
     * 根据paramMap中键值对的值生成where条件，查询出符合条件的T对象列表
     * <p/>
     * <pre>
     * 1,paramMap中的键必须是T中的字段。
     * 2,例如:
     *  T中有字段cname
     *  paramMap={"cname":'walden',"cnamexx",'walden'}
     *  生成sql:select * from {tableName} where cname='walden';
     *  如果cname不是T的字段 将被忽略，不拼接到where条件中,如cnamexx。
     * </pre>
     *
     * @param paramMap
     * @return
     * @author walden
     */
    public List<T> findList(Map<String, Object> paramMap) {
        EntityManager entityManager = loader.load(getEntityClass());
        String idColumnName = entityManager.getIdColumnName();
        String sql;
/*        if (paramMap.containsKey(idColumnName)) {
            sql = entityManager.getSelectDefaultSql();
        } else {*/
        sql = buildSelectSql4ByColumn(paramMap);
       /* }*/
        return getJdbcTemplate().findList(sql, paramMap, getEntityClass());
    }

    /**
     * 根据sql和paramMap中的参数查询数据位<b>单列值</b>
     *
     * @param sql
     * @param paramMap
     * @param domain
     * @return
     * @author walden
     */
    protected <D> List<D> findColumnList(CharSequence sql, Map<String, Object> paramMap, Class<D> domain) {
        return getJdbcTemplate().findColumnList(sql, paramMap, domain);
    }

    /**
     * 根据sql查询返回值封装到List<T>
     *
     * @param sql
     * @param paramMap
     * @return
     * @author walden
     */
    protected List<T> findList(CharSequence sql, Map<String, Object> paramMap) {
        return findList(sql, paramMap, getEntityClass());
    }

    /**
     * 根据sql和paramMap参数查询D的对象列表
     *
     * @param sql
     * @param paramMap
     * @param domain
     * @return
     * @author walden
     */
    protected <D> List<D> findList(CharSequence sql, Map<String, Object> paramMap, Class<D> domain) {
        return getJdbcTemplate().findList(sql, paramMap, domain);
    }

    /**
     * 将可变参数放到paramMap中去
     *
     * @param params
     * @return
     * @author walden
     */
    private Map<String, Object> buildParamMapByArgs(Object[] params) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        if (params != null && params.length > 1) {
            int length = params.length % 2 == 0 ? params.length : params.length - 1;
            for (int i = 0; i < length; i += 2) {
                paramMap.put((String) params[i], params[i + 1]);
            }
        } else {
            logger.info("params不能为空!");
        }
        return paramMap;
    }

    /**
     * 查询分页列表
     *
     * @param sql      【sqlserver中】必须带有order by的sql
     * @param paramMap 命名函数的参数
     * @param startRow 开始的索引(不包含)0,10,20,30
     * @param pageSize
     * @return
     * @author walden
     */
    protected List<T> findPageList(CharSequence sql, Map<String, Object> paramMap, int startRow, int pageSize) {
        return findPageList(sql, paramMap, startRow, pageSize, getEntityClass());
    }

    /**
     * 查询分页列表 分会对象为D类型
     *
     * @param sql
     * @param paramMap
     * @param startRow
     * @param pageSize
     * @param domain
     * @return
     * @author walden
     */
    protected <D> List<D> findPageList(CharSequence sql, Map<String, Object> paramMap, int startRow, int pageSize, Class<D> domain) {
        return getJdbcTemplate().findPageList(sql, paramMap, startRow, pageSize, domain);
    }

    /**
     * 查询前top条数据列表
     *
     * @param sql      必须带有order by的sql
     * @param paramMap 命名函数的参数
     * @param top      数据的条数10,20,30
     * @return
     * @author walden
     */
    protected List<T> findTopList(CharSequence sql, Map<String, Object> paramMap, int top) {
        return findPageList(sql, paramMap, 0, top);
    }

    /**
     * 根据sql,paramMap,page中的查询参数返回一个分页列表
     *
     * @param sql
     * @param paramMap
     * @param top
     * @param domain
     * @return
     * @author walden
     */
    protected <D> List<D> findTopList(CharSequence sql, Map<String, Object> paramMap, int top, Class<D> domain) {
        return findPageList(sql, paramMap, 0, top, domain);
    }

    /**
     * 根据可变参数对生成where条件，查询一个T对象
     * <p/>
     * <pre>
     * 1,params必须是且key1,value1,key2,value2...keyN,valueN键值对的形式,且key=String,value=Objcet;key必须为T中的字段。
     * 2,例如:getByColumn("cname",walden,"password",123456)
     * 生成sql：select * from {T.tableName} where cname='walden' and password='123456';
     * </pre>
     *
     * @param params
     * @return
     * @author walden
     */
    public T getByColumn(Object... params) {
        return get(buildParamMapByArgs(params));
    }

    /**
     * 根据可变参数对生成where条件，查询T对象列表
     * <p/>
     * <pre>
     * 1,params必须是且key1,value1,key2,value2...keyN,valueN键值对的形式,且key=String,value=Objcet;key必须为T中的字段。
     * 2,例如:findListByColumn("cname",walden,"password",123456)
     * 生成sql：select * from {T.tableName} where cname='walden' and password='123456';
     * </pre>
     *
     * @param params
     * @return
     * @author walden
     */
    public List<T> findListByColumn(Object... params) {
        return findList(buildParamMapByArgs(params));
    }

    /**
     * 设置一个实体的字段值
     *
     * @param field
     * @param obj
     * @param fieldValue
     */
    private void setValue2ObjectByField(Field field, Object obj, Object fieldValue) {
        try {
            field.set(obj, fieldValue);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    /**
     * 通过filed的反射机制从对象中取出给字段的值
     *
     * @param field
     * @param obj
     * @return
     */
    private Object getValueFromObjectByField(Field field, Object obj) {
        Object result = null;
        try {
            result = field.get(obj);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 如果PO中有对应的字段就将这个键值对拼接到where语句中
     *
     * @param paramMap
     * @return
     * @author walden
     */
    private String buildSelectSql4ByColumn(Map<String, Object> paramMap) {
        String template = "select * from %s %s";
        EntityManager entityManager = loader.load(getEntityClass());
        String tableName = entityManager.getTableDefined().getName();
        List<String> columnList = entityManager.getColumnNameList();
        StringBuffer whereSql = new StringBuffer();
        if (paramMap != null && paramMap.size() > 0) {
            for (String paramKey : paramMap.keySet()) {
                if (columnList.contains(paramKey)) {
                    whereSql.append(String.format(" and %s=:%s", paramKey, paramKey));
                } else {
                    logger.info(String.format(">>>>[%s.%s]不存在!", tableName, paramKey));
                }
            }
        }
        if (whereSql.length() > 0) whereSql.replace(0, 4, " where");
        return String.format(template, tableName, whereSql);
    }

    public ILoader<Class<?>, EntityManager> getLoader() {
        return loader;
    }

    public void setLoader(ILoader<Class<?>, EntityManager> loader) {
        this.loader = loader;
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public void setJdbcTemplate(SmartJdbcTemplate smartJdbcTemplate) {
        this.smartJdbcTemplate = smartJdbcTemplate;
    }

    public SmartJdbcTemplate getJdbcTemplate() {
        return smartJdbcTemplate;
    }
}
