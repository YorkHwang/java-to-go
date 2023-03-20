package com.york.java.to.go.service;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.york.java.to.go.domain.GoCodeResult;
import com.york.java.to.go.domain.JtgParm;
import com.york.java.to.go.domain.StructType;
import com.york.java.to.go.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;


/**
 * @author York.Hwang
 */

public class JavaConvertToGo {


    public static String upperFist(String src) {
        return src.substring(0, 1).toUpperCase() + src.substring(1);
    }

    public static String lowerFist(String src) {
        return src.substring(0, 1).toLowerCase() + src.substring(1);
    }

    public static String baseType2Go(String javaType) {
        switch (javaType) {
            case "String":
            case "string":
                return "string";
            case "Integer":
            case "int":
                return "int32";
            case "Long":
            case "long":
                return "int64";
            case "Boolean":
            case "boolean":
                return "bool";
            case "Float":
            case "float":
                return "float32";
            case "Double":
            case "double":
                return "float64";
            case "byte":
            case "Byte":
                return "byte";
            default:
                return null;
        }
    }

    public static String baseType2GoDefault(String javaType) {
        final String baseType = baseType2Go(javaType);
        if (baseType != null) {
            return baseType;
        }

        return javaType;
    }


    public static String javaType2Go(String javaType) {
        final String baseType = baseType2Go(javaType);
        if (baseType != null) {
            return baseType;
        }

        return typeFilter(javaType);

    }


    public static String typeFilter(final String javaType) {
        int listIndex = javaType.indexOf("List<");
        if (listIndex == 0) {
            //[]listType
            return "[]" + baseType2GoDefault(javaType.substring(5, javaType.length() - 1));
        }
        int napIndex = javaType.indexOf("Map<");
        if (napIndex == 0) {
            //map[keyType]valueType
            int dotIndex = javaType.indexOf(",");
            return "map[" + baseType2GoDefault(javaType.substring(4, dotIndex)) + "]" +
                    baseType2GoDefault(javaType.substring(dotIndex + 1, javaType.length() - 1));
        }

        return javaType;
    }

    public static String read(String filename) throws IOException {
        File file = new File(filename);
        byte[] b = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(b);
        return new String(b);
    }

    public static GoCodeResult parseToGo(final JtgParm jtgParm) {

        if (Utils.isBlank(jtgParm.getJava())) {
            final GoCodeResult goCodeResult = new GoCodeResult();
            goCodeResult.setGoCode("please select one java code");
            return goCodeResult;
        }
        final boolean hession2 = StructType.isHession2(jtgParm.getStructType());
        final StringBuilder goCode = new StringBuilder();
        CompilationUnit cu = StaticJavaParser.parse(jtgParm.getJava());
        String packageName;
        String fullPackageName = "unknown";
        if (cu.getPackageDeclaration().isPresent()) {
            packageName = Utils.isBlank(jtgParm.getPackageName())
                    ? cu.getPackageDeclaration().get().getName().getIdentifier()
                    : jtgParm.getPackageName();
            goCode.append("package ").append(packageName).append("\r\n\r\n");

            fullPackageName = cu.getPackageDeclaration().get().getName().asString();

        } else {
            packageName = Utils.isBlank(jtgParm.getPackageName())
                    ? "unknown"
                    : jtgParm.getPackageName();
            goCode.append("package ").append(packageName).append("\r\n\r\n");
        }

        if (hession2) {
            goCode.append("import hessian \"github.com/apache/dubbo-go-hessian2\"\r\n\r\n");
        }

        // package
        final NodeList<com.github.javaparser.ast.body.TypeDeclaration<?>> types = cu.getTypes();
        if (types == null || types.isEmpty()) {
            final GoCodeResult goCodeResult = new GoCodeResult();
            goCodeResult.setGoCode(goCode.toString());
            goCodeResult.setPackageName("unknown");
            goCodeResult.setStructName("no struct");
            return goCodeResult;
        }

        final String typeName = types.get(0).getName().getIdentifier();
        for (final TypeDeclaration type : types) {
            if (!type.isClassOrInterfaceDeclaration()) {
                continue;
            }
            final ClassOrInterfaceDeclaration interfaceDeclaration = (ClassOrInterfaceDeclaration) type;
            if (interfaceDeclaration.isInterface()) {
                buildInterface(goCode, interfaceDeclaration);
            } else {
                buildStruct(hession2, goCode, fullPackageName, interfaceDeclaration);
            }
        }


        final GoCodeResult goCodeResult = new GoCodeResult();
        goCodeResult.setGoCode(goCode.toString());
        goCodeResult.setPackageName(packageName);
        goCodeResult.setStructName(typeName);

        return goCodeResult;

    }

    private static void buildStruct(boolean hession2,
                                    StringBuilder goCode,
                                    String fullPackageName,
                                    final ClassOrInterfaceDeclaration type) {
        final String typeName = type.getName().getIdentifier();
        goCode.append("type ").append(typeName).append(" struct{");


        for (final Object member : type.getMembers()) {
            //field
            if (member instanceof com.github.javaparser.ast.body.FieldDeclaration) {
                final FieldDeclaration field = (FieldDeclaration) member;
                VariableDeclarator variable = field.getVariables().get(0);
                final String javaFiledName = variable.getName().getIdentifier();
                if ("serialVersionUID".equals(javaFiledName)) {
                    continue;
                }
                final String hessian = hession2 ? "    `hessian:\"" + javaFiledName + "\"`" : "";
                final String filedName = upperFist(javaFiledName);

                goCode.append("  \r\n").append(filedName).append(" ").append(javaType2Go(variable.getType().asString())).append(hessian);
            }

        }

        // 结构体字段结束
        goCode.append("\r\n}\r\n");

        for (final Object member : type.getMembers()) {
            //method
            if (member instanceof MethodDeclaration) {
                final MethodDeclaration method = (MethodDeclaration) member;

                // 方法关键词+名称
                goCode.append("\r\nfunc (").append(lowerFist(typeName))
                        .append(" *").append(typeName).append(") ").append(method.getName())
                ;

                // 方法参数
                int cnt = 0;
                goCode.append("(");
                for (Iterator it = method.getParameters().iterator(); it.hasNext(); ) {
                    if (cnt++ != 0) {
                        goCode.append(",");
                    }
                    final Parameter v = (Parameter) it.next();
                    goCode.append(v.getName().getIdentifier()).append(" ").append(javaType2Go(v.getType().asString()));
                }
                goCode.append(")");

                // 方法返回值
                if (method.getType() != null && "void".equals(method.getType().asString())) {
                    goCode.append(" error {\n\r}\r\n");
                } else {
                    // 方法返回值
                    goCode.append("(").append(method.getType() == null ? "" : javaType2Go(method.getType().asString()) + ",").append("error)" +
                            "{\r\n}\r\n");
                }
            }
        }

        if (hession2) {
            // JavaClassName
            goCode.append("\r\nfunc (").append(lowerFist(typeName))
                    .append(" *").append(typeName).append(") ").append("JavaClassName() string {\r\n")
                    .append("   return ").append("\"").append(fullPackageName).append(".").append(typeName).append("\"")
                    .append("\r\n} ");

            // 注册POJO
            goCode.append("\r\nfunc init() ").append("{\r\n")
                    .append("   hessian.RegisterPOJO(&").append(typeName).append("{})")
                    .append("\r\n} ");
        }

    }


    private static void buildInterface(final StringBuilder goCode,
                                       final ClassOrInterfaceDeclaration type) {
        final String typeName = type.getName().getIdentifier();
        goCode.append("type ").append(typeName).append(" interface {");
        for (final Object member : type.getMembers()) {
            //method
            if (member instanceof MethodDeclaration) {
                final MethodDeclaration method = (MethodDeclaration) member;

                // 方法关键词+名称
                goCode.append("\r\n").append(upperFist(method.getName().getIdentifier()));

                // 方法的参数
                int parameterCount = 0;
                goCode.append("(");
                for (Iterator it = method.getParameters().iterator(); it.hasNext(); ) {
                    if (parameterCount++ > 0) {
                        goCode.append(",");
                    }
                    final Parameter v = (Parameter) it.next();
                    goCode.append(v.getName().getIdentifier()).append(" ").append(javaType2Go(v.getType().asString()));
                }
                goCode.append(")");

                // 方法返回值
                if (method.getType() != null && "void".equals(method.getType().asString())) {
                    goCode.append(" error\r\n");
                } else {
                    goCode.append("(").append(method.getType() == null ? "" : javaType2Go(method.getType().asString()) + ",").append("error)\r\n");
                }
            }
        }

        // 结构体字段结束
        goCode.append("\r\n}\r\n");

    }

}
