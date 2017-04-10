package org.smart.jdbc.object;

import java.io.Serializable;

/**
 * 如果一个表没有主键，在对应的DAO类型参数设置成该类，这里类只是起到一个标志作用
 * @since 2014-4-12 下午1:46:10
 * @author walden
 */
public class NonId implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
}
