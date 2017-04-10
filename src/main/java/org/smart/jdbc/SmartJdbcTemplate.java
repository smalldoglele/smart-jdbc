package org.smart.jdbc;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.smart.jdbc.dialect.Dialect;
import org.smart.jdbc.support.DialectFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class SmartJdbcTemplate extends NamedParameterJdbcTemplate implements ISmartJdbc {

    private Log logger = LogFactory.getLog(getClass());

    private static DataSource dataSource;

    private String dialect;

    public SmartJdbcTemplate() {
        super(dataSource);
    }

    public SmartJdbcTemplate(DataSource dataSource) {
        super(dataSource);
    }

    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

    public void execute(CharSequence sql) {
        sqlExecuteLogger(sql);
        getJdbcOperations().execute(sql.toString());
    }

    public int update(CharSequence sql, Map<String, ?> paramMap) throws DataAccessException {
        sqlExecuteLogger(sql, paramMap);
        return super.update(sql.toString(), paramMap);
    }

    public int[] batchUpdate(CharSequence sql, Map<String, ?>[] batchValues) throws DataAccessException {
        sqlExecuteLogger(sql, batchValues);
        return super.batchUpdate(sql.toString(), batchValues);
    }

    public <D> D get(CharSequence sql, Map<String, Object> paramMap, Class<D> domain) {
        return getSingleResult(findList(sql, paramMap, domain));
    }

    public Integer getInteger(CharSequence sql, Map<String, Object> paramMap) {
        sqlExecuteLogger(sql, paramMap);
        return queryForObject(sql.toString(), paramMap, Integer.class);
    }

    public Long getLong(CharSequence sql, Map<String, Object> paramMap) {
        sqlExecuteLogger(sql, paramMap);
        return queryForObject(sql.toString(), paramMap, Long.class);
    }

    public Double getDouble(CharSequence sql, Map<String, Object> paramMap) {
        sqlExecuteLogger(sql, paramMap);
        return queryForObject(sql.toString(), paramMap, Double.class);
    }

    public <D> List<D> findList(CharSequence sql, Map<String, Object> paramMap, Class<D> domain) {
        sqlExecuteLogger(sql, paramMap);
        return query(sql.toString(), paramMap, new BeanPropertyRowMapper<D>(domain));
    }

    public <D> List<D> findColumnList(CharSequence sql, Map<String, Object> paramMap, Class<D> domain) {
        sqlExecuteLogger(sql, paramMap);
        return queryForList(sql.toString(), paramMap, domain);
    }

    public <D> List<D> findPageList(CharSequence sql, Map<String, Object> paramMap, int startRow, int pageSize, Class<D> domain) {
        Dialect dialect = DialectFactory.getDialect(getDialect());
        sql = dialect.getLimitString(sql.toString(), startRow, pageSize);
        sqlExecuteLogger(sql, paramMap);
        return query(sql.toString(), paramMap, new BeanPropertyRowMapper<D>(domain));
    }

    public <D> List<D> findTopList(CharSequence sql, Map<String, Object> paramMap, int top, Class<D> domain) {
        return findPageList(sql, paramMap, 0, top, domain);
    }

    public <D extends Serializable> IPage<D> getPage(CharSequence sql, Map<String, Object> paramMap, IPage<D> page, Class<D> domain) {
        page.setTotalCount(getTotalCount(sql.toString(), paramMap));
        page.setResults(findPageList(sql, paramMap, page.getStartRow(), page.getPageSize(), domain));
        return page;
    }

    /**
     * 根据查询列表的语句生成select count(*) 语句查到总条数
     *
     * @param sql
     * @param paramMap
     * @return
     * @author walden
     */
    protected int getTotalCount(String sql, Map<String, Object> paramMap) {
        StringBuilder sqlLowerCase = new StringBuilder(sql.toLowerCase());
        Pattern pattern = Pattern.compile("(\\s*select\\s+)|(\\s+from\\s+)");
        Matcher matcher = pattern.matcher(sqlLowerCase);
        int blance = 0;
        while (matcher.find()) {
            String m = matcher.group();
            if ("select".equals(m.trim()))
                --blance;
            else
                ++blance;

            if (blance == 0) break;
        }
        //定位到order by 语句的位置
        int orderbyIndex = sqlLowerCase.lastIndexOf(" order ");
        String whereSql = null;
        if (orderbyIndex != -1) {
            whereSql = sql.substring(matcher.end(), orderbyIndex);
        } else {
            whereSql = sql.substring(matcher.end());
        }
        return getInteger(String.format("select count(*) from %s", whereSql), paramMap);
    }

    /**
     * 从List<D> result中返回一个值，如果为空就返回null
     *
     * @param result
     * @return
     * @author walden
     */
    public <D> D getSingleResult(List<D> result) {
        if (result == null || result.size() == 0) {
            return null;
        } else {
            if (result.size() > 1) logger.info(">>>>查询结果大于1!");
            return result.get(0);
        }
    }

    private void sqlExecuteLogger(CharSequence sql, Map<String, ?>[] batchValues) {
        logger.info(">>>> " + sql.toString());
        for (Map<String, ?> paramMap : batchValues)
            logger.info(">>>> " + paramMap);
    }

    private void sqlExecuteLogger(CharSequence sql, Map<String, ?> paramMap) {
        logger.info(">>>> " + sql.toString());
        logger.info(">>>> " + paramMap);
    }

    private void sqlExecuteLogger(CharSequence sql) {
        logger.info(">>>> " + sql.toString());
    }

}
