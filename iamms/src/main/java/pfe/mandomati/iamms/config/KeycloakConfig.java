package com.example.demo.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

@Getter
@Configuration
public class KeycloakConfig {


    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    private Keycloak keycloakInstance;


    @PostConstruct
    public void init() {
        this.keycloakInstance = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }

    public static CredentialRepresentation createPasswordCredentials(String password) {
        CredentialRepresentation credentials = new CredentialRepresentation();
        credentials.setTemporary(false);
        credentials.setType(CredentialRepresentation.PASSWORD);
        credentials.setValue(password);
        return credentials;
    }

    public AuthzClient getAuthzClient() {
        Map<String, Object> clientCredentials = new HashMap<>();
        clientCredentials.put("secret", clientSecret);
        clientCredentials.put("grant_type", "password");

        org.keycloak.authorization.client.Configuration configuration =
                new org.keycloak.authorization.client.Configuration(
                        serverUrl, realm, clientId, clientCredentials, null);
        return AuthzClient.create(configuration);
    }

    public Keycloak getInstance() {
        return keycloakInstance;
    }
}