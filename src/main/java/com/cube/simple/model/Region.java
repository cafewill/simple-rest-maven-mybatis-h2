package com.cube.simple.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Region
 * - name: 다국어 JSON(문자열로 보관: {"ko":"서울","en":"Seoul","zh":"首爾"})
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Region {
    private Long id;
    /** JSON 문자열 */
    private String name;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}
