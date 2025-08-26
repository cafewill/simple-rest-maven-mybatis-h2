// com.cube.simple.handler.mybatis.JsonI18NHandler
package com.cube.simple.handler.mybatis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import org.apache.ibatis.type.*;

import java.sql.*;
import java.util.Map;

@MappedTypes(Map.class)
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.CLOB, JdbcType.LONGVARCHAR, JdbcType.OTHER})
public class JsonI18NHandler extends BaseTypeHandler<Map<String, String>> {

    private static final ObjectMapper om = new ObjectMapper();
    private static final TypeReference<Map<String, String>> TYPE =
            new TypeReference<Map<String, String>>() {};

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i,
                                    Map<String, String> parameter, JdbcType jdbcType) throws SQLException {
        try {
            String json = om.writeValueAsString(parameter);
            ps.setString(i, json); // H2/MySQL 모두 안전
        } catch (Exception e) {
            throw new SQLException("JSON serialize failed", e);
        }
    }

    @Override
    public Map<String, String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public Map<String, String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public Map<String, String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    private Map<String, String> parse(String s) throws SQLException {
        if (s == null) return null;
        String text = s.trim();
        if (text.isEmpty()) return null;
        try {
            // 1) 먼저 JSON 노드로 읽어서 토큰 타입 확인
            JsonNode node = om.readTree(text);

            // 1-a) "…"(문자열)로 감싸져 있으면 내부 문자열을 다시 객체로 파싱
            if (node.isTextual()) {
                String inner = node.asText(); // 따옴표 해제 + escape 해제
                return om.readValue(inner, TYPE);
            }

            // 1-b) {…} 객체면 그대로 Map으로 변환
            if (node.isObject()) {
                return om.convertValue(node, TYPE);
            }

            throw new SQLException("Invalid JSON for Region.name: expected object or string, got " + node.getNodeType());
        } catch (Exception e) {
            throw new SQLException("Error parsing JSON string to Map", e);
        }
    }
}
