package com.cube.simple.mapper.read;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.cube.simple.model.Device;

@Mapper
public interface ReadDeviceMapper {
    Device selectById(String id);
    List<Device> selectAll();
    Long selectCount();
} 