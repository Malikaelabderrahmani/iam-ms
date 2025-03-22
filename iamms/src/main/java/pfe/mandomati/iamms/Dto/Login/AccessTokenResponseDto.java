package pfe.mandomati.iamms.Dto.Login;

import org.keycloak.representations.AccessTokenResponse;
import lombok.Builder;
import lombok.Data;
// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.Jwts;
// import com.nimbusds.jose.jwk.JWK;
// import com.nimbusds.jose.jwk.JWKSet;
// import com.nimbusds.jose.jwk.RSAKey;
// import com.nimbusds.jwt.SignedJWT;

// import java.net.URL;
// import java.security.PublicKey;
// import java.security.interfaces.RSAPublicKey;
// import java.util.List;
// import java.util.Map;

@Data
@Builder
public class AccessTokenResponseDto {

    //private static final String KEYCLOAK_JWKS_URL = "https://elaoumrani:8444/realms/SchoolManagement/protocol/openid-connect/certs";

    private String accessToken;
    private String roleName;

    public static AccessTokenResponseDto toAccessTokenResponseDTO(AccessTokenResponse accessTokenResponse) {
        //String roleName = extractRoleFromToken(accessTokenResponse.getToken());
        return AccessTokenResponseDto.builder()
                .accessToken(accessTokenResponse.getToken())
                //.roleName(roleName)
                .build();
    }

    /**
     * Vérifie la signature du token JWT et extrait le rôle de l'utilisateur.
     */
    // private static String extractRoleFromToken(String token) {
    //     try {
    //         // 1️ Parser le JWT pour obtenir le `kid`
    //         SignedJWT signedJWT = SignedJWT.parse(token);
    //         String kid = signedJWT.getHeader().getKeyID(); // Récupérer l'ID de la clé
    //         PublicKey publicKey = getKeycloakPublicKey(kid); // Charger la clé publique

    //         // 2️ Vérifier la signature
    //         Claims claims = Jwts.parserBuilder()
    //                 .setSigningKey(publicKey)
    //                 .build()
    //                 .parseClaimsJws(token)
    //                 .getBody();

    //         // 3️ Extraire le rôle de l'utilisateur
    //         Map<String, Object> resourceAccess = claims.get("resource_access", Map.class);
    //         if (resourceAccess != null) {
    //             Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get("client"); // Remplace "client"
    //             if (clientAccess != null) {
    //                 List<String> roles = (List<String>) clientAccess.get("roles");
    //                 if (roles != null && !roles.isEmpty()) {
    //                     return roles.get(0);
    //                 }
    //             }
    //         }
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    //     return "UNKNOWN_ROLE";
    // }

    // /**
    //  * Récupère la clé publique Keycloak en fonction du `kid` du token JWT.
    //  */
    // private static PublicKey getKeycloakPublicKey(String kid) throws Exception {
    //     JWKSet jwkSet = JWKSet.load(new URL(KEYCLOAK_JWKS_URL));
    //     JWK jwk = jwkSet.getKeyByKeyId(kid);

    //     if (jwk == null) {
    //         throw new RuntimeException("Clé publique non trouvée pour le kid : " + kid);
    //     }

    //     return ((RSAKey) jwk).toPublicKey();
    // }
}
