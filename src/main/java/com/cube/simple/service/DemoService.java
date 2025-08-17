package com.cube.simple.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cube.simple.mapper.read.ReadDemoMapper;
import com.cube.simple.mapper.write.WriteDemoMapper;
import com.cube.simple.model.Demo;

@Service
public class DemoService {

	@Autowired
	private ReadDemoMapper readDemoMapper;
	
	@Autowired
	private WriteDemoMapper writeDemoMapper;

	@Transactional
	public void insert (Demo demo) {
		writeDemoMapper.insert (demo);
	}

    @Transactional(readOnly = true)
	public List <Demo> selectAll () {
		return readDemoMapper.selectAll ();
	}

    @Transactional(readOnly = true)
	public Demo selectById (Long id) {
		return readDemoMapper.selectById (id);
	}
	
    @Transactional(readOnly = true)
	public Long selectCount () {
		return readDemoMapper.selectCount ();
	}
	
	@Transactional
	public void update (Demo demo) {
		writeDemoMapper.update (demo);
	}

	@Transactional
	public void deleteById (Long id) {
		writeDemoMapper.deleteById (id);
	}
}
