package com.cube.simple.mapper.write.user;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.cube.simple.model.user.Place;

@Mapper
@Repository("writeUserPlaceMapper")
public interface WritePlaceMapper {
    
    void insert (Place place);
    void update (Place place);
    void deleteById (Long id);
}