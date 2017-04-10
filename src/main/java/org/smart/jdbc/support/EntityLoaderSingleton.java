package org.smart.jdbc.support;

/**
 * 线程安全的单例的EntityLoader
 * @since 2014-10-23 下午1:07:26
 * @author walden
 */
public class EntityLoaderSingleton {
    
    private static class SingetonHolder {
        
        public final static EntityLoader instance = new EntityLoader();
    }
    
    public static EntityLoader getInstance() {
        return SingetonHolder.instance;
    }
}
