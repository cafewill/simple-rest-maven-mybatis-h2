package com.cube.simple.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cube.simple.aspect.AESDecrypt;
import com.cube.simple.mapper.read.ReadMemberMapper;
import com.cube.simple.model.Member;

@Service
public class AuthService {

	@Autowired
	private ReadMemberMapper readMemberMapper;
	
    @AESDecrypt
    @Transactional(readOnly = true)
	public Member selectById (String id) {
		return readMemberMapper.selectById (id);
	}
}
