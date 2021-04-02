package com.habr.keycloak.storage;

import com.habr.keycloak.model.LegacyDatabaseRoleModel;
import com.habr.keycloak.model.LegacyDatabaseUserModel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Roman Chigvintsev
 */
public class LegacyDatabaseUserStorageProvider
        implements UserStorageProvider, UserLookupProvider, CredentialInputValidator {
    private static final Logger log = Logger.getLogger(LegacyDatabaseUserStorageProvider.class);

    private static final String SQL_FIND_USER_BY_NAME = "SELECT u.username, u.password, u.first_name, u.last_name, a.authority "
            + "FROM users u "
            + "INNER JOIN authorities a ON a.username = u.username "
            + "WHERE u.username = ?";

    private final KeycloakSession session;
    private final ComponentModel storageProviderModel;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final ConcurrentMap<UserModelKey, LegacyDatabaseUserModel> loadedUsers = new ConcurrentHashMap<>();

    public LegacyDatabaseUserStorageProvider(KeycloakSession session,
                                             ComponentModel storageProviderModel,
                                             DataSource dataSource,
                                             PasswordEncoder passwordEncoder) {
        Assert.notNull(session, "Session must not be null");
        Assert.notNull(storageProviderModel, "Storage provider must not be null");
        Assert.notNull(dataSource, "Data source must not be null");
        Assert.notNull(passwordEncoder, "Password encoder must not be null");

        this.session = session;
        this.storageProviderModel = storageProviderModel;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        if (!supportsCredentialType(credentialInput.getType())) {
            log.debugv("Credential type \"{0}\" is not supported", credentialInput.getType());
            return false;
        }
        String password = user.getFirstAttribute(LegacyDatabaseUserModel.ATTRIBUTE_PASSWORD);
        return passwordEncoder.matches(credentialInput.getChallengeResponse(), password);
    }

    @Override
    public void close() {
        // Do nothing
    }

    @Override
    public LegacyDatabaseUserModel getUserById(String id, RealmModel realm) {
        StorageId storageId = new StorageId(id);
        String username = storageId.getExternalId();
        return getUserByUsername(username, realm);
    }

    @Override
    public LegacyDatabaseUserModel getUserByUsername(String username, RealmModel realm) {
        UserModelKey userKey = new UserModelKey(username, realm.getId());
        return loadedUsers.computeIfAbsent(userKey, k -> {
            LegacyDatabaseUserModel user = findUserByName(username, realm);
            if (user != null) {
                log.debugv("User is loaded by name \"{0}\"", username);
            }
            return user;
        });
    }

    @Override
    public UserModel getUserByEmail(String email, RealmModel realm) {
        // Unsupported
        return null;
    }

    private LegacyDatabaseUserModel findUserByName(String username, RealmModel realm) {
        return jdbcTemplate.query(SQL_FIND_USER_BY_NAME, new Object[]{username}, new int[]{Types.VARCHAR},
                new LegacyDatabaseUserModelResultSetExtractor(realm));
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    private static class UserModelKey {
        private final String username;
        private final String realmId;
    }

    @RequiredArgsConstructor
    private class LegacyDatabaseUserModelResultSetExtractor implements ResultSetExtractor<LegacyDatabaseUserModel> {
        final RealmModel realm;

        @Override
        public LegacyDatabaseUserModel extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (!rs.next()) {
                return null;
            }

            LegacyDatabaseUserModel.Builder userModelBuilder = LegacyDatabaseUserModel.builder()
                    .session(session)
                    .realm(realm)
                    .storageProviderModel(storageProviderModel)
                    .username(rs.getString(1))
                    .password(rs.getString(2))
                    .firstName(rs.getString(3))
                    .lastName(rs.getString(4))
                    .withRole(new LegacyDatabaseRoleModel(realm, rs.getString(5)));

            while (rs.next()) {
                userModelBuilder.withRole(new LegacyDatabaseRoleModel(realm, rs.getString(5)));
            }

            return userModelBuilder.build();
        }
    }
}
