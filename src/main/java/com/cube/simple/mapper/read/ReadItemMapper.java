package com.cube.simple.mapper.read;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cube.simple.model.Item;

@Mapper
public interface ReadItemMapper {
	
	Long selectCount ();
	Item selectById (Long id);
	// List <Item> selectAll ();
    /**
     * @param category 카테고리 (null/빈문자열 → 조건 무시)
     * @param search   검색어 (null/빈문자열 → 조건 무시)
     * @param page     페이지 번호 (1부터 시작)
     * @param size     페이지당 조회 개수
     */
	List<Item> selectAll(
            @Param("page") int page,
            @Param("size") int size,
            @Param("category") String category,
            @Param("search") String search
        );	
}
