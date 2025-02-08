package pfe.mandomati.iamms.Dto.Login;

import org.keycloak.representations.AccessTokenResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccessTokenResponseDto {

    private String accessToken;

    private long expiresIn;

    private long refreshExpiresIn;

    private String refreshToken;

    private String tokenType;

    public static AccessTokenResponseDto toAccessTokenResponseDTO(
            AccessTokenResponse accessTokenResponse) {
        return AccessTokenResponseDto.builder()
                .accessToken(accessTokenResponse.getToken())
                .expiresIn(accessTokenResponse.getExpiresIn())
                .refreshToken(accessTokenResponse.getRefreshToken())
                .refreshExpiresIn(accessTokenResponse.getRefreshExpiresIn())
                .build();
    }
}
