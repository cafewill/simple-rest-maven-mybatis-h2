package com.cube.simple.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cube.simple.mapper.DemoMapper;
import com.cube.simple.model.Demo;

@Service
public class DemoService {

	@Autowired
	DemoMapper demoMapper;
	
	@Transactional
	public void insert (Demo demo) {
		demoMapper.insert (demo);
	}

    @Transactional(readOnly = true)
	public List <Demo> selectAll () {
		return demoMapper.selectAll ();
	}

    @Transactional(readOnly = true)
	public Demo selectById (Long id) {
		return demoMapper.selectById (id);
	}
	
    @Transactional(readOnly = true)
	public Long selectCount () {
		return demoMapper.selectCount ();
	}
	
	@Transactional
	public void update (Demo demo) {
		demoMapper.update (demo);
	}

	@Transactional
	public void delete (Long id) {
		demoMapper.delete (id);
	}
}
