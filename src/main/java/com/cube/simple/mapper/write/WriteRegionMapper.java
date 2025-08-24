package com.cube.simple.mapper.write;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cube.simple.model.Region;

@Mapper
public interface WriteRegionMapper {

    int insert(Region region);

    int update(Region region);

    int deleteById(@Param("id") Long id);
}
