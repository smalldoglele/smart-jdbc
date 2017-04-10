package org.smart.jdbc.dialect;

public class MySQLDialect implements Dialect {
    
    /**
     * select * from your_table where ... limit 10 offset 20 这个分页语句目前可以使用的数据库有MySQL/SQLite/PostgreSQL等数据库
     */
    public String getLimitString(String query, int offset, int limit) {
        return new StringBuffer(query.length() + 20).append(query).append(String.format(" limit %s offset %s", limit, offset)).toString();
    }
    
}
