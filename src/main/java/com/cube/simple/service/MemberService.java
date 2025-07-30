package com.cube.simple.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.cube.simple.mapper.read.ReadMemberMapper;
import com.cube.simple.mapper.write.WriteMemberMapper;
import com.cube.simple.model.Member;
import com.cube.simple.util.AESUtil;

@Service
public class MemberService {

    @Autowired
    private AESUtil aesUtil;

	@Autowired
	private ReadMemberMapper readMemberMapper;
	
	@Autowired
	private WriteMemberMapper writeMemberMapper;

	@Transactional
	public void insert (Member member) {
		member.setName(aesUtil.encrypt(member.getName()));
		writeMemberMapper.insert (member);
	}

    @Transactional(readOnly = true)
	public List <Member> selectAll () {
        List<Member> members = readMemberMapper.selectAll();
        if (CollectionUtils.isEmpty(members)) {
            return Collections.emptyList();
        }
        members.forEach(this::decryptName);
        return members;
	}

    @Transactional(readOnly = true)
	public Member selectById (String id) {
    	Member member = readMemberMapper.selectById (id);
		member.setName(aesUtil.decrypt(member.getName()));
		return member;
	}
	
    @Transactional(readOnly = true)
	public Long selectCount () {
		return readMemberMapper.selectCount ();
	}
	
	@Transactional
	public void update (Member member) {
	    if (StringUtils.hasText(member.getName())) {
	        member.setName(aesUtil.encrypt(member.getName()));
	    }
		writeMemberMapper.update (member);
	}

	@Transactional
	public void deleteById (String id) {
		writeMemberMapper.deleteById (id);
	}
	
	private void decryptName(Member member) {
	    if (Objects.nonNull(member) && Objects.nonNull(member.getName())) {
	        member.setName(aesUtil.decrypt(member.getName()));
	    }
	}	
}
