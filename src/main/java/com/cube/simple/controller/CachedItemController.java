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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cube.simple.dto.CommonRequest;
import com.cube.simple.dto.CommonResponse;
import com.cube.simple.enums.ResponseCode;
import com.cube.simple.model.Item;
import com.cube.simple.service.CachedItemService;
import com.cube.simple.util.MessageUtil;
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
@RequestMapping("/api/cached/items")
@SecurityRequirement(name = "JWT")
// @Tag(name = "{api.operations.entity.cachedItem}")
// @Tag(name = "Items", description = "아이템 CRUD API")
public class CachedItemController {

    private static final String ENTITY_TOKEN = "{api.operations.entity.cachedItem}";

    @Autowired private ObjectMapper objectMapper;
    @Autowired private CachedItemService cachedItemService;
    @Autowired private MessageUtil messages;

    /**
     * Create 권한: ADMIN만 가능
     */
    @PostMapping
    @Operation(
        summary     = "{api.operations.insert.summary|" + ENTITY_TOKEN + "}",
        description = "{api.operations.insert.description|" + ENTITY_TOKEN + "}"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description  = "{api.operations.insert.responses.ok|" + ENTITY_TOKEN + "}",
            content      = @Content(mediaType = "application/json",
                                  schema = @Schema(implementation = CommonResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "{api.operations.insert.responses.bad_request}"),
        @ApiResponse(responseCode = "500", description = "{api.operations.insert.responses.error}")
    })
    public ResponseEntity<?> insert(@Valid @RequestBody CommonRequest<Item> request) {
        CommonResponse response = CommonResponse.builder().build();
        try {
            if (request == null || request.getData() == null) {
                response.setCode(ResponseCode.ERROR);
                response.setMessage(messages.get("api.operations.insert.responses.bad_request"));
                return ResponseEntity.badRequest().body(response);
            }
            Item candidate = objectMapper.convertValue(request.getData(), Item.class);
            cachedItemService.insert(candidate);

            response.setData(candidate);
            response.setCode(ResponseCode.SUCCESS);
            response.setMessage(messages.get("api.operations.insert.responses.ok"));
            log.info(response.getMessage());
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Insert error", ex);
            response.setCode(ResponseCode.ERROR);
            response.setMessage(messages.get("api.operations.insert.responses.error"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Read 권한: USER, ADMIN 가능
     */
    @GetMapping
    @Operation(
        summary     = "{api.operations.select.summary|" + ENTITY_TOKEN + "}",
        description = "{api.operations.select.description|" + ENTITY_TOKEN + "}"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description  = "{api.operations.select.responses.ok|" + ENTITY_TOKEN + "}",
            content      = @Content(mediaType = "application/json",
                                  schema = @Schema(implementation = CommonResponse.class))
        ),
        @ApiResponse(responseCode = "500", description = "{api.operations.select.responses.error}")
    })
    public ResponseEntity<?> selectAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {

        CommonResponse response = CommonResponse.builder().build();
        try {
            List<Item> items = cachedItemService.selectAll(page, size, category, search);
            response.setData(items);
            response.setCode(ResponseCode.SUCCESS);
            response.setMessage(messages.get("api.operations.select.responses.ok"));
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Select all error", ex);
            response.setCode(ResponseCode.ERROR);
            response.setMessage(messages.get("api.operations.select.responses.error"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Read by ID 권한: USER, ADMIN 가능
     */
    @GetMapping("/{id}")
    @Operation(
        summary     = "{api.operations.select.summary|" + ENTITY_TOKEN + "}",
        description = "{api.operations.select.description|" + ENTITY_TOKEN + "}"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description  = "{api.operations.select.responses.ok|" + ENTITY_TOKEN + "}",
            content      = @Content(mediaType = "application/json",
                                  schema = @Schema(implementation = CommonResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "{api.operations.select.responses.not_found|" + ENTITY_TOKEN + "}"),
        @ApiResponse(responseCode = "500", description = "{api.operations.select.responses.error}")
    })
    public ResponseEntity<?> selectById(@PathVariable Long id) {
        CommonResponse response = CommonResponse.builder().build();
        try {
            Item found = cachedItemService.selectById(id);
            if (found != null) {
                response.setData(found);
                response.setCode(ResponseCode.SUCCESS);
                response.setMessage(messages.get("api.operations.select.responses.ok"));
                return ResponseEntity.ok(response);
            } else {
                response.setCode(ResponseCode.ERROR);
                response.setMessage(messages.get("api.operations.select.responses.not_found"));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception ex) {
            log.error("Select by id error", ex);
            response.setCode(ResponseCode.ERROR);
            response.setMessage(messages.get("api.operations.select.responses.error"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update 권한: ADMIN만 가능
     */
    @PutMapping("/{id}")
    @Operation(
        summary     = "{api.operations.update.summary|" + ENTITY_TOKEN + "}",
        description = "{api.operations.update.description|" + ENTITY_TOKEN + "}"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description  = "{api.operations.update.responses.ok|" + ENTITY_TOKEN + "}",
            content      = @Content(mediaType = "application/json",
                                  schema = @Schema(implementation = CommonResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "{api.operations.update.responses.bad_request}"),
        @ApiResponse(responseCode = "404", description = "{api.operations.update.responses.not_found|" + ENTITY_TOKEN + "}"),
        @ApiResponse(responseCode = "500", description = "{api.operations.update.responses.error}")
    })
    public ResponseEntity<?> updateById(@PathVariable Long id, @RequestBody CommonRequest<Item> request) {
        CommonResponse response = CommonResponse.builder().build();
        try {
            if (id == null || request == null || request.getData() == null) {
                response.setCode(ResponseCode.ERROR);
                response.setMessage(messages.get("api.operations.update.responses.bad_request"));
                return ResponseEntity.badRequest().body(response);
            }

            Item found = cachedItemService.selectById(id);
            if (found == null) {
                response.setCode(ResponseCode.ERROR);
                response.setMessage(messages.get("api.operations.update.responses.not_found"));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Item candidate = objectMapper.convertValue(request.getData(), Item.class);
            candidate.setId(id);
            cachedItemService.update(candidate);

            response.setData(candidate);
            response.setCode(ResponseCode.SUCCESS);
            response.setMessage(messages.get("api.operations.update.responses.ok"));
            log.info(response.getMessage());
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Update error", ex);
            response.setCode(ResponseCode.ERROR);
            response.setMessage(messages.get("api.operations.update.responses.error"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete 권한: ADMIN만 가능
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary     = "{api.operations.delete.summary|" + ENTITY_TOKEN + "}",
        description = "{api.operations.delete.description|" + ENTITY_TOKEN + "}"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description  = "{api.operations.delete.responses.ok|" + ENTITY_TOKEN + "}",
            content      = @Content(mediaType = "application/json",
                                  schema = @Schema(implementation = CommonResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "{api.operations.delete.responses.not_found|" + ENTITY_TOKEN + "}"),
        @ApiResponse(responseCode = "500", description = "{api.operations.delete.responses.error}")
    })
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        CommonResponse response = CommonResponse.builder().build();
        try {
            Item found = cachedItemService.selectById(id);
            if (found == null) {
                response.setCode(ResponseCode.ERROR);
                response.setMessage(messages.get("api.operations.delete.responses.not_found"));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            cachedItemService.deleteById(id);
            response.setData(found);
            response.setCode(ResponseCode.SUCCESS);
            response.setMessage(messages.get("api.operations.delete.responses.ok"));
            log.info(response.getMessage());
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Delete error", ex);
            response.setCode(ResponseCode.ERROR);
            response.setMessage(messages.get("api.operations.delete.responses.error"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}