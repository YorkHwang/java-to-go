package com.york.java.to.go.domain;


/**
 * 参数
 */
public class JtgParm {
    private String java;

    /**
     * 指定包名
     */
    private String packageName;

    /**
     * 0-普通，1-hession2
     */
    private int structType = 0;

    public String getJava() {
        return java;
    }

    public void setJava(final String java) {
        this.java = java;
    }

    public int getStructType() {
        return structType;
    }

    public void setStructType(final int structType) {
        this.structType = structType;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }
}
