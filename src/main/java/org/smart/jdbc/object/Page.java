package org.smart.jdbc.object;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.smart.jdbc.IPage;

/**
 * @author walden
 * @since 2013-10-15 下午4:48:22
 */
public class Page<T extends Serializable> implements IPage<T> {

    protected int startRow;// 开始行数

    protected int pageSize = 20;// 分页条数

    protected int totalCount;// 总条数

    protected int totalPage;// 总页数

    protected int currentPage;// 当前页

    protected int pageIndexSize = 9;// 当前页显示的索引数组的长度

    protected int[] pageIndex;// 当前页显示的索引数组

    protected int prevPage;// 上一页

    protected int nextPage;// 下一页

    protected List<T> results;// 结果集

    /**
     * 设置当前页面构造函数
     *
     * @param currentPage
     * @author walden
     */
    public Page(int currentPage) {
        this.currentPage = currentPage;
    }

    /**
     * 设置当前页面和分页长度构造函数
     *
     * @param pageSize
     * @param currentPage
     * @author walden
     */
    public Page(int pageSize, int currentPage) {
        this.pageSize = pageSize;
        this.currentPage = currentPage;
    }

    /**
     * @param pageSize
     * @param totalCount
     * @param currentPage
     * @author walden
     */
    public Page(int pageSize, int currentPage, int totalCount) {
        this.pageSize = pageSize;
        this.currentPage = currentPage;
        this.totalCount = totalCount;
        initPage();
    }

    /**
     * 初始化分页信息
     *
     * @author walden
     */
    private void initPage() {
        // 初始化分页条数
        pageSize = (pageSize <= 1 ? 1 : pageSize);
        // 初始化总页数
        totalPage = totalCount / pageSize + (totalCount % pageSize == 0 ? 0 : 1);
        totalPage = totalPage == 0 ? 1 : totalPage;
        // 初始化验证当前页
        if (currentPage < 1) currentPage = 1;
        if (currentPage > totalPage) currentPage = totalPage;
        // 初始化SQL起始列
        startRow = (currentPage - 1) * pageSize;
        // 初始化上一页
        prevPage = (currentPage == 1 ? 1 : currentPage - 1);
        // 初始化下一页
        nextPage = (currentPage == totalPage ? totalPage : currentPage + 1);
        // 初始化分页的索引块
        initPageIndex();
    }

    /**
     * 初始化分页的索引块
     *
     * @return
     * @author walden
     */
    public int[] initPageIndex() {
        int startPoint = 1, endPoint = pageIndexSize, halfSize = pageIndexSize / 2;
        //if 当分页索引块大小大于总页数时候 else 当分页索引块大小小于等于总页数
        if (pageIndexSize < totalPage) {
            if (currentPage <= halfSize) {
                startPoint = 1;
                endPoint = pageIndexSize;
            } else if (currentPage > totalPage - halfSize) {
                startPoint = totalPage - pageIndexSize+1;
                endPoint = totalPage;
            } else {
                startPoint = currentPage - halfSize;
                endPoint = currentPage + halfSize;
            }
        } else {
            startPoint = 1;
            endPoint = totalPage;
        }

        int length = endPoint - startPoint + 1;
        pageIndex = new int[length];
        for (int i = 0; i < length; i++) {
            pageIndex[i] = startPoint++;
        }
        return pageIndex;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        initPage();
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageIndexSize() {
        return pageIndexSize;
    }

    public void setPageIndexSize(int pageIndexSize) {
        this.pageIndexSize = pageIndexSize;
    }

    public void setPageIndex(int[] pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPrevPage() {
        return prevPage;
    }

    public void setPrevPage(int prevPage) {
        this.prevPage = prevPage;
    }

    public int getNextPage() {
        return nextPage;
    }

    public void setNextPage(int nextPage) {
        this.nextPage = nextPage;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }

    public int[] getPageIndex() {
        return pageIndex;
    }

    @Override
    public String toString() {
        return "Page [startRow=" + startRow + ", pageSize=" + pageSize + ", totalCount=" + totalCount + ", totalPage=" + totalPage + ", currentPage=" + currentPage + ", pageIndexSize="
                + pageIndexSize + ", pageIndex=" + Arrays.toString(pageIndex) + ", prevPage=" + prevPage + ", nextPage=" + nextPage + ", results=" + results + "]";
    }

}
