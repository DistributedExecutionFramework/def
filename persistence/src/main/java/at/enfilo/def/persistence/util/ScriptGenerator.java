package at.enfilo.def.persistence.util;

import at.enfilo.def.config.util.ConfigReader;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;

import java.util.EnumSet;

public class ScriptGenerator {

    private static final String CONFIG_FILE = "database.yml";
    private static final String PROPERTY_DIALECT = "hibernate.dialect";
    private static final String PROPERTY_DRIVER = "hibernate.connection.driver_class";
    private static final String PROPERTY_URL = "hibernate.connection.url";
    private static final String PROPERTY_USER = "hibernate.connection.username";
    private static final String PROPERTY_PASSWORD = "hibernate.connection.password";

    public static void main(String[] args) {
        try {
            DatabaseConfiguration databaseConfig = ConfigReader.readConfiguration(
                    CONFIG_FILE, ConnectionProvider.class, DatabaseConfiguration.class
            );

            MetadataSources metadata = new MetadataSources(
                    new StandardServiceRegistryBuilder()
                            .configure("hibernate.cfg.xml")
                            .applySetting(PROPERTY_DRIVER, databaseConfig.getDriver())
                            .applySetting(PROPERTY_URL, databaseConfig.getUrl())
                            .applySetting(PROPERTY_USER, databaseConfig.getUser())
                            .applySetting(PROPERTY_PASSWORD, databaseConfig.getPassword())
                            .applySetting(PROPERTY_DIALECT, databaseConfig.getDialect())
                            .build());

            EnumSet<TargetType> targetTypes = EnumSet.of(TargetType.STDOUT);

            SchemaExport export = new SchemaExport();

            export.setDelimiter(";");
            export.setFormat(true);

            export.createOnly(targetTypes, metadata.buildMetadata());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
