package com.cube.simple.mapper.read;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.cube.simple.model.Member;

@Mapper
public interface ReadMemberMapper {
    Member selectById(String id);
    List<Member> selectAll();
    Long selectCount();
} 