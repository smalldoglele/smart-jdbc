package org.smart.jdbc.support;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.smart.jdbc.ILoader;
import org.smart.jdbc.annotation.Column;
import org.smart.jdbc.annotation.Entity;
import org.smart.jdbc.annotation.Id;
import org.smart.jdbc.annotation.Table;
import org.smart.jdbc.annotation.Transient;
import org.smart.jdbc.annotation.UniqueConstraint;
import org.smart.jdbc.object.ColumnDefined;
import org.smart.jdbc.object.EntityManager;
import org.smart.jdbc.object.TableDefined;
import org.springframework.stereotype.Component;

public class EntityLoader implements ILoader<Class<?>, EntityManager> {
    
    private Log logger = LogFactory.getLog(EntityLoader.class);
    
    private Map<Class<?>, EntityManager> entityMap = new ConcurrentHashMap<Class<?>, EntityManager>();
    
    public void initMap() {}
    
    public void set(Class<?> klass, EntityManager entity) {
        this.entityMap.put(klass, entity);
    }
    
    public EntityManager load(Class<?> klass) {
        EntityManager entity = entityMap.get(klass);
        if (entity == null) {
            entity = loadEntityManager(klass);
        }
        return entity;
    }
    
    public Map<Class<?>, EntityManager> loadMap() {
        return this.entityMap;
    }
    
    public EntityManager loadEntityManager(Class<?> klass) {
        Entity entityAnnotation = klass.getAnnotation(Entity.class);
        EntityManager entityManager = new EntityManager();
        if (entityAnnotation != null) {
            Table tableAnnotation = klass.getAnnotation(Table.class);
            if (tableAnnotation != null) {
                TableDefined tableDefined = new TableDefined();
                tableDefined.setName(tableAnnotation.name());
                UniqueConstraint[] uniqueConstraints = tableAnnotation.uniqueConstraints();
                for (UniqueConstraint uc : uniqueConstraints) {
                    tableDefined.getUniqueConstraints().add(uc.columnNames());
                }
                entityManager.setTableDefined(tableDefined);
            } else {
                logger.info(klass.getName() + "没有被@Table注解!");
            }
            Field[] fields = klass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                entityManager.getFieldNameList().add(field.getName());
                if (field.getAnnotation(Transient.class) != null || field.getAnnotations().length == 0) {
                    continue;
                }
                
                Column columnAnnotation = field.getAnnotation(Column.class);
                ColumnDefined columnDefined = new ColumnDefined();
                if (columnAnnotation != null) {
                    String columnName = columnAnnotation.name();
                    columnDefined.setName(columnName);
                    columnDefined.setNullable(columnAnnotation.nullable());
                    columnDefined.setInsertable(columnAnnotation.insertable());
                    columnDefined.setUpdatable(columnAnnotation.updatable());
                    if (field.getAnnotation(Id.class) != null) {
                        entityManager.setIdField(field);
                        entityManager.setIdColumnName(columnName);
                    } else {// 默认的updateSQL 是按照Id来更新的所以UpdateSetItemList列表里面不用包含Id
                        entityManager.getUpdateSetItemList().add(columnName + "=:" + columnName);
                    }
                    entityManager.getColumnNameList().add(columnName);
                    entityManager.getNamedValueList().add(":" + columnName);
                    entityManager.getFieldColumnMap().put(field, columnDefined);
                }
            }
        } else {
            logger.info(klass.getName() + "没有被@Entity注解!");
        }
        this.entityMap.put(klass, entityManager);
        return entityManager;
    }
    
    /**
     * 根据默认的映射规则从类属性获得字段名字
     * @param field
     * @return
     */
    public static String getColumnNameFromField(Field field) {
        return field.getName();
    }
}
