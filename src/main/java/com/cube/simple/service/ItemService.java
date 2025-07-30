package com.cube.simple.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.cube.simple.mapper.read.ReadItemMapper;
import com.cube.simple.mapper.write.WriteItemMapper;
import com.cube.simple.model.Item;

@Service
public class ItemService {

	@Autowired
	private ReadItemMapper readItemMapper;
	
	@Autowired
	private WriteItemMapper writeItemMapper;

	@Transactional
	public void insert (Item item) {
		writeItemMapper.insert (item);
	}

    @Transactional(readOnly = true)
	public List <Item> selectAll (int page, int size, String category, String search) {

    	if (page < 1) {
            page = 1;
        }
        if (!StringUtils.hasText(category)) {
            category = null;
        }
        if (!StringUtils.hasText(search)) {
            search = null;
        }    	
		return readItemMapper.selectAll (page, size, category, search);
	}

    @Transactional(readOnly = true)
	public Item selectById (Long id) {
		return readItemMapper.selectById (id);
	}
	
    @Transactional(readOnly = true)
	public Long selectCount () {
		return readItemMapper.selectCount ();
	}
	
	@Transactional
	public void update (Item item) {
		writeItemMapper.update (item);
	}

	@Transactional
	public void deleteById (Long id) {
		writeItemMapper.deleteById (id);
	}
}
