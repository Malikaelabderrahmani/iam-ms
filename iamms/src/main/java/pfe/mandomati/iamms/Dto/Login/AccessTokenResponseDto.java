package pfe.mandomati.iamms.Dto.Login;

import org.keycloak.representations.AccessTokenResponse;
import lombok.Builder;
import lombok.Data;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;

import java.net.URL;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AccessTokenResponseDto {

    private static final String KEYCLOAK_JWKS_URL = "https://elaoumrani:8444/realms/SchoolManagement/protocol/openid-connect/certs";

    private String accessToken;
    private String roleName;

    public static AccessTokenResponseDto toAccessTokenResponseDTO(AccessTokenResponse accessTokenResponse) {
        String roleName = extractRoleFromToken(accessTokenResponse.getToken());
        return AccessTokenResponseDto.builder()
                .accessToken(accessTokenResponse.getToken())
                .roleName(roleName)
                .build();
    }

    /**
     * Vérifie la signature du token JWT et extrait le rôle de l'utilisateur.
     */
    private static String extractRoleFromToken(String token) {
        try {
            // 1️⃣ Parser le JWT pour obtenir le `kid`
            SignedJWT signedJWT = SignedJWT.parse(token);
            String kid = signedJWT.getHeader().getKeyID(); // Récupérer l'ID de la clé
            PublicKey publicKey = getKeycloakPublicKey(kid); // Charger la clé publique
    
            // 2️⃣ Vérifier la signature et extraire les claims
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
    
            // 3️⃣ Récupérer `clientId` (l'application qui a émis le token)
            String clientId = claims.get("azp", String.class);
            System.out.println("Client ID utilisé : " + clientId);
    
            // 4️⃣ Extraire les rôles de `resource_access`
            Map<String, Object> resourceAccess = claims.get("resource_access", Map.class);
            System.out.println("Contenu de resource_access : " + resourceAccess);
    
            if (resourceAccess != null && clientId != null) {
                Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);
                if (clientAccess != null) {
                    List<String> roles = (List<String>) clientAccess.get("roles");
                    if (roles != null && !roles.isEmpty()) {
                        System.out.println("Rôle trouvé dans resource_access : " + roles.get(0));
                        return roles.get(0); // Retourne le premier rôle trouvé
                    }
                }
            }
    
            // 5️⃣ Si `resource_access` ne contient pas de rôle, essayer `realm_access`
            Map<String, Object> realmAccess = claims.get("realm_access", Map.class);
            if (realmAccess != null) {
                List<String> realmRoles = (List<String>) realmAccess.get("roles");
                if (realmRoles != null && !realmRoles.isEmpty()) {
                    System.out.println("Rôle trouvé dans realm_access : " + realmRoles.get(0));
                    return realmRoles.get(0); // Retourne le premier rôle trouvé dans `realm_access`
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        System.out.println("Aucun rôle trouvé, retour UNKNOWN_ROLE");
        return "UNKNOWN_ROLE"; // Si aucun rôle n'est trouvé
    }
    /**
     * Récupère la clé publique Keycloak en fonction du `kid` du token JWT.
     */
    private static PublicKey getKeycloakPublicKey(String kid) throws Exception {
        JWKSet jwkSet = JWKSet.load(new URL(KEYCLOAK_JWKS_URL));
        JWK jwk = jwkSet.getKeyByKeyId(kid);

        if (jwk == null) {
            throw new RuntimeException("Clé publique non trouvée pour le kid : " + kid);
        }

        return ((RSAKey) jwk).toPublicKey();
    }
}
