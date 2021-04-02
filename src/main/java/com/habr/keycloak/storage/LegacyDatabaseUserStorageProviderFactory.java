package com.habr.keycloak.storage;

import lombok.Setter;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Driver;
import java.util.Map;

/**
 * @author Roman Chigvintsev
 */
public class LegacyDatabaseUserStorageProviderFactory
        implements UserStorageProviderFactory<LegacyDatabaseUserStorageProvider> {
    private static final Logger log = Logger.getLogger(LegacyDatabaseUserStorageProviderFactory.class);

    private static final String PROVIDER_ID = "habr.legacy-database";

    public static final String PROPERTY_DATASOURCE_DRIVER_CLASS_NAME = "legacy-database.datasource.driver-class-name";
    public static final String PROPERTY_DATASOURCE_URL = "legacy-database.datasource.url";
    public static final String PROPERTY_DATASOURCE_USERNAME = "legacy-database.datasource.username";
    public static final String PROPERTY_DATASOURCE_PASSWORD = "legacy-database.datasource.password";

    private DataSource dataSource;
    private PasswordEncoder passwordEncoder;

    @Setter
    private PropertySource<Map<String, Object>> propertySource;

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public void init(Config.Scope config) {
        initDataSource();
        initPasswordEncoder();
    }

    @Override
    public LegacyDatabaseUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new LegacyDatabaseUserStorageProvider(session, model, dataSource, passwordEncoder);
    }

    @SuppressWarnings("unchecked")
    private void initDataSource() {
        String driverClassName = getDataSourceDriverClassName();
        String url = getDataSourceUrl();

        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        try {
            dataSource.setDriverClass((Class<? extends Driver>) Class.forName(driverClassName));
            dataSource.setUrl(url);
            dataSource.setUsername(getDataSourceUsername());
            dataSource.setPassword(getDataSourcePassword());
            this.dataSource = dataSource;
            log.debugv("Data source to connect with database \"{0}\" is created", url);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("JDBC driver class \"" + driverClassName + "\" is not found", e);
        }
    }

    private void initPasswordEncoder() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    private PropertySource<Map<String, Object>> getPropertySource() {
        if (propertySource == null) {
            propertySource = getDefaultPropertySource();
        }
        return propertySource;
    }

    private PropertySource<Map<String, Object>> getDefaultPropertySource() {
        return new PropertiesPropertySource("default", System.getProperties());
    }

    private String getDataSourceDriverClassName() {
        String driverClassName = (String) getPropertySource().getProperty(PROPERTY_DATASOURCE_DRIVER_CLASS_NAME);
        if (!StringUtils.hasLength(driverClassName)) {
            throw new IllegalStateException("System property \"" + PROPERTY_DATASOURCE_DRIVER_CLASS_NAME
                    + "\" must be defined");
        }
        return driverClassName;
    }

    private String getDataSourceUrl() {
        String url = (String) getPropertySource().getProperty(PROPERTY_DATASOURCE_URL);
        if (!StringUtils.hasLength(url)) {
            throw new IllegalStateException("System property \"" + PROPERTY_DATASOURCE_URL
                    + "\" must be defined");
        }
        return url;
    }

    private String getDataSourceUsername() {
        return (String) getPropertySource().getProperty(PROPERTY_DATASOURCE_USERNAME);
    }

    private String getDataSourcePassword() {
        return (String) getPropertySource().getProperty(PROPERTY_DATASOURCE_PASSWORD);
    }
}
