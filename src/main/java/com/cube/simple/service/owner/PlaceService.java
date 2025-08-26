package com.cube.simple.service.owner;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cube.simple.mapper.read.owner.ReadPlaceMapper;
import com.cube.simple.mapper.write.owner.WritePlaceMapper;
import com.cube.simple.model.owner.Place;

@Service("ownerPlaceService")
public class PlaceService {

    @Autowired
    private ReadPlaceMapper readPlaceMapper;
    
    @Autowired
    private WritePlaceMapper writePlaceMapper;

    @Transactional
    public void insert (Place place) {
        writePlaceMapper.insert (place);
    }

    @Transactional(readOnly = true)
    public List <Place> selectAll () {
        return readPlaceMapper.selectAll ();
    }

    @Transactional(readOnly = true)
    public Place selectById (Long id) {
        return readPlaceMapper.selectById (id);
    }
    
    @Transactional(readOnly = true)
    public Long selectCount () {
        return readPlaceMapper.selectCount ();
    }
    
    @Transactional
    public void update (Place place) {
        writePlaceMapper.update (place);
    }

    @Transactional
    public void deleteById (Long id) {
        writePlaceMapper.deleteById (id);
    }
}