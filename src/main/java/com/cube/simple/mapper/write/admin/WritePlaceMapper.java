package com.cube.simple.mapper.write.admin;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.cube.simple.model.admin.Place;

@Mapper
@Repository("writeAdminPlaceMapper")
public interface WritePlaceMapper {
    
    void insert (Place place);
    void update (Place place);
    void deleteById (Long id);
}