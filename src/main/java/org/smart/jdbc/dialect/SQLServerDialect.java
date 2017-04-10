package org.smart.jdbc.dialect;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SQLServerDialect implements Dialect {
    
    private Log logger = LogFactory.getLog(getClass());
    
    private int getAfterSelectInsertPoint(String sql) {
        int selectIndex = sql.toLowerCase().indexOf("select");
        final int selectDistinctIndex = sql.toLowerCase().indexOf("select distinct");
        return selectIndex + (selectDistinctIndex == selectIndex ? 15 : 6);
    }
    
    /**
     * 这样使用的分页语句，有两个规定： 1，查询分页的语句一定要有order by 语句； 2，order by 语句中的字段不能带表的别名，一定是查询结果集合中唯一的[列名],[列的别名]或者[子查询的别名]； EG：
     * 
     * <pre>
     * select * from t_user tu ,t_role tr where tu.uid=tr.uuserid order by tu.uid,tr.uid desc 
     * 这样的语句是不允许的，因为order by中是包含了表的别名，需要改写为 
     * select tu.uid userid,tr.uid roleid, ... from t_user tu ,t_role tr where tu.uid=tr.uuserid 
     * order by userid,roleid desc
     * 下面是实现SQLServer优化分页的语句
     * select * from (
     *     select row_number () over (order by username) as rownum_,inner_.* from (
     *         select (select tu.cname from t_user tu where tu.uid=tol.uuserid) username,* from t_operate_log tol
     *     ) as inner_
     * ) as outer_ where rownum_>20 and rownum_<=40
     * </pre>
     */
    public String getLimitString(String sql, int offset, int limit) {
        String pagingSelect = null;
        if (offset == 0) {
            pagingSelect = new StringBuffer(sql.length() + 8).append(sql).insert(getAfterSelectInsertPoint(sql), " top " + limit).toString();
        } else {
            final String template = "select * from (select row_number () over (%s) as rownum_,inner_.* from (%s) as inner_) as outer_ where rownum_>%d and rownum_<=%d";
            int orderByIndex = sql.lastIndexOf("order by");
            if (orderByIndex == -1) {
                logger.info(">>>>" + sql);
                throw new UnsupportedOperationException("[SQLServer/SmartJdbc分页的SQL语句]必须要有order by语句!");
            }
            String sqlOrderBy = sql.substring(orderByIndex);
            if (sqlOrderBy.contains(".")) {
                logger.info(">>>>" + sql);
                throw new UnsupportedOperationException("[SQLServer/SmartJdbc分页的SQL语句]order by语句中必须不能包含\".\",order by语句请使用查询结果集合中唯一的[列名],[列的别名]或者[子查询的别名]，不用使用[表别名]!");
            }
            String sqlNonOrderBy = sql.substring(0, orderByIndex);
            pagingSelect = String.format(template, sqlOrderBy, sqlNonOrderBy, offset, offset + limit);
        }
        return pagingSelect;
    }
}
