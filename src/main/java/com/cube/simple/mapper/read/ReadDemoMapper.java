package com.cube.simple.mapper.read;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.cube.simple.model.Demo;

@Mapper
@Repository
public interface ReadDemoMapper {
	
	Long selectCount ();
	Demo selectById (Long id);
	List <Demo> selectAll ();
}
