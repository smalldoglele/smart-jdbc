package org.walden.test;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @since 2013-8-17 上午10:55:26
 * @author walden
 */
public class Test {
    
    private static final int EXECOUNT = 100000;
    
    public static void main(String[] args) {
        long begin, end;
        String sql = "select *, (select td.cname from t_dept td where td.uid=tt.udeptid and td.uid=:uid) as cdeptname," + "(select tu.cname from t_user tu where tu.uid=tt.uuserid) as cusername,"
                     + "(( select count(ttt.uid) from t_team_tmp ttt where ttt.uid = tt.uid) + (select count(tat.uid) from t_accountrp_tmp tat where tat.uteamid = tt.uid)) as itmpcount,"
                     + "(select tc.cname from t_customer tc where tc.uid=tt.ucustomerid) as customername"
                     + " from t_team tt  where exists (select uid from t_team where uid=tt.uid and uid=:uidsdx) order by (select * from t_team)";
        begin = System.currentTimeMillis();
        for (int i = 0; i < EXECOUNT; i++) {
            getCountSql1(sql, new HashMap<String, Object>());
        }
        end = System.currentTimeMillis();
        System.out.println("运行时间是" + (end - begin));
        
        begin = System.currentTimeMillis();
        for (int i = 0; i < EXECOUNT; i++) {
            getCountSql(sql, new HashMap<String, Object>());
        }
        end = System.currentTimeMillis();
        System.out.println("运行时间是" + (end - begin));
    }
    
    /**
     * @param sql
     * @author walden
     */
    private static void getCountSql1(String sql, Map<String, Object> paramMap) {
        StringBuffer buffer = new StringBuffer();
        Pattern pattern = Pattern.compile("select|from");
        Matcher matcher = pattern.matcher(sql);
        int blance = 0;
        while (matcher.find()) {
            String m = matcher.group();
            if ("select".equals(m)) {
                blance--;
            } else {
                blance++;
            }
            matcher.appendReplacement(buffer, m);
            if (blance == 0) break;
        }
        String selectFromSql = buffer.toString();
        String whereSql = sql.replace(selectFromSql, "");
        int orderbyIndex = whereSql.lastIndexOf("order");
        if (orderbyIndex != -1) {
            whereSql = whereSql.substring(0, orderbyIndex);
        }
        String result = "select count(*) from" + whereSql;
        // System.out.println(result);
    }
    
    /**
     * 这个函数用来测试运行时间
     * @param sql
     * @param paramMap
     * @Jesse Lu
     * @author walden
     */
    private static void getCountSql(String sql, Map<String, Object> paramMap) {
        Integer result = 0; // 最终需要返回的结果变量
        Integer fromIndex = 0; // 主查询的FROM关键字的起始位置变量
        // 匹配主查询SELECT后面所有成对以及嵌套一层子查询的SELECT/FROM直到第一个单独出现的FROM为止
        // ((.(?!\\bSELECT\\b|\\bFROM\\b))*.\\bSELECT\\b(.(?!\\bFROM\\b))*.\\bFROM\\b(.(?!\\bSELECT\\b|\\bFROM\\b))*)* 此为一对SELECT/FROM的匹配规则
        Pattern fromPattern = Pattern.compile("^\\s*\\bSELECT\\b((.(?!\\bSELECT\\b|\\bFROM\\b))*.\\bSELECT\\b((.(?!\\bSELECT\\b|\\bFROM\\b))*.\\bSELECT\\b(.(?!\\bFROM\\b))*.\\bFROM\\b(.(?!\\bSELECT\\b|\\bFROM\\b))*)*(.(?!\\bFROM\\b))*.\\bFROM\\b((.(?!\\bSELECT\\b|\\bFROM\\b))*.\\bSELECT\\b(.(?!\\bFROM\\b))*.\\bFROM\\b(.(?!\\bSELECT\\b|\\bFROM\\b))*)*(.(?!\\bSELECT\\b|\\bFROM\\b))*)*(.(?!\\bFROM\\b))*",
                                              Pattern.CASE_INSENSITIVE);
        Matcher fromMatcher = fromPattern.matcher(sql);
        if (fromMatcher.find()) {
            // 加上被替换的内容长度，以及后面紧跟着主查询FROM的1个字符。因为最后一个字符是(.(?!\\bFROM\\b))
            fromIndex += fromMatcher.group().length() + 1;
            Pattern namedParamPattern = Pattern.compile(":[\\w\\-]+"); // 匹配预处理名字的编译器
            String prefixSQL = sql.toString().substring(0, fromIndex);
            String suffixSQL = sql.toString().substring(fromIndex);
            Matcher namedParamMatcher = namedParamPattern.matcher(prefixSQL);
            Map<String, Object> removeMap = new HashMap<String, Object>(); // 存储需要移除的预处理参数
            while (namedParamMatcher.find()) { // 查找仅在FROM前出现过的预处理名字参数，并从paramMap中移除
                if (!suffixSQL.matches(".*" + namedParamMatcher.group() + "\\b.*")) {
                    String key = namedParamMatcher.group().substring(1);
                    removeMap.put(key, paramMap.get(key));
                    paramMap.remove(key);
                }
            }
            // 去除主查询的ORDER BY。为简单处理，本处未考虑ORDER BY使用预处理参数的情况
            // 匹配主查询ORDER BY的编译器[规则：从ORDER BY到行尾中可含成对括号且不含后半个括号的内容，成对括号中支持最多嵌套3层括号]
            // (([^\\(\\)]*\\([^\\(\\)]*\\))*[^\\)]*)* 此为成对括号的正则，在其中，对其自身做了三次递归嵌套
            Pattern orderByPattern = Pattern.compile("\\bORDER\\s+BY\\b([^\\(\\)]*\\((([^\\(\\)]*\\((([^\\(\\)]*\\([^\\(\\)]*\\))*[^\\)]*)*\\))*[^\\)]*)*\\))*[^\\)]*$", Pattern.CASE_INSENSITIVE);
            Matcher orderByMatcher = orderByPattern.matcher(suffixSQL);
            if (orderByMatcher.find()) {
                suffixSQL = suffixSQL.substring(0, orderByMatcher.start());
            }
            // System.out.println("SELECT COUNT(1) " + suffixSQL);
        }
    }
}
