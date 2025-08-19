package com.cube.simple.util;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.internal.DefaultCommentGenerator;
import org.mybatis.generator.internal.util.StringUtility;

public class CustomCommentGenerator extends DefaultCommentGenerator {

    private boolean addRemarkComments;

    @Override
    public void addConfigurationProperties(java.util.Properties properties) {
        super.addConfigurationProperties(properties);
        this.addRemarkComments = Boolean.parseBoolean(properties.getProperty("addRemarkComments", "false"));
    }

    @Override
    public void addModelClassComment(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (addRemarkComments && StringUtility.stringHasValue(introspectedTable.getRemarks())) {
            topLevelClass.addJavaDocLine("/**");
            topLevelClass.addJavaDocLine(" * Database Table Remarks:");
            topLevelClass.addJavaDocLine(" *   " + introspectedTable.getRemarks());
            topLevelClass.addJavaDocLine(" */");
        }
    }

    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        if (addRemarkComments && StringUtility.stringHasValue(introspectedColumn.getRemarks())) {
            field.addJavaDocLine("/**");
            field.addJavaDocLine(" * Database Column Remarks:");
            field.addJavaDocLine(" *   " + introspectedColumn.getRemarks());
            field.addJavaDocLine(" */");
        }
    }

    // 불필요한 메서드 주석 제거
    @Override
    public void addGeneralMethodComment(Method method, IntrospectedTable introspectedTable) { }
    @Override
    public void addGetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) { }
    @Override
    public void addSetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) { }
    @Override
    public void addComment(XmlElement xmlElement) { }
    @Override
    public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable, boolean markAsDoNotDelete) { }
}
