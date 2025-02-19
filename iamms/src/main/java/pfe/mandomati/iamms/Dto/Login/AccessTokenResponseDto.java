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
            String kid = signedJWT.getHeader().getKeyID();
            PublicKey publicKey = getKeycloakPublicKey(kid);
    
            // 2️⃣ Vérifier la signature et extraire les claims
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
    
            // ✅ LOG : Afficher tous les claims pour voir ce qui est dedans
            System.out.println("🔹 JWT Claims: " + claims);
    
            // 3️⃣ Récupérer `clientId` (`azp`)
            String clientId = claims.get("azp", String.class);
            System.out.println("🔹 Client ID utilisé (azp) : " + clientId);
    
            // 4️⃣ Extraire `resource_access`
            Map<String, Object> resourceAccess = claims.get("resource_access", Map.class);
            System.out.println("🔹 Contenu de resource_access : " + resourceAccess);
    
            if (resourceAccess != null && clientId != null) {
                Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);
                System.out.println("🔹 Accès au client (" + clientId + ") : " + clientAccess);
    
                if (clientAccess != null) {
                    List<String> roles = (List<String>) clientAccess.get("roles");
                    System.out.println("🔹 Rôles trouvés : " + roles);
    
                    if (roles != null && !roles.isEmpty()) {
                        System.out.println("✅ Rôle retourné : " + roles.get(0));
                        return roles.get(0); 
                    }
                } else {
                    System.out.println("❌ Aucun accès trouvé pour clientId: " + clientId);
                }
            }
    
            // 5️⃣ Si `resource_access` est vide, essayer `realm_access`
            Map<String, Object> realmAccess = claims.get("realm_access", Map.class);
            System.out.println("🔹 Contenu de realm_access : " + realmAccess);
    
            if (realmAccess != null) {
                List<String> realmRoles = (List<String>) realmAccess.get("roles");
                System.out.println("🔹 Rôles du realm trouvés : " + realmRoles);
    
                if (realmRoles != null && !realmRoles.isEmpty()) {
                    System.out.println("✅ Rôle retourné depuis realm_access : " + realmRoles.get(0));
                    return realmRoles.get(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        System.out.println("❌ Aucun rôle trouvé, retour UNKNOWN_ROLE");
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
