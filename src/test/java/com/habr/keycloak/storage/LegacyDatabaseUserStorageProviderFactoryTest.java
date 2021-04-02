package com.habr.keycloak.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.springframework.core.env.PropertiesPropertySource;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * @author Roman Chigvintsev
 */
class LegacyDatabaseUserStorageProviderFactoryTest {
    private Properties pluginProperties;
    private LegacyDatabaseUserStorageProviderFactory providerFactory;

    @BeforeEach
    void setUp() {
        pluginProperties = new Properties();
        providerFactory = new LegacyDatabaseUserStorageProviderFactory();
        providerFactory.setPropertySource(new PropertiesPropertySource("plugin", pluginProperties));
    }

    @Test
    void shouldCreateUserStorageProvider() {
        pluginProperties.setProperty("legacy-database.datasource.url", "jdbc:test1");
        pluginProperties.setProperty("legacy-database.datasource.driver-class-name",
                TestJdbcDriver.class.getName());
        pluginProperties.setProperty("legacy-database.datasource.username", "user");
        pluginProperties.setProperty("legacy-database.datasource.password", "secret");

        providerFactory.init(null);
        KeycloakSession session = mock(KeycloakSession.class);
        ComponentModel componentModel = mock(ComponentModel.class);
        assertNotNull(providerFactory.create(session, componentModel));
    }

    @Test
    void shouldThrowExceptionOnInitWhenJdbcDriverClassIsNotFound() {
        String driverClassName = "org.fairytale.ImaginaryDriver";
        pluginProperties.setProperty("legacy-database.datasource.driver-class-name", driverClassName);
        pluginProperties.setProperty("legacy-database.datasource.url", "jdbc:test1");

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> providerFactory.init(null));
        assertEquals("JDBC driver class \"" + driverClassName + "\" is not found", e.getMessage());
    }

    public static class TestJdbcDriver implements Driver {
        @Override
        public Connection connect(String url, Properties info) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean acceptsURL(String url) {
            return false;
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
            return new DriverPropertyInfo[0];
        }

        @Override
        public int getMajorVersion() {
            return 1;
        }

        @Override
        public int getMinorVersion() {
            return 0;
        }

        @Override
        public boolean jdbcCompliant() {
            return false;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }
    }
}
