package com.cube.simple.mapper.write;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.cube.simple.model.Item;

@Mapper
@Repository
public interface WriteItemMapper {
	
	void insert (Item item);
	void update (Item item);
	void deleteById (Long id);
}
