package com.cube.simple.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.cube.simple.model.Item;

@Mapper
public interface ItemMapper {
	
	Long selectCount ();
	Item selectById (Long id);
	List <Item> selectAll ();
	void insert (Item item);
	void update (Item item);
	void delete (Long id);
}
