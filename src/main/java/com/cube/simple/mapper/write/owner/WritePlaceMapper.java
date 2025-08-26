package com.cube.simple.mapper.write.owner;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.cube.simple.model.owner.Place;

@Mapper
@Repository("writeOwnerPlaceMapper")
public interface WritePlaceMapper {
    
    void insert (Place place);
    void update (Place place);
    void deleteById (Long id);
}