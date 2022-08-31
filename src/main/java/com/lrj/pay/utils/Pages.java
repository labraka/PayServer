package com.lrj.pay.utils;

import lombok.Data;

/**
 * @ClassName: Pages
 * @Description: 分页对象
 * @Date: 2022/8/29 16:25
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
@Data
public class Pages {
    /**
     * 分页查询开始记录位置.
     */
    private int begin;
    /**
     * 分页查看下结束位置.
     */
    private int end;
    /**
     * 当前页码.
     */
    private int pageNo;
    /**
     * 每页显示记录数.
     */
    private int size = 20;
    /**
     * 查询结果总记录数.
     */
    private int totalRecords;
    /**
     * 总共页数.
     */
    private int pageCount;

    public Pages(int pageNo, int size) {
        this.pageNo = pageNo;
        pageNo = pageNo > 0 ? pageNo : 1;
        this.size = size;
        this.begin = this.size * (pageNo - 1);
        this.end = this.size * pageNo;
    }

    public Pages() {

    }

    public void setBegin(int begin) {
        this.begin = begin;
        if (this.size != 0) {
            this.pageNo = (int) Math.floor((this.begin * 1.0d) / this.size) + 1;
        }
    }

//    public void setSize(int length) {
//        this.size = length;
//        if (this.begin != 0) {
//            this.begin = length * (this.pageNo  - 1);
//            this.end = length * this.pageNo ;
//            this.pageNo = (int) Math.floor((this.begin * 1.0d) / this.size) + 1;
//        }
//    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
        this.pageCount = (int) Math.floor((this.totalRecords * 1.0d) / this.size);
        if (this.totalRecords % this.size != 0) {
            this.pageCount++;
        }
    }
}
