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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cube.simple.dto.CommonRequest;
import com.cube.simple.dto.CommonResponse;
import com.cube.simple.enums.ResponseCode;
import com.cube.simple.model.Item;
import com.cube.simple.service.CachedItemService;
import com.cube.simple.service.ItemService;
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
@RequestMapping("/api/items")
@SecurityRequirement(name = "JWT")
// @Tag(name = "Items", description = "아이템 CRUD API")
public class ItemController {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ItemService itemService;

    @Autowired
    private CachedItemService cachedItemService;

    /**
     * Create 권한: ADMIN만 가능
     */
    @PostMapping
    @Operation(
        summary     = "api.item.insert.summary",
        description = "api.item.insert.description"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "api.item.insert.responses.ok",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "400", description = "api.item.insert.responses.bad_request"),
        @ApiResponse(responseCode = "500", description = "api.item.insert.responses.error")
    })    
    public ResponseEntity<?> insert(@Valid @RequestBody CommonRequest <Item> request) {
    	
        CommonResponse response = CommonResponse.builder().build();
        
        try {
            if (Objects.nonNull (request) && Objects.nonNull (request.getData())) {
                Item candidate = objectMapper.convertValue(request.getData(), Item.class);
                itemService.insert(candidate);
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
     * Read 권한: USER, ADMIN 가능
     */
    @GetMapping
    @Operation(
        summary     = "api.item.selectAll.summary",
        description = "api.item.selectAll.description"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "api.item.selectAll.responses.ok",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "500", description = "api.item.selectAll.responses.error")
    })
    public ResponseEntity<?> selectAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search
		) {
    	
        CommonResponse response = CommonResponse.builder().build();
        
        try {
            List<Item> items = itemService.selectAll(page, size, category, search);
            response.setData(items);
            response.setCode(ResponseCode.SUCCESS);
            response.setMessage(String.format("Select success : %d items", items.size()));
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
    @Operation(
        summary     = "api.item.selectById.summary",
        description = "api.item.selectById.description"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "api.item.selectById.responses.ok",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "400", description = "api.item.selectById.responses.bad_request"),
        @ApiResponse(responseCode = "404", description = "api.item.selectById.responses.not_found"),
        @ApiResponse(responseCode = "500", description = "api.item.selectById.responses.error")
    })    
    public ResponseEntity<?> selectById(@PathVariable Long id) {
    	
        CommonResponse response = CommonResponse.builder().build();
        
        try {
            if (Objects.isNull(id)) {
                response.setCode(ResponseCode.ERROR);
                response.setMessage("Select error : id is null");
                return ResponseEntity.badRequest().body(response);
            }
            Item item = itemService.selectById(id);
            if (Objects.nonNull(item)) {
                response.setData(item);
                response.setCode(ResponseCode.SUCCESS);
                response.setMessage(String.format("Select success : %s", item));
                return ResponseEntity.ok(response);
            } else {
                response.setCode(ResponseCode.ERROR);
                response.setMessage(String.format("Select error : item not found for id=%d", id));
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
     * Update 권한: ADMIN만 가능
     */
    @PutMapping("/{id}")
    @Operation(
        summary     = "api.item.updateById.summary",
        description = "api.item.updateById.description"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "api.item.updateById.responses.ok",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "400", description = "api.item.updateById.responses.bad_request"),
        @ApiResponse(responseCode = "404", description = "api.item.updateById.responses.not_found"),
        @ApiResponse(responseCode = "500", description = "api.item.updateById.responses.error")
    })
    public ResponseEntity<?> updateById(@PathVariable Long id, @RequestBody CommonRequest <Item> request) {

        CommonResponse response = CommonResponse.builder().build();
        
        try {
            if (Objects.isNull(id) || Objects.isNull(request) || Objects.isNull(request.getData())) {
                response.setCode(ResponseCode.ERROR);
                response.setMessage("Update error : invalid id or request");
                return ResponseEntity.badRequest().body(response);
            }
            Item found = itemService.selectById(id);
            if (Objects.isNull(found)) {
                response.setCode(ResponseCode.ERROR);
                response.setMessage(String.format("Update error : no item for id=%d", id));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Item candidate = objectMapper.convertValue(request.getData(), Item.class);
            candidate.setId(id);
            itemService.update(candidate);

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
    @Operation(
        summary     = "api.item.deleteById.summary",
        description = "api.item.deleteById.description"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "api.item.deleteById.responses.ok",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "400", description = "api.item.deleteById.responses.bad_request"),
        @ApiResponse(responseCode = "404", description = "api.item.deleteById.responses.not_found"),
        @ApiResponse(responseCode = "500", description = "api.item.deleteById.responses.error")
    })
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
    	
        CommonResponse response = CommonResponse.builder().build();
        
        try {
            if (Objects.isNull(id)) {
                response.setCode(ResponseCode.ERROR);
                response.setMessage("Delete error : id is null");
                return ResponseEntity.badRequest().body(response);
            }
            Item found = itemService.selectById(id);
            if (Objects.isNull(found)) {
                response.setCode(ResponseCode.ERROR);
                response.setMessage(String.format("Delete error : no item for id=%d", id));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            itemService.deleteById(id);
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
