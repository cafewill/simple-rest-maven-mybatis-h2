package com.cube.simple.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cube.simple.dto.DemoRequest;
import com.cube.simple.dto.DemoResponse;
import com.cube.simple.model.Demo;
import com.cube.simple.service.DemoService;
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
@Tag(name = "Demos", description = "데모 CRUD API")
@RestController
@RequestMapping("/api/demos")
public class DemoController {
	
	@Autowired
	DemoService demoService;

	@Autowired
	ObjectMapper objectMapper;
	
	@PostMapping
    @Operation(
        summary = "새 데모 등록",
        description = "DemoRequest DTO로 전달된 데이터를 기반으로 새 데모를 저장합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "등록 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DemoResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
	public ResponseEntity<DemoResponse> insert(@RequestBody DemoRequest request) {
		
	    // 1) 기본 응답 객체 생성
	    DemoResponse response = DemoResponse.builder().build();

	    try {
	        // 2) 유효성 검사
	        if (request != null && request.getData() != null) {
	        	
	            // 3) 비즈니스 로직 수행
	            Demo candidate = objectMapper.readValue(
	                    objectMapper.writeValueAsString(request.getData()),
	                    Demo.class
	            );
	            demoService.insert(candidate);

	            // 4) 성공 응답 설정
	            response.setData(candidate);
	            response.setStatus(true);
	            response.setMessage(String.format("Insert success : %s", candidate));
	            log.info(response.getMessage());

	            // 5) HTTP 200 OK 와 함께 body 반환
	            return ResponseEntity
	                    .ok(response);
	        } else {
	            // 6) 요청이 잘못된 경우 HTTP 400 Bad Request
	            response.setStatus(false);
	            response.setMessage(String.format("Insert error : invalid request %s", request));
	            log.warn(response.getMessage());

	            return ResponseEntity
	                    .badRequest()
	                    .body(response);
	        }
	    } catch (Exception e) {
	        // 7) 예외 발생 시 HTTP 500 Internal Server Error
	        response.setStatus(false);
	        response.setMessage(String.format("Insert error : %s", e.getLocalizedMessage()));
	        log.error(response.getMessage(), e);

	        return ResponseEntity
	                .status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(response);
	    }
	}
	
    @GetMapping
    @Operation(summary = "모든 데모 조회", description = "등록된 모든 데모 목록을 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DemoResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    public ResponseEntity<DemoResponse> selectAll() {
        DemoResponse response = DemoResponse.builder().build();
        try {
            List<Demo> items = demoService.selectAll();
            if (items != null) {
                int count = items.size();
                for (int i = 0; i < count; i++) {
                    log.info("Select demo #{} : {}", i + 1, items.get(i));
                }
                response.setData(items);
                response.setStatus(true);
                response.setMessage(String.format("Select success : %d items", count));
                return ResponseEntity.ok(response);
            } else {
                response.setStatus(false);
                response.setMessage("Select error : no items found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            log.error("Select all error", e);
            response.setStatus(false);
            response.setMessage(String.format("Select error : %s", e.getLocalizedMessage()));
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID로 데모 조회", description = "PathVariable로 전달된 ID의 데모를 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DemoResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 ID"),
        @ApiResponse(responseCode = "404", description = "데모 없음"),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    public ResponseEntity<DemoResponse> selectById(@PathVariable Long id) {
        DemoResponse response = DemoResponse.builder().build();
        try {
            if (id == null) {
                response.setStatus(false);
                response.setMessage("Select error : id is null");
                return ResponseEntity.badRequest().body(response);
            }
            Demo demo = demoService.selectById(id);
            if (demo != null) {
                response.setData(demo);
                response.setStatus(true);
                response.setMessage(String.format("Select success : %s", demo));
                return ResponseEntity.ok(response);
            } else {
                response.setStatus(false);
                response.setMessage(String.format("Select error : demo not found for id=%d", id));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            log.error("Select by id error", e);
            response.setStatus(false);
            response.setMessage(String.format("Select error : %s", e.getLocalizedMessage()));
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "데모 수정",
        description = "PathVariable로 전달된 ID의 데모를, RequestBody로 전달된 데이터로 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DemoResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 ID 또는 요청"),
        @ApiResponse(responseCode = "404", description = "데모가 존재하지 않음"),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    public ResponseEntity<DemoResponse> update(
            @PathVariable Long id,
            @RequestBody DemoRequest request) {

        DemoResponse response = DemoResponse.builder().build();
        try {
            if (id == null || request == null || request.getData() == null) {
                response.setStatus(false);
                response.setMessage("Update error : invalid id or request");
                return ResponseEntity.badRequest().body(response);
            }
            Demo existing = demoService.selectById(id);
            if (existing == null) {
                response.setStatus(false);
                response.setMessage(String.format("Update error : no demo for id=%d", id));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 실제 업데이트 로직 (예: objectMapper 이용 또는 DTO → Entity 변환)
            Demo toUpdate = objectMapper.convertValue(request.getData(), Demo.class);
            toUpdate.setId(id);
            demoService.update(toUpdate);

            response.setData(toUpdate);
            response.setStatus(true);
            response.setMessage(String.format("Update success : %s", toUpdate));
            log.info(response.getMessage());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Update error", e);
            response.setStatus(false);
            response.setMessage(String.format("Update error : %s", e.getLocalizedMessage()));
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "데모 삭제",
        description = "PathVariable로 전달된 ID의 데모를 삭제합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DemoResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 ID"),
        @ApiResponse(responseCode = "404", description = "데모가 존재하지 않음"),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    public ResponseEntity<DemoResponse> delete(@PathVariable Long id) {
        DemoResponse response = DemoResponse.builder().build();
        try {
            if (id == null) {
                response.setStatus(false);
                response.setMessage("Delete error : id is null");
                return ResponseEntity.badRequest().body(response);
            }
            Demo existing = demoService.selectById(id);
            if (existing == null) {
                response.setStatus(false);
                response.setMessage(String.format("Delete error : no demo for id=%d", id));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            demoService.delete(id);
            response.setData(existing);
            response.setStatus(true);
            response.setMessage(String.format("Delete success : %s", existing));
            log.info(response.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Delete error", e);
            response.setStatus(false);
            response.setMessage(String.format("Delete error : %s", e.getLocalizedMessage()));
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

}
