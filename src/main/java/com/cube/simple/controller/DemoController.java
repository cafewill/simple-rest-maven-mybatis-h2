package com.cube.simple.controller;

import java.util.List;
import java.util.Objects;

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
import com.cube.simple.enums.ResponseCode;
import com.cube.simple.model.Demo;
import com.cube.simple.service.DemoService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/demos")
// @Tag(name = "Demos", description = "데모 CRUD API")
public class DemoController {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private DemoService demoService;

    /**
     * Create 권한: ADMIN 만 가능
     */
    @PostMapping
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
    public ResponseEntity<?> insert(@Valid @RequestBody DemoRequest <Demo> request) {
    	
        DemoResponse response = DemoResponse.builder().build();

        try {
            if (!Objects.isNull (request) && !Objects.isNull (request.getData())) {
                Demo candidate = objectMapper.convertValue(request.getData(), Demo.class);
                demoService.insert(candidate);
                response.setData(candidate);
                response.setCode(ResponseCode.SUCCESS);
                response.setMessage(String.format("Insert success : %s", candidate));
                log.info(response.getMessage());
                return ResponseEntity.ok(response);
            } else {
                response.setCode(ResponseCode.ERROR);
                response.setMessage(String.format("Insert error : invalid request %s", request));
                log.warn(response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception ex) {
            response.setCode(ResponseCode.ERROR);
            response.setMessage(String.format("Insert error : %s", ex.getLocalizedMessage()));
            log.error(response.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Read 권한: 모두 공개 (인증 없이 접근 가능)
     */
    @GetMapping
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
            response.setCode(ResponseCode.SUCCESS);
            response.setMessage(String.format("Select success : %d items", demos.size()));
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Select all error", ex);
            response.setCode(ResponseCode.ERROR);
            response.setMessage(String.format("Select error : %s", ex.getLocalizedMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Read 권한: 모두 공개 (인증 없이 접근 가능)
     */
    @GetMapping("/{id}")
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
    	
        DemoResponse response = DemoResponse.builder().build();
        
        try {
            if (Objects.isNull(id)) {
                response.setCode(ResponseCode.ERROR);
                response.setMessage("Select error : id is null");
                return ResponseEntity.badRequest().body(response);
            }
            Demo demo = demoService.selectById(id);
            if (Objects.nonNull (demo)) {
                response.setData(demo);
                response.setCode(ResponseCode.SUCCESS);
                response.setMessage(String.format("Select success : %s", demo));
                return ResponseEntity.ok(response);
            } else {
                response.setCode(ResponseCode.ERROR);
                response.setMessage(String.format("Select error : demo not found for id=%d", id));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception ex) {
            log.error("Select by id error", ex);
            response.setCode(ResponseCode.ERROR);
            response.setMessage(String.format("Select error : %s", ex.getLocalizedMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Update 권한: ADMIN 만 가능
     */
    @PutMapping("/{id}")
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
    public ResponseEntity<?> updateById(@PathVariable Long id, @RequestBody DemoRequest <Demo> request) {

        DemoResponse response = DemoResponse.builder().build();
        
        try {
            if (Objects.isNull(id) || Objects.isNull(request) || Objects.isNull(request.getData())) {
                response.setCode(ResponseCode.ERROR);
                response.setMessage("Update error : invalid id or request");
                return ResponseEntity.badRequest().body(response);
            }
            Demo found = demoService.selectById(id);
            if (Objects.isNull(found)) {
                response.setCode(ResponseCode.ERROR);
                response.setMessage(String.format("Update error : no demo for id=%d", id));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Demo candidate = objectMapper.convertValue(request.getData(), Demo.class);
            candidate.setId(id);
            demoService.update(candidate);
            response.setData(candidate);
            response.setCode(ResponseCode.SUCCESS);
            response.setMessage(String.format("Update success : %s", candidate));
            log.info(response.getMessage());
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Update error", ex);
            response.setCode(ResponseCode.ERROR);
            response.setMessage(String.format("Update error : %s", ex.getLocalizedMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete 권한: ADMIN 만 가능
     */
    @DeleteMapping("/{id}")
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
                response.setCode(ResponseCode.ERROR);
                response.setMessage("Delete error : id is null");
                return ResponseEntity.badRequest().body(response);
            }
            Demo found = demoService.selectById(id);
            if (Objects.isNull(found)) {
                response.setCode(ResponseCode.ERROR);
                response.setMessage(String.format("Delete error : no demo for id=%d", id));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            demoService.deleteById(id);
            response.setData(found);
            response.setCode(ResponseCode.SUCCESS);
            response.setMessage(String.format("Delete success : %s", found));
            log.info(response.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Delete error", ex);
            response.setCode(ResponseCode.ERROR);
            response.setMessage(String.format("Delete error : %s", ex.getLocalizedMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
