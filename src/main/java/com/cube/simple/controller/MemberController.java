package com.cube.simple.controller;

import java.util.List;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;
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

import com.cube.simple.dto.CommonRequest;
import com.cube.simple.dto.CommonResponse;
import com.cube.simple.mapper.read.ReadMemberMapper;
import com.cube.simple.mapper.write.WriteMemberMapper;
import com.cube.simple.model.Member;
import com.cube.simple.service.MemberService;
import com.cube.simple.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/members")
@SecurityRequirement(name = "JWT")
@Tag(name = "Members", description = "사용자 CRUD API")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReadMemberMapper readMemberMapper;

    @Autowired
    private WriteMemberMapper writeMemberMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final DigestUtils digest = new DigestUtils("SHA-256");

    /**
     * Create 권한: ADMIN만 가능
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "새 사용자 등록",
        description = "CommonRequest DTO로 전달된 데이터를 기반으로 새 사용자를 저장함"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "등록 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    public ResponseEntity<?> insert(@RequestBody CommonRequest request) {
        CommonResponse response = CommonResponse.builder().build();
        try {
        	if (Objects.nonNull (request) && Objects.nonNull (request.getData())) {
                Member candidate = objectMapper.convertValue(request.getData(), Member.class);
                memberService.insert(candidate);
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
     * Read 권한: ADMIN 가능
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "모든 사용자 조회", description = "등록된 모든 사용자 목록을 반환함")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    public ResponseEntity<?> selectAll() {
        CommonResponse response = CommonResponse.builder().build();
        try {
            List<Member> members = memberService.selectAll();
            response.setData(members);
            response.setStatus(true);
            response.setMessage(String.format("Select success : %d members", members.size()));
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
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "ID로 사용자 조회", description = "PathVariable로 전달된 ID의 사용자를 반환함")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 ID"),
        @ApiResponse(responseCode = "404", description = "사용자 없음"),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    public ResponseEntity<?> selectById(@PathVariable String id) {
        CommonResponse response = CommonResponse.builder().build();
        try {
            if (Objects.isNull(id)) {
                response.setStatus(false);
                response.setMessage("Select error : id is null");
                return ResponseEntity.badRequest().body(response);
            }
            Member member = memberService.selectById(id);
            if (Objects.nonNull(member)) {
                response.setData(member);
                response.setStatus(true);
                response.setMessage(String.format("Select success : %s", member));
                return ResponseEntity.ok(response);
            } else {
                response.setStatus(false);
                response.setMessage(String.format("Select error : member not found for id=%d", id));
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
     * Update 권한: USER, ADMIN만 가능
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
        summary = "사용자 수정",
        description = "PathVariable로 전달된 ID의 사용자를, RequestBody로 전달된 데이터로 수정함"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 ID 또는 요청"),
        @ApiResponse(responseCode = "404", description = "사용자가 존재하지 않음"),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    public ResponseEntity<?> update(
            @PathVariable String id,
            @RequestBody CommonRequest request) {

        CommonResponse response = CommonResponse.builder().build();
        try {
            if (Objects.isNull(id) || Objects.isNull(request) || Objects.isNull(request.getData())) {
                response.setStatus(false);
                response.setMessage("Update error : invalid id or request");
                return ResponseEntity.badRequest().body(response);
            }
            Member found = memberService.selectById(id);
            if (Objects.isNull(found)) {
                response.setStatus(false);
                response.setMessage(String.format("Update error : no member for id=%d", id));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Member candidate = objectMapper.convertValue(request.getData(), Member.class);
            candidate.setId(id);
            memberService.update(candidate);
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
     * Delete 권한: ADMIN만 가능
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "사용자 삭제",
        description = "PathVariable로 전달된 ID의 사용자를 삭제함"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 ID"),
        @ApiResponse(responseCode = "404", description = "사용자가 존재하지 않음"),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    public ResponseEntity<CommonResponse> delete(@PathVariable String id) {
        CommonResponse response = CommonResponse.builder().build();
        try {
            if (Objects.isNull(id)) {
                response.setStatus(false);
                response.setMessage("Delete error : id is null");
                return ResponseEntity.badRequest().body(response);
            }
            Member found = memberService.selectById(id);
            if (Objects.isNull(found)) {
                response.setStatus(false);
                response.setMessage(String.format("Delete error : no member for id=%d", id));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            memberService.deleteById(id);
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