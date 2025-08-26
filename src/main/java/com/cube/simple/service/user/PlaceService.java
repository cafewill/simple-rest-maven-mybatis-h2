package com.cube.simple.service.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cube.simple.mapper.read.user.ReadPlaceMapper;
import com.cube.simple.mapper.write.user.WritePlaceMapper;
import com.cube.simple.model.user.Place;

@Service("userPlaceService")
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
    public List <Place> selectAll (String lang) {
        return readPlaceMapper.selectAll (lang);
    }

    @Transactional(readOnly = true)
    public Place selectById (String lang, Long id) {
        return readPlaceMapper.selectById (lang, id);
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