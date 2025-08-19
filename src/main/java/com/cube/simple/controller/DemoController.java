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

// CHANGED: Common DTOs import
import com.cube.simple.dto.DemoRequest;
import com.cube.simple.dto.DemoResponse;
import com.cube.simple.enums.ResponseCode;
import com.cube.simple.model.Demo;
import com.cube.simple.service.DemoService;
// ADDED: Messages utility import
import com.cube.simple.util.Messages;
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
@Tag(name = "{api.operations.entity.demo}")
// @Tag(name = "Demos", description = "데모 CRUD API")
public class DemoController {

    // ADDED: Constants for i18n messages, similar to UsersController
    private static final String ENTITY_KEY   = "api.operations.entity.demo";
    private static final String ENTITY_TOKEN = "{api.operations.entity.demo}";

    @Autowired private ObjectMapper objectMapper;
    @Autowired private DemoService demoService;
    // ADDED: Messages service for i18n
    @Autowired private Messages messages;

    /**
     * Create 권한: ADMIN 만 가능
     */
    @PostMapping
    // CHANGED: Standardized Swagger annotations
    @Operation(
        summary     = "{api.operations.insert.summary|" + ENTITY_TOKEN + "}",
        description = "{api.operations.insert.description|" + ENTITY_TOKEN + "}",
        security    = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description  = "{api.operations.insert.responses.ok|" + ENTITY_TOKEN + "}",
            content      = @Content(mediaType = "application/json",
                                  schema = @Schema(implementation = DemoResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "{api.operations.insert.responses.bad_request}"),
        @ApiResponse(responseCode = "500", description = "{api.operations.insert.responses.error}")
    })
    // CHANGED: Use DemoRequest and DemoResponse
    public ResponseEntity<?> insert(@Valid @RequestBody DemoRequest<Demo> request) {
        DemoResponse response = DemoResponse.builder().build();
        try {
            if (request == null || request.getData() == null) {
                response.setCode(ResponseCode.ERROR);
                // CHANGED: Use i18n message
                response.setMessage(messages.get("api.operations.insert.responses.bad_request"));
                return ResponseEntity.badRequest().body(response);
            }
            Demo candidate = objectMapper.convertValue(request.getData(), Demo.class);
            demoService.insert(candidate);

            response.setData(candidate);
            response.setCode(ResponseCode.SUCCESS);
            // CHANGED: Use i18n message
            response.setMessage(messages.get("api.operations.insert.responses.ok", ENTITY_KEY));
            log.info(response.getMessage());
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Insert error", ex);
            response.setCode(ResponseCode.ERROR);
            // CHANGED: Use i18n message
            response.setMessage(messages.get("api.operations.insert.responses.error"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Read 권한: 모두 공개 (인증 없이 접근 가능)
     */
    @GetMapping
    // CHANGED: Standardized Swagger annotations
    @Operation(
        summary     = "{api.operations.select.summary|" + ENTITY_TOKEN + "}",
        description = "{api.operations.select.description|" + ENTITY_TOKEN + "}"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description  = "{api.operations.select.responses.ok|" + ENTITY_TOKEN + "}",
            content      = @Content(mediaType = "application/json",
                                  schema = @Schema(implementation = DemoResponse.class))
        ),
        @ApiResponse(responseCode = "500", description = "{api.operations.select.responses.error}")
    })
    public ResponseEntity<?> selectAll() {
        DemoResponse response = DemoResponse.builder().build();
        try {
            List<Demo> list = demoService.selectAll();
            response.setData(list);
            response.setCode(ResponseCode.SUCCESS);
            // CHANGED: Use i18n message
            response.setMessage(messages.get("api.operations.select.responses.ok", ENTITY_KEY));
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Select all error", ex);
            response.setCode(ResponseCode.ERROR);
            // CHANGED: Use i18n message
            response.setMessage(messages.get("api.operations.select.responses.error"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Read 권한: 모두 공개 (인증 없이 접근 가능)
     */
    @GetMapping("/{id}")
    // CHANGED: Standardized Swagger annotations
    @Operation(
        summary     = "{api.operations.select.summary|" + ENTITY_TOKEN + "}",
        description = "{api.operations.select.description|" + ENTITY_TOKEN + "}"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description  = "{api.operations.select.responses.ok|" + ENTITY_TOKEN + "}",
            content      = @Content(mediaType = "application/json",
                                  schema = @Schema(implementation = DemoResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "{api.operations.select.responses.not_found|" + ENTITY_TOKEN + "}"),
        @ApiResponse(responseCode = "500", description = "{api.operations.select.responses.error}")
    })
    public ResponseEntity<?> selectById(@PathVariable Long id) {
        DemoResponse response = DemoResponse.builder().build();
        try {
            Demo found = demoService.selectById(id);
            if (found != null) {
                response.setData(found);
                response.setCode(ResponseCode.SUCCESS);
                // CHANGED: Use i18n message
                response.setMessage(messages.get("api.operations.select.responses.ok", ENTITY_KEY));
                return ResponseEntity.ok(response);
            } else {
                response.setCode(ResponseCode.ERROR);
                // CHANGED: Use i18n message
                response.setMessage(messages.get("api.operations.select.responses.not_found", ENTITY_KEY));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception ex) {
            log.error("Select by id error", ex);
            response.setCode(ResponseCode.ERROR);
            // CHANGED: Use i18n message
            response.setMessage(messages.get("api.operations.select.responses.error"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update 권한: ADMIN 만 가능
     */
    @PutMapping("/{id}")
    // CHANGED: Standardized Swagger annotations
    @Operation(
        summary     = "{api.operations.update.summary|" + ENTITY_TOKEN + "}",
        description = "{api.operations.update.description|" + ENTITY_TOKEN + "}",
        security    = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description  = "{api.operations.update.responses.ok|" + ENTITY_TOKEN + "}",
            content      = @Content(mediaType = "application/json",
                                  schema = @Schema(implementation = DemoResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "{api.operations.update.responses.bad_request}"),
        @ApiResponse(responseCode = "404", description = "{api.operations.update.responses.not_found|" + ENTITY_TOKEN + "}"),
        @ApiResponse(responseCode = "500", description = "{api.operations.update.responses.error}")
    })
    public ResponseEntity<?> updateById(@PathVariable Long id, @RequestBody DemoRequest<Demo> request) {
        DemoResponse response = DemoResponse.builder().build();
        try {
            if (id == null || request == null || request.getData() == null) {
                response.setCode(ResponseCode.ERROR);
                // CHANGED: Use i18n message
                response.setMessage(messages.get("api.operations.update.responses.bad_request"));
                return ResponseEntity.badRequest().body(response);
            }

            Demo found = demoService.selectById(id);
            if (found == null) {
                response.setCode(ResponseCode.ERROR);
                // CHANGED: Use i18n message
                response.setMessage(messages.get("api.operations.update.responses.not_found", ENTITY_KEY));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Demo candidate = objectMapper.convertValue(request.getData(), Demo.class);
            candidate.setId(id);
            demoService.update(candidate);

            response.setData(candidate);
            response.setCode(ResponseCode.SUCCESS);
            // CHANGED: Use i18n message
            response.setMessage(messages.get("api.operations.update.responses.ok", ENTITY_KEY));
            log.info(response.getMessage());
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Update error", ex);
            response.setCode(ResponseCode.ERROR);
            // CHANGED: Use i18n message
            response.setMessage(messages.get("api.operations.update.responses.error"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete 권한: ADMIN 만 가능
     */
    @DeleteMapping("/{id}")
    // CHANGED: Standardized Swagger annotations
    @Operation(
        summary     = "{api.operations.delete.summary|" + ENTITY_TOKEN + "}",
        description = "{api.operations.delete.description|" + ENTITY_TOKEN + "}",
        security    = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description  = "{api.operations.delete.responses.ok|" + ENTITY_TOKEN + "}",
            content      = @Content(mediaType = "application/json",
                                  schema = @Schema(implementation = DemoResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "{api.operations.delete.responses.not_found|" + ENTITY_TOKEN + "}"),
        @ApiResponse(responseCode = "500", description = "{api.operations.delete.responses.error}")
    })
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        DemoResponse response = DemoResponse.builder().build();
        try {
            Demo found = demoService.selectById(id);
            if (found == null) {
                response.setCode(ResponseCode.ERROR);
                // CHANGED: Use i18n message
                response.setMessage(messages.get("api.operations.delete.responses.not_found", ENTITY_KEY));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            demoService.deleteById(id);
            response.setData(found);
            response.setCode(ResponseCode.SUCCESS);
            // CHANGED: Use i18n message
            response.setMessage(messages.get("api.operations.delete.responses.ok", ENTITY_KEY));
            log.info(response.getMessage());
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Delete error", ex);
            response.setCode(ResponseCode.ERROR);
            // CHANGED: Use i18n message
            response.setMessage(messages.get("api.operations.delete.responses.error"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
