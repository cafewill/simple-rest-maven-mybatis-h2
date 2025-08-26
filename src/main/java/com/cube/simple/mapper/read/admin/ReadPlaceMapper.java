package com.cube.simple.mapper.read.admin;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.cube.simple.model.admin.Place;

@Mapper
@Repository("readAdminPlaceMapper")
public interface ReadPlaceMapper {
    
    Long selectCount ();
    Place selectById (Long id);
    List <Place> selectAll ();
}