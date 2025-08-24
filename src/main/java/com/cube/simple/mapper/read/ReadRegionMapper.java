package com.cube.simple.mapper.read;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cube.simple.model.Region;

@Mapper
public interface ReadRegionMapper {

    List<Region> selectAll();

    Region selectById(@Param("id") Long id);

    Long selectCount();

    /** ko 필드로 조회 (H2 MySQL 모드/운영 MySQL에서 동작) */
    List<Region> selectByKo(@Param("ko") String ko);
}
