package org.smart.jdbc.dialect;

public interface Dialect {
    
    /**
     * 根据传入的SQL封装成查询分页的SQL
     * @param query 查询SQL
     * @param offset start row 开始行(不含)
     * @param limit page size 分页的大小
     * @return 分页查询SQL
     * @author walden
     */
    public String getLimitString(String query, int offset, int limit);
    
}
