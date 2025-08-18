package com.cube.simple.mapper.write;

import org.apache.ibatis.annotations.Mapper;

import com.cube.simple.model.Device;

@Mapper
public interface WriteDeviceMapper {
    void insert (Device device);
    void update (Device device);
    void deleteById (String id);
} 