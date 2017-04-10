package org.smart.jdbc.object;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityManager {
    
    private Field idField;
    
    private String idColumnName;
    
    private TableDefined tableDefined;
    
    // 这个类里面的所有字段
    private List<String> fieldNameList = new ArrayList<String>();
    
    // 这个类@Column主机的column名字
    private List<String> columnNameList = new ArrayList<String>();
    
    // 命名参数字段 主要用于生成默认的insert
    private List<String> namedValueList = new ArrayList<String>();
    
    // 主要用于生成默认的update
    private List<String> updateSetItemList = new ArrayList<String>();
    
    // 类的域与数据表字段的对应映射
    private Map<Field, ColumnDefined> fieldColumnMap = new HashMap<Field, ColumnDefined>();
    
    private String insertDefaultSql;
    
    private String deleteDefaultSql;
    
    private String updateDefaultSql;
    
    private String selectDefaultSql;
    
    public String getListString(List<String> list) {
        if (list == null || list.size() == 0) {
            return null;
        } else {
            String listStr = list.toString();
            return listStr.substring(1, listStr.length() - 1);
        }
    }
    
    public Field getIdField() {
        return idField;
    }
    
    public void setIdField(Field idField) {
        this.idField = idField;
    }
    
    public String getIdColumnName() {
        return idColumnName;
    }
    
    public void setIdColumnName(String idColumnName) {
        this.idColumnName = idColumnName;
    }
    
    public TableDefined getTableDefined() {
        return tableDefined;
    }
    
    public void setTableDefined(TableDefined tableDefined) {
        this.tableDefined = tableDefined;
    }
    
    public List<String> getFieldNameList() {
        return fieldNameList;
    }
    
    public void setFieldNameList(List<String> fieldNameList) {
        this.fieldNameList = fieldNameList;
    }
    
    public List<String> getColumnNameList() {
        return columnNameList;
    }
    
    public void setColumnNameList(List<String> columnNameList) {
        this.columnNameList = columnNameList;
    }
    
    public List<String> getNamedValueList() {
        return namedValueList;
    }
    
    public void setNamedValueList(List<String> namedValueList) {
        this.namedValueList = namedValueList;
    }
    
    public List<String> getUpdateSetItemList() {
        return updateSetItemList;
    }
    
    public void setUpdateSetItemList(List<String> updateSetItemList) {
        this.updateSetItemList = updateSetItemList;
    }
    
    public Map<Field, ColumnDefined> getFieldColumnMap() {
        return fieldColumnMap;
    }
    
    public void setFieldColumnMap(Map<Field, ColumnDefined> fieldColumnMap) {
        this.fieldColumnMap = fieldColumnMap;
    }
    
    public String getInsertDefaultSql() {
        if (insertDefaultSql == null) {
            insertDefaultSql = buildInsertDefaultSql();
        }
        return insertDefaultSql;
    }
    
    /**
     * 创建插入语句
     * @return
     */
    private String buildInsertDefaultSql() {
        String sql = "insert into %s(%s) values(%s)";
        return String.format(sql, tableDefined.getName(), getListString(columnNameList), this.getListString(namedValueList));
    }
    
    public void setInsertDefaultSql(String insertDefaultSql) {
        this.insertDefaultSql = insertDefaultSql;
    }
    
    public String getDeleteDefaultSql() {
        if (deleteDefaultSql == null) {
            deleteDefaultSql = buildDeleteDefaultSql();
        }
        return deleteDefaultSql;
    }
    
    /**
     * 创建delete语句
     * @return
     */
    private String buildDeleteDefaultSql() {
        String sql = "delete from %s where %s=:%s";
        return String.format(sql, tableDefined.getName(), idColumnName, idColumnName);
    }
    
    public void setDeleteDefaultSql(String deleteDefaultSql) {
        this.deleteDefaultSql = deleteDefaultSql;
    }
    
    public String getUpdateDefaultSql() {
        if (updateDefaultSql == null) {
            updateDefaultSql = buildUpdateDefaultSql();
        }
        return updateDefaultSql;
    }
    
    /**
     * 创建update语句
     * @return
     */
    private String buildUpdateDefaultSql() {
        String sql = "update %s set %s where %s=:%s";
        return String.format(sql, tableDefined.getName(), getListString(updateSetItemList), idColumnName, idColumnName);
    }
    
    public void setUpdateDefaultSql(String updateDefaultSql) {
        this.updateDefaultSql = updateDefaultSql;
    }
    
    public String getSelectDefaultSql() {
        if (selectDefaultSql == null) {
            selectDefaultSql = buildSelectDefaultSql();
        }
        return selectDefaultSql;
    }
    
    private String buildSelectDefaultSql() {
        String sql = "select * from %s where %s=:%s";
        return String.format(sql, tableDefined.getName(), idColumnName, idColumnName);
    }
    
    public void setSelectDefaultSql(String selectDefaultSql) {
        this.selectDefaultSql = selectDefaultSql;
    }
    
}
