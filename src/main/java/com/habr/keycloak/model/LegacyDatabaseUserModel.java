package com.habr.keycloak.model;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.adapter.AbstractUserAdapter;

import java.util.*;

/**
 * @author Roman Chigvintsev
 */
public class LegacyDatabaseUserModel extends AbstractUserAdapter {
    public static final String ATTRIBUTE_PASSWORD = "password";

    private final MultivaluedHashMap<String, String> attributes = new MultivaluedHashMap<>();
    private final Set<RoleModel> roles;

    private LegacyDatabaseUserModel(Builder builder) {
        super(builder.session, builder.realm, builder.storageProviderModel);
        attributes.putSingle(UserModel.USERNAME, builder.username);
        attributes.putSingle(UserModel.FIRST_NAME, builder.firstName);
        attributes.putSingle(UserModel.LAST_NAME, builder.lastName);
        attributes.putSingle(ATTRIBUTE_PASSWORD, builder.password);
        this.roles = Collections.unmodifiableSet(builder.roles);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String getUsername() {
        return getFirstAttribute(UserModel.USERNAME);
    }

    @Override
    public String getFirstName() {
        return getFirstAttribute(UserModel.FIRST_NAME);
    }

    @Override
    public String getLastName() {
        return getFirstAttribute(UserModel.LAST_NAME);
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        return new MultivaluedHashMap<>(attributes);
    }

    @Override
    public String getFirstAttribute(String name) {
        return attributes.getFirst(name);
    }

    @Override
    public List<String> getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    protected Set<RoleModel> getRoleMappingsInternal() {
        return roles;
    }

    public static class Builder {
        private KeycloakSession session;
        private RealmModel realm;
        private ComponentModel storageProviderModel;
        private String username;
        private String password;
        private String firstName;
        private String lastName;

        private final Set<RoleModel> roles = new HashSet<>();

        public LegacyDatabaseUserModel build() {
            return new LegacyDatabaseUserModel(this);
        }

        public Builder session(KeycloakSession session) {
            this.session = session;
            return this;
        }

        public Builder realm(RealmModel realm) {
            this.realm = realm;
            return this;
        }

        public Builder storageProviderModel(ComponentModel storageProviderModel) {
            this.storageProviderModel = storageProviderModel;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder withRole(RoleModel role) {
            this.roles.add(role);
            return this;
        }
    }
}
