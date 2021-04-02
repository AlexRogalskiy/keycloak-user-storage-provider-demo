package com.habr.keycloak.storage;

import com.habr.keycloak.model.LegacyDatabaseRoleModel;
import com.habr.keycloak.model.LegacyDatabaseUserModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Roman Chigvintsev
 */
class LegacyDatabaseUserStorageProviderTest {
    private EmbeddedDatabase embeddedDatabase;
    private LegacyDatabaseUserStorageProvider provider;

    @BeforeEach
    void setUp() {
        ClientModel client = mock(ClientModel.class);
        KeycloakContext context = mock(KeycloakContext.class);
        when(context.getClient()).thenReturn(client);
        KeycloakSession session = mock(KeycloakSession.class);
        when(session.getContext()).thenReturn(context);
        ComponentModel componentModel = new ComponentModel();
        embeddedDatabase = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addDefaultScripts()
                .build();
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        provider = new LegacyDatabaseUserStorageProvider(session, componentModel, embeddedDatabase, passwordEncoder);
    }

    @AfterEach
    void tearDown() {
        embeddedDatabase.shutdown();
    }

    @Test
    void shouldLoadUserByName() {
        RealmModel realm = mock(RealmModel.class);
        when(realm.getId()).thenReturn("SdHDHesCk4");

        UserModel user = provider.getUserByUsername("test", realm);
        assertNotNull(user);
        assertEquals("test", user.getUsername());
    }

    @Test
    void shouldLoadUserById() {
        RealmModel realm = mock(RealmModel.class);
        when(realm.getId()).thenReturn("SdHDHesCk4");

        String username = "test";
        StorageId storageId = new StorageId("lB497ShddT", username);

        UserModel user = provider.getUserById(storageId.getId(), realm);
        assertNotNull(user);
        assertEquals(username, user.getUsername());
    }

    @Test
    void shouldLoadUserRoles() {
        RealmModel realm = mock(RealmModel.class);
        when(realm.getId()).thenReturn("SdHDHesCk4");

        Set<RoleModel> expectedRoles = new HashSet<>();
        expectedRoles.add(new LegacyDatabaseRoleModel(realm, "ROLE_USER"));
        expectedRoles.add(new LegacyDatabaseRoleModel(realm, "ROLE_HAMLET"));

        UserModel user = provider.getUserByUsername("test", realm);
        assertNotNull(user);
        assertEquals(expectedRoles, user.getRoleMappingsStream().collect(Collectors.toSet()));
    }

    @Test
    void shouldReturnNullWhenUserIsNotFoundByName() {
        RealmModel realm = mock(RealmModel.class);
        when(realm.getId()).thenReturn("SdHDHesCk4");
        assertNull(provider.getUserByUsername("#####", realm));
    }

    @Test
    void shouldBeConfiguredForPasswordCredentialType() {
        provider.isConfiguredFor(null, null, PasswordCredentialModel.TYPE);
    }

    @Test
    void shouldSupportPasswordCredentialType() {
        provider.supportsCredentialType(PasswordCredentialModel.TYPE);
    }

    @Test
    void shouldValidatePassword() {
        RealmModel realm = mock(RealmModel.class);
        when(realm.getId()).thenReturn("SdHDHesCk4");

        String password = "secret";

        UserModel user = mock(UserModel.class);
        when(user.getUsername()).thenReturn("test");
        when(user.getFirstAttribute(LegacyDatabaseUserModel.ATTRIBUTE_PASSWORD)).thenReturn(password);

        CredentialInput credentialInput = mock(CredentialInput.class);
        when(credentialInput.getType()).thenReturn(PasswordCredentialModel.TYPE);
        when(credentialInput.getChallengeResponse()).thenReturn(password);

        assertTrue(provider.isValid(realm, user, credentialInput));
    }

    @Test
    void shouldIgnoreUnsupportedCredentialTypeOnValidation() {
        CredentialInput credentialInput = mock(CredentialInput.class);
        when(credentialInput.getType()).thenReturn("retina");
        assertFalse(provider.isValid(null, null, credentialInput));
    }
}
