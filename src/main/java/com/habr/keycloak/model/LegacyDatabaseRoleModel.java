package com.habr.keycloak.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.keycloak.models.RoleContainerModel;
import org.keycloak.models.RoleModel;
import org.keycloak.storage.ReadOnlyException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Roman Chigvintsev
 */
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class LegacyDatabaseRoleModel implements RoleModel {
    @Getter
    private final RoleContainerModel container;
    @Getter
    private final String name;

    @Override
    public String getId() {
        return getName();
    }

    @Override
    public void setName(String name) {
        throw new ReadOnlyException("Role is read only for this update");
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void setDescription(String description) {
        throw new ReadOnlyException("Role is read only for this update");
    }

    @Override
    public boolean isComposite() {
        return false;
    }

    @Override
    public void addCompositeRole(RoleModel role) {
        throw new ReadOnlyException("Role is read only for this update");
    }

    @Override
    public void removeCompositeRole(RoleModel role) {
        throw new ReadOnlyException("Role is read only for this update");
    }

    @Override
    public Stream<RoleModel> getCompositesStream() {
        return Stream.empty();
    }

    @Override
    public boolean isClientRole() {
        return false;
    }

    @Override
    public String getContainerId() {
        return container.getId();
    }

    @Override
    public boolean hasRole(RoleModel role) {
        return false;
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        return Collections.emptyMap();
    }

    @Override
    public void setSingleAttribute(String name, String value) {
        throw new ReadOnlyException("Role is read only for this update");
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        throw new ReadOnlyException("Role is read only for this update");
    }

    @Override
    public void removeAttribute(String name) {
        throw new ReadOnlyException("Role is read only for this update");
    }

    @Override
    public Stream<String> getAttributeStream(String name) {
        return Stream.empty();
    }
}
