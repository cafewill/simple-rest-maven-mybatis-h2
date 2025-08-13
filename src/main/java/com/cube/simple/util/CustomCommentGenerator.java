package com.cube.simple.util;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.internal.DefaultCommentGenerator;
import org.mybatis.generator.internal.util.StringUtility;

public class CustomCommentGenerator extends DefaultCommentGenerator {

    /**
     * 모델의 각 필드(property)에 주석을 추가합니다.
     */
    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        // DB 코멘트를 가져옵니다.
        String remarks = introspectedColumn.getRemarks();

        // 코멘트가 있고, 비어 있지 않은 경우에만 Javadoc 주석을 추가합니다.
        if (StringUtility.stringHasValue(remarks)) {
            field.addJavaDocLine("/**");
            field.addJavaDocLine(" * " + remarks);
            field.addJavaDocLine(" */");
        }
    }
}