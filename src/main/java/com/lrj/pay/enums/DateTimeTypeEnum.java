package com.lrj.pay.enums;

/**
 * @ClassName: DateTimeTypeEnum
 * @Description: 日期类型枚举
 * @Date: 2022/8/12 14:32
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
public enum DateTimeTypeEnum {
    MILLIS(0, "毫秒"),
    SECONDS(1, "秒"),
    MINUTES(2, "分"),
    HOUR(3, "时"),
    DAY(4, "日"),
    WEEK(5, "周"),
    MONTH(6, "月"),
    YEAR(7, "年"),
    ;
    private Integer type;
    private String des;

    DateTimeTypeEnum(Integer type, String des) {
        this.type = type;
        this.des = des;
    }

    /**
     * 根据类型查询日期类型枚举
     * @author: luorenjie
     * @date: 2022/8/31 14:22
     * @param type
     * @return: com.lrj.pay.enums.DateTimeTypeEnum
     */
    public static DateTimeTypeEnum getDateTimeType(Integer type) {
        DateTimeTypeEnum[] dateTimeTypeEnums = DateTimeTypeEnum.values();
        for (DateTimeTypeEnum dateTimeTypeEnum : dateTimeTypeEnums) {
            if (dateTimeTypeEnum.getType().equals(type)) {
                return dateTimeTypeEnum;
            }
        }
        return null;
    }

    public Integer getType() {
        return type;
    }

    public String getDes() {
        return des;
    }
}
