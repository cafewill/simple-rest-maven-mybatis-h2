package com.cube.simple.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.cube.simple.mapper.read.ReadItemMapper;
import com.cube.simple.mapper.write.WriteItemMapper;
import com.cube.simple.model.Item;

@Service
public class CachedItemService {

	@Autowired
	private ReadItemMapper readItemMapper;
	
	@Autowired
	private WriteItemMapper writeItemMapper;

	@Transactional
    @CacheEvict(cacheNames = {"items", "itemCount"}, allEntries = true)
	public void insert (Item item) {
		writeItemMapper.insert (item);
	}

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "items", key = "#page + '-' + #size + '-' + (#category?:'') + '-' + (#search?:'')")
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
        
        // Cache 정상 동작 여부 테스트를 위한 딜레이 설정함
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
		return readItemMapper.selectAll (page, size, category, search);
	}

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "item", key = "#id")
	public Item selectById (Long id) {
		return readItemMapper.selectById (id);
	}
	
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "itemCount")
	public Long selectCount () {
		return readItemMapper.selectCount ();
	}
	
	@Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "item",      key = "#item.id"),
            @CacheEvict(cacheNames = "items",     allEntries = true),
            @CacheEvict(cacheNames = "itemCount", allEntries = true)
        })
	public void update (Item item) {
		writeItemMapper.update (item);
	}

	@Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "item",      key = "#id"),
            @CacheEvict(cacheNames = "items",     allEntries = true),
            @CacheEvict(cacheNames = "itemCount", allEntries = true)
        })
	public void deleteById (Long id) {
		writeItemMapper.deleteById (id);
	}
}
