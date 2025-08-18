package com.cube.simple.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cube.simple.aspect.AESDecrypt;
import com.cube.simple.aspect.AESEncrypt;
import com.cube.simple.aspect.SHAEncrypt;
import com.cube.simple.mapper.read.ReadDeviceMapper;
import com.cube.simple.mapper.write.WriteDeviceMapper;
import com.cube.simple.model.Device;

@Service
public class DeviceService {

	@Autowired
	private ReadDeviceMapper readDeviceMapper;
	
	@Autowired
	private WriteDeviceMapper writeDeviceMapper;

	@Transactional
	public void insert (Device device) {
		writeDeviceMapper.insert (device);
	}

    @Transactional(readOnly = true)
	public List <Device> selectAll () {
		return readDeviceMapper.selectAll ();
	}

    @Transactional(readOnly = true)
	public Device selectById (String id) {
		return readDeviceMapper.selectById (id);
	}
	
    @Transactional(readOnly = true)
	public Long selectCount () {
		return readDeviceMapper.selectCount ();
	}
	
	@Transactional
	public void update (Device device) {
		writeDeviceMapper.update (device);
	}

	@Transactional
	public void deleteById (String id) {
		writeDeviceMapper.deleteById (id);
	}
}
