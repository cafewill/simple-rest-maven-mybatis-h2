package com.cube.simple.controller.user;

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
import com.cube.simple.model.user.Place;
import com.cube.simple.service.user.PlaceService;
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
@RestController("userPlaceController")
@RequestMapping("/api/user/places")
public class PlaceController {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private PlaceService placeService;

    /**
     * Create 권한: ADMIN 만 가능
     */
    @PostMapping
    @Operation(
            summary     = "{api.operations.insert.summary}",
            description = "{api.operations.insert.description}",
            security    = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "{api.operations.insert.responses.ok}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "400", description = "{api.operations.insert.responses.bad_request}"),
        @ApiResponse(responseCode = "500", description = "{api.operations.insert.responses.error}")
    })    
    public ResponseEntity<?> create(@Valid @RequestBody CommonRequest <Place> request) {
        
        CommonResponse response = CommonResponse.builder().build();

        try {
            if (!Objects.isNull (request) && !Objects.isNull (request.getData())) {
                Place candidate = objectMapper.convertValue(request.getData(), Place.class);
                placeService.insert(candidate);
                response.setData(candidate);
                response.setCode(ResponseCode.SUCCESS);
                response.setMessage(String.format("Create success : %s", candidate));
                log.info(response.getMessage());
                return ResponseEntity.ok(response);
            } else {
                response.setCode(ResponseCode.ERROR);
                response.setMessage(String.format("Create error : invalid request %s", request));
                log.warn(response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception ex) {
            response.setCode(ResponseCode.ERROR);
            response.setMessage(String.format("Create error : %s", ex.getLocalizedMessage()));
            log.error(response.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Read 권한: 모두 공개 (인증 없이 접근 가능)
     */
    @GetMapping
    @Operation(
            summary     = "{api.operations.select.summary}",
            description = "{api.operations.select.description}"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "{api.operations.select.responses.ok}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "500", description = "{api.operations.select.responses.error}")
    })        
    public ResponseEntity<?> selectAll(@RequestParam(name="lang", required=false, defaultValue="ko") String lang) {
        
        CommonResponse response = CommonResponse.builder().build();
        
        try {
            List<Place> places = placeService.selectAll(lang);
            response.setData(places);
            response.setCode(ResponseCode.SUCCESS);
            response.setMessage(String.format("Select success : %d items", places.size()));
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
            summary     = "{api.operations.select.summary}",
            description = "{api.operations.select.description}"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "{api.operations.select.responses.ok}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "400", description = "{api.operations.select.responses.bad_request}"),
        @ApiResponse(responseCode = "404", description = "{api.operations.select.responses.responses.not_found}"),
        @ApiResponse(responseCode = "500", description = "{api.operations.select.responses.error}")
    })        
    public ResponseEntity<?> selectById(@RequestParam(name="lang", required=false, defaultValue="ko") String lang, @PathVariable Long id) {
        
        CommonResponse response = CommonResponse.builder().build();
        
        try {
            if (Objects.isNull(id)) {
                response.setCode(ResponseCode.ERROR);
                response.setMessage("Select error : id is null");
                return ResponseEntity.badRequest().body(response);
            }
            Place place = placeService.selectById(lang, id);
            if (Objects.nonNull (place)) {
                response.setData(place);
                response.setCode(ResponseCode.SUCCESS);
                response.setMessage(String.format("Select success : %s", place));
                return ResponseEntity.ok(response);
            } else {
                response.setCode(ResponseCode.ERROR);
                response.setMessage(String.format("Select error : place not found for id=%d", id));
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
            summary     = "{api.operations.update.summary}",
            description = "{api.operations.update.description}",
            security    = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "{api.operations.update.responses.ok}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "400", description = "{api.operations.update.responses.bad_request}"),
        @ApiResponse(responseCode = "404", description = "{api.operations.update.responses.responses.not_found}"),
        @ApiResponse(responseCode = "500", description = "{api.operations.update.responses.error}")
    })        
    public ResponseEntity<?> updateById(@PathVariable Long id, @RequestBody CommonRequest <Place> request) {

        CommonResponse response = CommonResponse.builder().build();
        
        try {
            if (Objects.isNull(id) || Objects.isNull(request) || Objects.isNull(request.getData())) {
                response.setCode(ResponseCode.ERROR);
                response.setMessage("Update error : invalid id or request");
                return ResponseEntity.badRequest().body(response);
            }
            Place found = placeService.selectById("ko", id);
            if (Objects.isNull(found)) {
                response.setCode(ResponseCode.ERROR);
                response.setMessage(String.format("Update error : no place for id=%d", id));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Place candidate = objectMapper.convertValue(request.getData(), Place.class);
            candidate.setId(id);
            placeService.update(candidate);
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
            summary     = "{api.operations.delete.summary}",
            description = "{api.operations.delete.description}",
            security    = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "{api.operations.delete.responses.ok}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "400", description = "{api.operations.delete.responses.bad_request}"),
        @ApiResponse(responseCode = "404", description = "{api.operations.delete.responses.responses.not_found}"),
        @ApiResponse(responseCode = "500", description = "{api.operations.delete.responses.error}")
    })            
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        
        CommonResponse response = CommonResponse.builder().build();
        
        try {
            if (Objects.isNull(id)) {
                response.setCode(ResponseCode.ERROR);
                response.setMessage("Delete error : id is null");
                return ResponseEntity.badRequest().body(response);
            }
            Place found = placeService.selectById("ko", id);
            if (Objects.isNull(found)) {
                response.setCode(ResponseCode.ERROR);
                response.setMessage(String.format("Delete error : no place for id=%d", id));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            placeService.deleteById(id);
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