package com.cube.simple.controller;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cube.simple.dto.CommonResponse;
import com.cube.simple.dto.DemoRequest;
import com.cube.simple.dto.DemoResponse;
import com.cube.simple.model.Demo;
import com.cube.simple.service.DemoService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/demos")
// @Tag(name = "Demos", description = "데모 CRUD API")
public class DemoController {
    
    @Autowired
    private DemoService demoService;

    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Create 권한: ADMIN 만 가능
     */
    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')")
    /*
    @Operation(
        summary = "새 데모 등록",
        description = "DemoRequest DTO로 전달된 데이터를 기반으로 새 데모를 저장함",
		security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "등록 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DemoResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    */
    @Operation(
    	    summary     = "{api.demo.insert.summary}",
    	    description = "{api.demo.insert.description}",
    	    security    = @SecurityRequirement(name = "JWT")
	)
	@ApiResponses({
	    @ApiResponse(responseCode = "200", description = "{api.demo.insert.responses.ok}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DemoResponse.class))),
	    @ApiResponse(responseCode = "400", description = "{api.demo.insert.responses.bad_request}"),
	    @ApiResponse(responseCode = "500", description = "{api.demo.insert.responses.error}")
	})    
    public ResponseEntity<?> insert(@Valid @RequestBody DemoRequest request) {
        DemoResponse response = DemoResponse.builder().build();
        try {
            if (!Objects.isNull (request) && !Objects.isNull (request.getData())) {
                Demo candidate = objectMapper.convertValue(request.getData(), Demo.class);
                demoService.insert(candidate);
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
     * Read 권한: 모두 공개 (인증 없이 접근 가능)
     */
    @GetMapping
    /*
    @Operation(
    		summary = "모든 데모 조회", 
    		description = "등록된 모든 데모 목록을 반환함"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DemoResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    */
    @Operation(
    	    summary     = "{api.demo.selectAll.summary}",
    	    description = "{api.demo.selectAll.description}"
	)
	@ApiResponses({
	    @ApiResponse(responseCode = "200", description = "{api.demo.selectAll.responses.ok}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DemoResponse.class))),
	    @ApiResponse(responseCode = "500", description = "{api.demo.selectAll.responses.error}")
	})        
    public ResponseEntity<?> selectAll() {
        DemoResponse response = DemoResponse.builder().build();
        try {
            List<Demo> demos = demoService.selectAll();
            response.setData(demos);
            response.setStatus(true);
            response.setMessage(String.format("Select success : %d items", demos.size()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Select all error", e);
            response.setStatus(false);
            response.setMessage(String.format("Select error : %s", e.getLocalizedMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Read 권한: 모두 공개 (인증 없이 접근 가능)
     */
    @GetMapping("/{id}")
    /*
    @Operation(
    		summary = "ID로 데모 조회", 
    		description = "PathVariable로 전달된 ID의 데모를 반환함",
    		security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 ID"),
        @ApiResponse(responseCode = "404", description = "데모 없음"),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    */
    @Operation(
    	    summary     = "{api.demo.selectById.summary}",
    	    description = "{api.demo.selectById.description}"
	)
	@ApiResponses({
	    @ApiResponse(responseCode = "200", description = "{api.demo.selectById.responses.ok}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DemoResponse.class))),
	    @ApiResponse(responseCode = "400", description = "{api.demo.selectById.responses.bad_request}"),
	    @ApiResponse(responseCode = "404", description = "{api.demo.selectById.responses.responses.not_found}"),
	    @ApiResponse(responseCode = "500", description = "{api.demo.selectById.responses.error}")
	})        
    public ResponseEntity<?> selectById(@PathVariable Long id) {
        CommonResponse response = CommonResponse.builder().build();
        try {
            if (Objects.isNull(id)) {
                response.setStatus(false);
                response.setMessage("Select error : id is null");
                return ResponseEntity.badRequest().body(response);
            }
            Demo demo = demoService.selectById(id);
            if (Objects.nonNull (demo)) {
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Update 권한: ADMIN 만 가능
     */
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    /*
    @Operation(
        summary = "데모 수정",
        description = "PathVariable로 전달된 ID의 데모를, RequestBody로 전달된 데이터로 수정함",
		security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DemoResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 ID 또는 요청"),
        @ApiResponse(responseCode = "404", description = "데모가 존재하지 않음"),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    */
    @Operation(
    	    summary     = "{api.demo.updateById.summary}",
    	    description = "{api.demo.updateById.description}",
    	    security    = @SecurityRequirement(name = "JWT")
	)
	@ApiResponses({
	    @ApiResponse(responseCode = "200", description = "{api.demo.updateById.responses.ok}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DemoResponse.class))),
	    @ApiResponse(responseCode = "400", description = "{api.demo.updateById.responses.bad_request}"),
	    @ApiResponse(responseCode = "404", description = "{api.demo.updateById.responses.responses.not_found}"),
	    @ApiResponse(responseCode = "500", description = "{api.demo.updateById.responses.error}")
	})        
    public ResponseEntity<?> updateById(
            @PathVariable Long id,
            @RequestBody DemoRequest request) {

        DemoResponse response = DemoResponse.builder().build();
        try {
            if (Objects.isNull(id) || Objects.isNull(request) || Objects.isNull(request.getData())) {
                response.setStatus(false);
                response.setMessage("Update error : invalid id or request");
                return ResponseEntity.badRequest().body(response);
            }
            Demo found = demoService.selectById(id);
            if (Objects.isNull(found)) {
                response.setStatus(false);
                response.setMessage(String.format("Update error : no demo for id=%d", id));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Demo candidate = objectMapper.convertValue(request.getData(), Demo.class);
            candidate.setId(id);
            demoService.update(candidate);
            response.setData(candidate);
            response.setStatus(true);
            response.setMessage(String.format("Update success : %s", candidate));
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
     * Delete 권한: ADMIN 만 가능
     */
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    /*
    @Operation(
        summary = "데모 삭제",
        description = "PathVariable로 전달된 ID의 데모를 삭제함",
		security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = DemoResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 ID"),
        @ApiResponse(responseCode = "404", description = "데모가 존재하지 않음"),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    */
    @Operation(
    	    summary     = "{api.demo.deleteById.summary}",
    	    description = "{api.demo.deleteById.description}",
    	    security    = @SecurityRequirement(name = "JWT")
	)
	@ApiResponses({
	    @ApiResponse(responseCode = "200", description = "{api.demo.deleteById.responses.ok}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DemoResponse.class))),
	    @ApiResponse(responseCode = "400", description = "{api.demo.deleteById.responses.bad_request}"),
	    @ApiResponse(responseCode = "404", description = "{api.demo.deleteById.responses.responses.not_found}"),
	    @ApiResponse(responseCode = "500", description = "{api.demo.deleteById.responses.error}")
	})            
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        DemoResponse response = DemoResponse.builder().build();
        try {
            if (Objects.isNull(id)) {
                response.setStatus(false);
                response.setMessage("Delete error : id is null");
                return ResponseEntity.badRequest().body(response);
            }
            Demo found = demoService.selectById(id);
            if (Objects.isNull(found)) {
                response.setStatus(false);
                response.setMessage(String.format("Delete error : no demo for id=%d", id));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            demoService.deleteById(id);
            response.setData(found);
            response.setStatus(true);
            response.setMessage(String.format("Delete success : %s", found));
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
