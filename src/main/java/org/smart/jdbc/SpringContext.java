package org.smart.jdbc;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 获得Spring的ApplicationContext或者WebApplicationContext
 * @since 2013-7-25 下午7:10:03
 * @author walden
 */
public class SpringContext {
    
    private static ApplicationContext applicationContext = null;
    
    /**
     * 根据web.xml中spring的配置文件生成springContext
     * @return
     */
    public static ApplicationContext getSpringContext() {
        if (applicationContext == null) {
            applicationContext = new ClassPathXmlApplicationContext("classpath*:spring/*.xml");
        }
        return applicationContext;
    }
    
    /**
     * 获得spring注册的bean
     * @param beanName
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName) {
        return (T) SpringContext.getSpringContext().getBean(beanName);
    }
    
}
