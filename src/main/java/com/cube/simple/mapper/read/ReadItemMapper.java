package com.cube.simple.mapper.read;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.cube.simple.model.Item;

@Mapper
@Repository
public interface ReadItemMapper {
	
	Long selectCount ();
	Item selectById (Long id);
	List <Item> selectAll ();
}
