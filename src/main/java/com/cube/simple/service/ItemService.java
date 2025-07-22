package com.cube.simple.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cube.simple.mapper.ItemMapper;
import com.cube.simple.model.Item;

@Service
public class ItemService {

	@Autowired
	ItemMapper itemMapper;
	
	@Transactional
	public void insert (Item item) {
		itemMapper.insert (item);
	}

    @Transactional(readOnly = true)
	public List <Item> selectAll () {
		return itemMapper.selectAll ();
	}

    @Transactional(readOnly = true)
	public Item selectById (Long id) {
		return itemMapper.selectById (id);
	}
	
    @Transactional(readOnly = true)
	public Long selectCount () {
		return itemMapper.selectCount ();
	}
	
	@Transactional
	public void update (Item item) {
		itemMapper.update (item);
	}

	@Transactional
	public void delete (Long id) {
		itemMapper.delete (id);
	}
}
