package org.smart.jdbc.support;

import org.smart.jdbc.dialect.Dialect;
import org.smart.jdbc.dialect.MySQLDialect;
import org.smart.jdbc.dialect.OracleDialect;
import org.smart.jdbc.dialect.SQLServerDialect;

public class DialectFactory {
    
    public static Dialect getDialect(String dialect) {
        Dialect dlt = null;
        String dialectLower = dialect.toLowerCase();
        if ("db2".equals(dialectLower) || "sqlserver".equals(dialectLower)) {
            dlt = new SQLServerDialect();
        } else if ("postgresql".equals(dialectLower) || "mysql".equals(dialectLower) || "sqlite".equals(dialectLower)) {
            dlt = new MySQLDialect();
        } else if ("oracle".equals(dialectLower)) {
            dlt = new OracleDialect();
        }
        return dlt;
    }
}
