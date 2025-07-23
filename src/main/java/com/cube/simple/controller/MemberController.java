package com.cube.simple.controller;

import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.cube.simple.model.Member;
import com.cube.simple.model.Member;
import com.cube.simple.security.jwt.JwtUtil;
import com.cube.simple.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Members", description = "사용자 CRUD API")
@RestController
@RequestMapping("/api/members")
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
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(
        summary = "새 사용자 등록",
        description = "CommonRequest DTO로 전달된 데이터를 기반으로 새 사용자를 저장합니다."
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
    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping
    @Operation(summary = "모든 사용자 조회", description = "등록된 모든 사용자 목록을 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    public ResponseEntity<CommonResponse> selectAll() {
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
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "ID로 사용자 조회", description = "PathVariable로 전달된 ID의 사용자를 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CommonResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 ID"),
        @ApiResponse(responseCode = "404", description = "사용자 없음"),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    public ResponseEntity<CommonResponse> selectById(@PathVariable String id) {
        CommonResponse response = CommonResponse.builder().build();
        try {
            if (id == null) {
                response.setStatus(false);
                response.setMessage("Select error : id is null");
                return ResponseEntity.badRequest().body(response);
            }
            Member member = memberService.selectById(id);
            if (member != null) {
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
     * 사용자 정보 수정 (ROLE_USER는 본인 id만, ROLE_ADMIN은 전체)
     */
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateById(@PathVariable String id, @RequestBody Member member) {
        Member found = readMemberMapper.selectById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        // USER 권한일 경우 본인 확인
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))
            && !found.getName().equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        // 비밀번호 해시 변경 시
        if (member.getPassword() != null) {
            member.setPassword(digest.digestAsHex(member.getPassword()));
        }
        writeMemberMapper.update(member);
        return ResponseEntity.ok().build();
    }

    /**
     * ADMIN 권한: 사용자 삭제
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable String id) {
        writeMemberMapper.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}