package com.cube.simple.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.cube.simple.model.Demo;

@Mapper
public interface DemoMapper {
	
	Long selectCount ();
	Demo selectById (Long id);
	List <Demo> selectAll ();
	void insert (Demo demo);
	void update (Demo demo);
	void delete (Long id);
}
