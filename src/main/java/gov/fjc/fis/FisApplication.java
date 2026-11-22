package gov.fjc.fis;

import com.google.common.base.Strings;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.lumo.Lumo;
import io.jmix.flowui.theme.lumo.JmixLumo;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/**
 * Primary Spring Boot application entry point for the Financial Information
 * System (FIS).
 *
 * <p>This class bootstraps the application, configures the Vaadin and Jmix UI
 * environment, and defines the primary data source used by the application.
 * It also serves as the application's Vaadin {@link AppShellConfigurator},
 * enabling Progressive Web App (PWA) features, server push support, and
 * application-wide theme configuration.
 *
 * <p>During startup, the application logs the URL used to access the running
 * instance, including the configured server port and context path.
 */
@Push
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet(JmixLumo.STYLESHEET)
@StyleSheet("themes/fis-lumo/styles.css")
@PWA(name = "Fis", shortName = "Fis", offline = false)
@SpringBootApplication
public class FisApplication implements AppShellConfigurator {

    @Autowired
    private Environment environment;

    /**
     * Starts the FIS application.
     *
     * @param args command-line arguments supplied to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(FisApplication.class, args);
    }

    /**
     * Creates the primary {@link DataSourceProperties} bean used to configure the
     * application's main data source.
     *
     * @return the configured data source properties
     */
    @Bean
    @Primary
    @ConfigurationProperties("main.datasource")
    DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Creates the primary {@link DataSource} for the application using the
     * configured data source properties.
     *
     * @param dataSourceProperties the data source configuration properties
     * @return the application's primary data source
     */
    @Bean
    @Primary
    @ConfigurationProperties("main.datasource.hikari")
    DataSource dataSource(final DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

    /**
     * Logs the application URL after startup completes.
     *
     * <p>The URL includes the dynamically assigned server port and configured
     * servlet context path, allowing administrators and developers to quickly
     * locate the running application instance.
     *
     * @param event the application started event
     */
    @EventListener
    public void printApplicationUrl(final ApplicationStartedEvent event) {
        LoggerFactory.getLogger(FisApplication.class).info("Application started at "
                + "http://localhost:"
                + environment.getProperty("local.server.port")
                + Strings.nullToEmpty(environment.getProperty("server.servlet.context-path")));
    }
}
