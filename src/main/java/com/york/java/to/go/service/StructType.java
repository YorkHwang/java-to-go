package com.york.java.to.go.service;

/**
 * @Description: 结构体类型
 */
public enum StructType {
    /* 删除状态码 */
    COMMON(0, "普通"),
    /* 正常状态码 */
    HESSION2(1, "hession2");

    private int val;
    private String name;

    StructType(int val, String name) {
        this.val = val;
        this.name = name;
    }

    public static boolean isHession2(int val) {
        return HESSION2.val == val;
    }

    public int getVal() {
        return val;
    }

    public String getName() {
        return name;
    }
}
