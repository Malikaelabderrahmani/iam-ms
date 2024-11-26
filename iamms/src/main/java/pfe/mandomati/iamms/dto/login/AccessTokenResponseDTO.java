package com.example.demo.dto.login;

import lombok.Builder;
import lombok.Data;
import org.keycloak.representations.AccessTokenResponse;

@Data
@Builder
public class AccessTokenResponseDTO {
    private String accessToken;

    private long expiresIn;

    private long refreshExpiresIn;

    private String refreshToken;

    private String tokenType;

    public static AccessTokenResponseDTO toAccessTokenResponseDTO(
            AccessTokenResponse accessTokenResponse) {
        return AccessTokenResponseDTO.builder()
                .accessToken(accessTokenResponse.getToken())
                .expiresIn(accessTokenResponse.getExpiresIn())
                .refreshToken(accessTokenResponse.getRefreshToken())
                .refreshExpiresIn(accessTokenResponse.getRefreshExpiresIn())
                .build();
    }
}
