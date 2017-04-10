package org.smart.jdbc.dialect;

public class EmptyDialect implements Dialect {
    
    public String getLimitString(String query, int offset, int limit) {
        return null;
    }
    
}
