package com.cube.simple.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cube.simple.mapper.read.ReadMemberMapper;
import com.cube.simple.mapper.write.WriteMemberMapper;
import com.cube.simple.model.Member;

@Service
public class MemberService {

	@Autowired
	private ReadMemberMapper readMemberMapper;
	
	@Autowired
	private WriteMemberMapper writeMemberMapper;

	@Transactional
	public void insert (Member member) {
		writeMemberMapper.insert (member);
	}

    @Transactional(readOnly = true)
	public List <Member> selectAll () {
		return readMemberMapper.selectAll ();
	}

    @Transactional(readOnly = true)
	public Member selectById (String id) {
		return readMemberMapper.selectById (id);
	}
	
    @Transactional(readOnly = true)
	public Long selectCount () {
		return readMemberMapper.selectCount ();
	}
	
	@Transactional
	public void update (Member member) {
		writeMemberMapper.update (member);
	}

	@Transactional
	public void deleteById (String id) {
		writeMemberMapper.deleteById (id);
	}
}
