package com.cube.simple.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import com.cube.simple.dto.CommonRequest;
import com.cube.simple.dto.CommonResponse;
import com.cube.simple.model.Item;
import com.cube.simple.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Items", description = "아이템 CRUD API")
@RestController
@RequestMapping("/api/items")
public class ItemController {
    
    @Autowired
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Create 권한: ADMIN만 가능
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(
        summary = "새 아이템 등록",
        description = "CommonRequest DTO로 전달된 데이터를 기반으로 새 아이템을 저장합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "등록 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    public ResponseEntity<CommonResponse> insert(@RequestBody CommonRequest request) {
        CommonResponse response = CommonResponse.builder().build();
        try {
            if (request != null && request.getData() != null) {
                Item candidate = objectMapper.convertValue(request.getData(), Item.class);
                itemService.insert(candidate);

                response.setData(candidate);
                response.setStatus(true);
                response.setMessage(String.format("Insert success : %s", candidate));
                log.info(response.getMessage());
                return ResponseEntity.ok(response);
            } else {
                response.setStatus(false);
                response.setMessage(String.format("Insert error : invalid request %s", request));
                log.warn(response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.setStatus(false);
            response.setMessage(String.format("Insert error : %s", e.getLocalizedMessage()));
            log.error(response.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Read 권한: USER, ADMIN 가능
     */
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    @Operation(summary = "모든 아이템 조회", description = "등록된 모든 아이템 목록을 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    public ResponseEntity<CommonResponse> selectAll() {
        CommonResponse response = CommonResponse.builder().build();
        try {
            List<Item> items = itemService.selectAll();
            response.setData(items);
            response.setStatus(true);
            response.setMessage(String.format("Select success : %d items", items.size()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Select all error", e);
            response.setStatus(false);
            response.setMessage(String.format("Select error : %s", e.getLocalizedMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Read by ID 권한: USER, ADMIN 가능
     */
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "ID로 아이템 조회", description = "PathVariable로 전달된 ID의 아이템을 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 ID"),
        @ApiResponse(responseCode = "404", description = "아이템 없음"),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    public ResponseEntity<CommonResponse> selectById(@PathVariable Long id) {
        CommonResponse response = CommonResponse.builder().build();
        try {
            if (id == null) {
                response.setStatus(false);
                response.setMessage("Select error : id is null");
                return ResponseEntity.badRequest().body(response);
            }
            Item item = itemService.selectById(id);
            if (item != null) {
                response.setData(item);
                response.setStatus(true);
                response.setMessage(String.format("Select success : %s", item));
                return ResponseEntity.ok(response);
            } else {
                response.setStatus(false);
                response.setMessage(String.format("Select error : item not found for id=%d", id));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            log.error("Select by id error", e);
            response.setStatus(false);
            response.setMessage(String.format("Select error : %s", e.getLocalizedMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update 권한: ADMIN만 가능
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(
        summary = "아이템 수정",
        description = "PathVariable로 전달된 ID의 아이템을, RequestBody로 전달된 데이터로 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 ID 또는 요청"),
        @ApiResponse(responseCode = "404", description = "아이템이 존재하지 않음"),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    public ResponseEntity<CommonResponse> update(
            @PathVariable Long id,
            @RequestBody CommonRequest request) {

        CommonResponse response = CommonResponse.builder().build();
        try {
            if (id == null || request == null || request.getData() == null) {
                response.setStatus(false);
                response.setMessage("Update error : invalid id or request");
                return ResponseEntity.badRequest().body(response);
            }
            Item existing = itemService.selectById(id);
            if (existing == null) {
                response.setStatus(false);
                response.setMessage(String.format("Update error : no item for id=%d", id));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Item toUpdate = objectMapper.convertValue(request.getData(), Item.class);
            toUpdate.setId(id);
            itemService.update(toUpdate);

            response.setData(toUpdate);
            response.setStatus(true);
            response.setMessage(String.format("Update success : %s", toUpdate));
            log.info(response.getMessage());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Update error", e);
            response.setStatus(false);
            response.setMessage(String.format("Update error : %s", e.getLocalizedMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete 권한: ADMIN만 가능
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(
        summary = "아이템 삭제",
        description = "PathVariable로 전달된 ID의 아이템을 삭제합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 ID"),
        @ApiResponse(responseCode = "404", description = "아이템이 존재하지 않음"),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    public ResponseEntity<CommonResponse> delete(@PathVariable Long id) {
        CommonResponse response = CommonResponse.builder().build();
        try {
            if (id == null) {
                response.setStatus(false);
                response.setMessage("Delete error : id is null");
                return ResponseEntity.badRequest().body(response);
            }
            Item existing = itemService.selectById(id);
            if (existing == null) {
                response.setStatus(false);
                response.setMessage(String.format("Delete error : no item for id=%d", id));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            itemService.deleteById(id);
            response.setData(existing);
            response.setStatus(true);
            response.setMessage(String.format("Delete success : %s", existing));
            log.info(response.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Delete error", e);
            response.setStatus(false);
            response.setMessage(String.format("Delete error : %s", e.getLocalizedMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
