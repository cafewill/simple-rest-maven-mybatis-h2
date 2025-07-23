package com.cube.simple.mapper.write;

import org.apache.ibatis.annotations.Mapper;

import com.cube.simple.model.Item;

@Mapper
public interface WriteItemMapper {
	
	void insert (Item item);
	void update (Item item);
	void deleteById (Long id);
}
