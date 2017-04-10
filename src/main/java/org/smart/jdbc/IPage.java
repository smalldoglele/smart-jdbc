package org.smart.jdbc;

import java.io.Serializable;
import java.util.List;

/**
 * 分页接口类
 *
 * @author walden
 * @since 2013-10-15 下午4:13:56
 */
public interface IPage<T extends Serializable> {

    /**
     * 获得SQL的startRow
     *
     * @return
     */
    int getStartRow();

    /**
     * 获得当前页数
     *
     * @return
     */
    int getCurrentPage();

    /**
     * 获得SQL的pageSize
     */
    int getPageSize();

    /**
     * 设置数据的总条数 注意：smart框架在设置了总条数后，要重新初始化分页，即重新计算一下。
     *
     * @param totalCount
     */
    void setTotalCount(int totalCount);

    /**
     * 获得数据的总条数
     *
     * @return
     */
    int getTotalCount();

    /**
     * 设置分页的结果集
     *
     * @param result
     */
    void setResults(List<T> result);

    /**
     * 获得分页的结果集
     */
    List<T> getResults();

    /**
     * 获得分页索引数组
     *
     * @author walden
     */
    int[] getPageIndex();

    /**
     * 获得总页数
     */
    int getTotalPage();
}
