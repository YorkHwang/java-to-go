package com.york.java.to.go.domain;

import java.util.Objects;

public class GoCodeResult {

    private String goCode;
    private String packageName;
    private String structName;


    @Override
    public boolean equals(final Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        final GoCodeResult that = (GoCodeResult) o;
        return Objects.equals(goCode, that.goCode) && Objects.equals(packageName, that.packageName) &&
            Objects.equals(structName, that.structName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(goCode, packageName, structName);
    }

    public String getGoCode() {
        return goCode;
    }

    public void setGoCode(final String goCode) {
        this.goCode = goCode;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    public String getStructName() {
        return structName;
    }

    public void setStructName(final String structName) {
        this.structName = structName;
    }
}
