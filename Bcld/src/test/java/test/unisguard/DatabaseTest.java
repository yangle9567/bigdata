package test.unisguard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.sf.log4jdbc.ConnectionSpy;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.platform.derby.DerbyPlatform;
import org.apache.ddlutils.platform.postgresql.PostgreSqlPlatform;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JDBCConnectionConfiguration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;

public class DatabaseTest {

    private static Properties properties;

    @Test
    public void testCreateDerbyTables() throws ClassNotFoundException, SQLException {
        Class.forName(properties.getProperty("derby.jdbc.driver"));
        Connection connection = new ConnectionSpy(DriverManager.getConnection(properties.getProperty("derby.jdbc.url")));
        Database database = new DatabaseIO().read("src/test/resources/BcldDb.xml");
        Platform platform = new DerbyPlatform();
        platform.createTables(connection, database, true, true);
    }

    @Test
    public void testCreatePostgresTables() throws ClassNotFoundException, SQLException {
        Class.forName(properties.getProperty("jdbc.driver"));
        Connection connection = new ConnectionSpy(DriverManager.getConnection(properties.getProperty("jdbc.url"), properties.getProperty("jdbc.username"), properties.getProperty("jdbc.password")));
        Database database = new DatabaseIO().read("src/test/resources/BcldDb.xml");
        Platform platform = new PostgreSqlPlatform();
        platform.createTables(connection, database, true, true);
    }

    @Test
    public void testAlterPostgresTables() throws ClassNotFoundException, SQLException {
        Class.forName(properties.getProperty("jdbc.driver"));
        Connection connection = new ConnectionSpy(DriverManager.getConnection(properties.getProperty("jdbc.url"), properties.getProperty("jdbc.username"), properties.getProperty("jdbc.password")));
        Database database = new DatabaseIO().read("src/test/resources/BcldDb.xml");
        Platform platform = new PostgreSqlPlatform();
        String sql = platform.getAlterTablesSql(connection, database);
        System.out.println(sql);
    }

    @Test
    public void testMybatisGenerator() throws IOException, XMLParserException, InvalidConfigurationException, SQLException, InterruptedException {
        List<String> warnings = new ArrayList<String>();
        boolean overwrite = true;
        File configFile = new File("src/test/resources/OperationConfig.xml");
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(configFile);
        Context context = config.getContext("Jdbc");

        JDBCConnectionConfiguration jdbcConnectionConfiguration = new JDBCConnectionConfiguration();
        jdbcConnectionConfiguration.setDriverClass(properties.getProperty("jdbc.driver"));
        jdbcConnectionConfiguration.setConnectionURL(properties.getProperty("jdbc.url"));
        jdbcConnectionConfiguration.setUserId(properties.getProperty("jdbc.username"));
        jdbcConnectionConfiguration.setPassword(properties.getProperty("jdbc.password"));
        context.setJdbcConnectionConfiguration(jdbcConnectionConfiguration);

        DefaultShellCallback callback = new DefaultShellCallback(overwrite);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);
    }

    @BeforeClass
    public static void beforeClass() throws FileNotFoundException, IOException {
        properties = new Properties();
        properties.load(new FileInputStream(new File("src/main/resources/config.properties")));
    }

}
