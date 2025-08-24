package com.cube.simple.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cube.simple.mapper.read.ReadRegionMapper;
import com.cube.simple.mapper.write.WriteRegionMapper;
import com.cube.simple.model.Region;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegionService {

    private final ReadRegionMapper readRegionMapper;
    private final WriteRegionMapper writeRegionMapper;

    public List<Region> selectAll() {
        return readRegionMapper.selectAll();
    }

    public Region selectById(Long id) {
        return readRegionMapper.selectById(id);
    }

    public Long count() {
        return readRegionMapper.selectCount();
    }

    public List<Region> selectByKo(String ko) {
        return readRegionMapper.selectByKo(ko);
    }

    @Transactional
    public int create(Region region) {
        return writeRegionMapper.insert(region);
    }

    @Transactional
    public int update(Region region) {
        return writeRegionMapper.update(region);
    }

    @Transactional
    public int delete(Long id) {
        return writeRegionMapper.deleteById(id);
    }
}
