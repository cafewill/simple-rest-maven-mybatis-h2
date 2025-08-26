package com.cube.simple.mapper.read.owner;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.cube.simple.model.owner.Place;

@Mapper
@Repository("readOwnerPlaceMapper")
public interface ReadPlaceMapper {
    
    Long selectCount ();
    Place selectById (Long id);
    List <Place> selectAll ();
}