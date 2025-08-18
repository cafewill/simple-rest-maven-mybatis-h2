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

import com.cube.simple.config.SecurityExpressions;
import com.cube.simple.dto.CommonRequest;
import com.cube.simple.dto.CommonResponse;
import com.cube.simple.enums.ResponseCode;
import com.cube.simple.model.Device;
import com.cube.simple.service.DeviceService;
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
@RequestMapping("/api/devices")
@SecurityRequirement(name = "JWT")
// @Tag(name = "Devices", description = "알림 토큰 CRUD API")
public class DeviceController {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeviceService deviceService;

    /**
     * Create 권한: ADMIN만 가능
     */
    @PostMapping
    @PreAuthorize(SecurityExpressions.HAS_ROLE_ADMIN)
    @Operation(
        summary     = "api.device.insert.summary",
        description = "api.device.insert.description"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "api.device.insert.responses.ok",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "400", description = "api.device.insert.responses.bad_request"),
        @ApiResponse(responseCode = "500", description = "api.device.insert.responses.error")
    })
    public ResponseEntity<?> insert(@Valid @RequestBody CommonRequest <Device> request) {
    	
        CommonResponse response = CommonResponse.builder().build();
        
        try {
        	if (Objects.nonNull (request) && Objects.nonNull (request.getData())) {
                Device candidate = objectMapper.convertValue(request.getData(), Device.class);
                deviceService.insert(candidate);
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
     * Read 권한: ADMIN 가능
     */
    @GetMapping
    @PreAuthorize(SecurityExpressions.HAS_ROLE_ADMIN)
    @Operation(
        summary     = "api.device.selectAll.summary",
        description = "api.device.selectAll.description"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "api.device.selectAll.responses.ok",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "500", description = "api.device.selectAll.responses.error")
    })
    public ResponseEntity<?> selectAll() {
    	
        CommonResponse response = CommonResponse.builder().build();
        
        try {
            List<Device> members = deviceService.selectAll();
            response.setData(members);
            response.setCode(ResponseCode.SUCCESS);
            response.setMessage(String.format("Select success : %d members", members.size()));
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Select all error", ex);
            response.setCode(ResponseCode.ERROR);
            response.setMessage(String.format("Select error : %s", ex.getLocalizedMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Read by ID 권한: USER, ADMIN 가능
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.name")
    @Operation(
        summary     = "api.device.selectById.summary",
        description = "api.device.selectById.description"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "api.device.selectById.responses.ok",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "400", description = "api.device.selectById.responses.bad_request"),
        @ApiResponse(responseCode = "404", description = "api.device.selectById.responses.not_found"),
        @ApiResponse(responseCode = "500", description = "api.device.selectById.responses.error")
    })
    public ResponseEntity<?> selectById(@PathVariable String id) {
    	
        CommonResponse response = CommonResponse.builder().build();
        
        try {
            if (Objects.isNull(id)) {
                response.setCode(ResponseCode.ERROR);
                response.setMessage("Select error : id is null");
                return ResponseEntity.badRequest().body(response);
            }
            Device member = deviceService.selectById(id);
            if (Objects.nonNull(member)) {
                response.setData(member);
                response.setCode(ResponseCode.SUCCESS);
                response.setMessage(String.format("Select success : %s", member));
                return ResponseEntity.ok(response);
            } else {
                response.setCode(ResponseCode.ERROR);
                response.setMessage(String.format("Select error : member not found for id=%d", id));
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
     * Update 권한: USER, ADMIN만 가능
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.name")
    @Operation(
	    summary     = "api.device.updateById.summary",
	    description = "api.device.updateById.description"
	)
	@ApiResponses({
	    @ApiResponse(responseCode = "200", description = "api.device.updateById.responses.ok"),
	    @ApiResponse(responseCode = "400", description = "api.device.updateById.responses.bad_request"),
	    @ApiResponse(responseCode = "404", description = "api.device.updateById.responses.not_found"),
	    @ApiResponse(responseCode = "500", description = "api.device.updateById.responses.error")
	})
    public ResponseEntity<?> updateById(@PathVariable String id, @RequestBody CommonRequest <Device> request) {

        CommonResponse response = CommonResponse.builder().build();
        
        try {
            if (Objects.isNull(id) || Objects.isNull(request) || Objects.isNull(request.getData())) {
                response.setCode(ResponseCode.ERROR);
                response.setMessage("Update error : invalid id or request");
                return ResponseEntity.badRequest().body(response);
            }
            Device found = deviceService.selectById(id);
            if (Objects.isNull(found)) {
                response.setCode(ResponseCode.ERROR);
                response.setMessage(String.format("Update error : no member for id=%d", id));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Device candidate = objectMapper.convertValue(request.getData(), Device.class);
            candidate.setId(id);
            deviceService.update(candidate);
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
     * Delete 권한: ADMIN만 가능
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.name")
    @Operation(
	    summary     = "api.device.deleteById.summary",
	    description = "api.device.deleteById.description"
	)
	@ApiResponses({
	    @ApiResponse(responseCode = "200", description = "api.device.deleteById.responses.ok"),
	    @ApiResponse(responseCode = "400", description = "api.device.deleteById.responses.bad_request"),
	    @ApiResponse(responseCode = "404", description = "api.device.deleteById.responses.not_found"),
	    @ApiResponse(responseCode = "500", description = "api.device.deleteById.responses.error")
	})
    public ResponseEntity<CommonResponse> deleteById(@PathVariable String id) {
        CommonResponse response = CommonResponse.builder().build();
        try {
            if (Objects.isNull(id)) {
                response.setCode(ResponseCode.ERROR);
                response.setMessage("Delete error : id is null");
                return ResponseEntity.badRequest().body(response);
            }
            Device found = deviceService.selectById(id);
            if (Objects.isNull(found)) {
                response.setCode(ResponseCode.ERROR);
                response.setMessage(String.format("Delete error : no member for id=%d", id));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            deviceService.deleteById(id);
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