package org.walden.test;

import org.smart.jdbc.object.Page;


/**
 * @author walden
 * @since 2014-3-21 下午1:39:10
 */
public class TestPage {

    /**
     * @param args
     * @author walden
     */
    public static void main(String[] args) {
        test();
    }


    public static void test() {
        Page<String> page = new Page(5, 5);
        page.setPageIndexSize(5);
        page.setTotalCount(26);
        System.out.println(page.toString());
    }

}
