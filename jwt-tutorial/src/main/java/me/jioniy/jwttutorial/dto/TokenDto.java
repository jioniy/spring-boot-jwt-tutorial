package me.jioniy.jwttutorial.dto;
import lombok.*;

/**
 * Token 정보를 Response할 때 사용
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenDto {
    private String token;
}
