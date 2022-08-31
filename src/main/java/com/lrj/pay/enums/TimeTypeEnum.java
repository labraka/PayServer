package com.lrj.pay.enums;

/**
 * @ClassName: TimeTypeEnum
 * @Description: 请求时间类型参数
 * @Date: 2022/8/25 10:16
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
public enum TimeTypeEnum {
    DAY(1, "日"),
    WEEK(2, "周"),
    MONTH(3, "月"),
    YEAR(4, "年"),
    ;
    private Integer type;
    private String des;

    TimeTypeEnum(Integer type, String des) {
        this.type = type;
        this.des = des;
    }

    /**
     * 根据类型查询请求时间类型枚举
     *
     * @param type
     * @author: luorenjie
     * @date: 2022/8/31 14:27
     * @return: com.lrj.pay.enums.TimeTypeEnum
     */
    public static TimeTypeEnum getTimeType(Integer type) {
        TimeTypeEnum[] timeTypeEnums = TimeTypeEnum.values();
        for (TimeTypeEnum timeTypeEnum : timeTypeEnums) {
            if (timeTypeEnum.getType().equals(type)) {
                return timeTypeEnum;
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
