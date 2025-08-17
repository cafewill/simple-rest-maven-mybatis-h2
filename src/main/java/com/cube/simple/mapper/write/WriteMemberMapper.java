package com.cube.simple.mapper.write;

import org.apache.ibatis.annotations.Mapper;

import com.cube.simple.model.Member;

@Mapper
public interface WriteMemberMapper {
    void insert (Member member);
    void update (Member member);
    void deleteById (String id);
} 