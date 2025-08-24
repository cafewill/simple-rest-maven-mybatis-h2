package com.cube.simple.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cube.simple.model.Region;
import com.cube.simple.service.RegionService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 생성 요청:
     * {
     *   "name": { "ko": "서울", "en": "Seoul", "zh": "首爾" }
     * }
     */
    @PostMapping
    public ResponseEntity<?> insert(@RequestBody RegionUpsertReq req) throws JsonProcessingException {
        Region candidate = new Region();
        candidate.setName(objectMapper.writeValueAsString(req.getName()));
        regionService.create(candidate);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Region>> selectAll(@RequestParam(value = "ko", required = false) String ko) {
        if (ko != null && !ko.isBlank()) {
            return ResponseEntity.ok(regionService.selectByKo(ko));
        }
        return ResponseEntity.ok(regionService.selectAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Region> selectById(@PathVariable Long id) {
        Region found = regionService.selectById(id);
        return (found == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    /**
     * 수정 요청:
     * {
     *   "name": { "ko": "삼척", "en": "Samcheok", "zh": "三陟" }
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody RegionUpsertReq req) throws JsonProcessingException {
        Region found = new Region();
        found.setId(id);
        if (req.getName() != null) {
            found.setName(objectMapper.writeValueAsString(req.getName()));
        }
        int n = regionService.update(found);
        return (n > 0) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        int n = regionService.delete(id);
        return (n > 0) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @Data
    public static class RegionUpsertReq {
        /** 다국어 이름 Map: {"ko": "...", "en": "...", "zh": "..."} */
        private Map<String, String> name;
    }
}
