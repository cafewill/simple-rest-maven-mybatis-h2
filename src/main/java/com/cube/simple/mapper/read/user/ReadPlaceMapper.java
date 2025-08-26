package com.cube.simple.mapper.read.user;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.cube.simple.model.user.Place;

@Mapper
@Repository("readUserPlaceMapper")
public interface ReadPlaceMapper {
    List<Place> selectAll(@Param("lang") String lang);
    Place selectById(@Param("lang") String lang, @Param("id") Long id);
    Long selectCount();
}