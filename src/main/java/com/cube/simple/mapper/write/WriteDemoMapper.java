package com.cube.simple.mapper.write;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.cube.simple.model.Demo;

@Mapper
@Repository
public interface WriteDemoMapper {
	
	void insert (Demo demo);
	void update (Demo demo);
	void deleteById (Long id);
}
