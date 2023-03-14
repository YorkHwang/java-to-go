package com.york.java.to.go.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.york.java.to.go.domain.JtgParm;
import com.york.java.to.go.domain.GoCodeResult;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;


/**
 * @author York.Hwang
 */

public class JavaToGo {


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
        if(StringUtils.isBlank(jtgParm.getJava())) {
            final GoCodeResult goCodeResult = new GoCodeResult();
            goCodeResult.setGoCode("选中java class类代码");
            return goCodeResult;
        }
        final boolean hession2 = StructType.isHession2(jtgParm.getStructType());
        final StringBuilder goCode = new StringBuilder();
        //创建解析器
        final ASTParser parser = ASTParser.newParser(AST.JLS3);
        //设定解析器的源代码字符
        parser.setSource(jtgParm.getJava().toCharArray());
        //使用解析器进行解析并返回AST上下文结果(CompilationUnit为根节点)
        final CompilationUnit result = (CompilationUnit) parser.createAST(null);

        //获取类型
        final List types = result.types();
        //取得类型声明
        TypeDeclaration typeDec = (TypeDeclaration) types.get(0);
        //取得包名
        PackageDeclaration packetDec = result.getPackage();
        //取得类名
        String className = typeDec.getName().toString();
        //取得函数(Method)声明列表
        MethodDeclaration methodDec[] = typeDec.getMethods();
        //取得函数(Field)声明列表
        FieldDeclaration fieldDec[] = typeDec.getFields();

        final String packageName = StringUtils.isEmpty(jtgParm.getPackageName())
            ?pickPackageName(packetDec.getName().toString()):jtgParm.getPackageName();

        goCode.append("package ").append(packageName).append("\r\n\r\n");
        if (hession2) {
            goCode.append("import hessian \"github.com/apache/dubbo-go-hessian2\"\r\n\r\n");
        }
        goCode.append("type ").append(className).append(" struct{");

        for (final FieldDeclaration fieldDecEle : fieldDec) {
            final VariableDeclarationFragment frag = (VariableDeclarationFragment) fieldDecEle.fragments().get(0);
            final String javaFiledName = frag.getName().toString();
            if ("serialVersionUID".equals(javaFiledName)) {
                continue;
            }
            final String hessian = hession2 ? "   `hessian:\"" + javaFiledName + "\"`" : "";
            final String filedName = upperFist(javaFiledName);
            goCode.append("  \r\n").append(filedName).append(" ").append(javaType2Go(fieldDecEle.getType().toString())).append(hessian);
        }

        // 结构体结束
        goCode.append("\r\n}\r\n");


        //方法
        for (final MethodDeclaration method : methodDec) {
            // 方法关键词+名称
            goCode.append("\r\nfunc (").append(lowerFist(className))
                .append(" *").append(className).append(") ").append(method.getName())
            ;

            // 方法参数
            int cnt = 0;
            for (Iterator it = method.parameters().iterator(); it.hasNext(); ) {
                if (cnt++ == 0) {
                    goCode.append("(");
                } else {
                    goCode.append(",");
                }
                final SingleVariableDeclaration v = (SingleVariableDeclaration) it.next();
                goCode.append(v.getName()).append(" ").append(javaType2Go(v.getType().toString()));
            }
            if (cnt > 0) {
                goCode.append(")");
            }

            // 方法返回值
            goCode.append("(").append(method.getReturnType2() == null ? "" : javaType2Go(method.getReturnType2().toString()) + ",").append("error)" +
                "{\r\n}\r\n");
        }

        if (hession2) {
            // JavaClassName
            goCode.append("\r\nfunc (").append(lowerFist(className))
                .append(" *").append(className).append(") ").append("JavaClassName() string {\r\n")
                .append("   return ").append("\"").append(packetDec.getName()).append(".").append(className).append("\"")
                .append("\r\n} ");

            // 注册POJO
            goCode.append("\r\nfunc init() ").append("{\r\n")
                .append("   hessian.RegisterPOJO(&").append(className).append("{})")
                .append("\r\n} ");
        }

        final GoCodeResult goCodeResult = new GoCodeResult();
        goCodeResult.setGoCode(goCode.toString());
        goCodeResult.setPackageName(packageName);
        goCodeResult.setStructName(className);

        return goCodeResult;
    }

    private static String pickPackageName(final String javaPackageName) {
        if(javaPackageName.lastIndexOf(".") > 0) {
            return javaPackageName.substring(javaPackageName.lastIndexOf(".")+1);
        }
        return javaPackageName;
    }
}
