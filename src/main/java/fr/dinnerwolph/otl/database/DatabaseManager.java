package fr.dinnerwolph.otl.database;

import fr.dinnerwolph.otl.bungee.BungeeOTL;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Dinnerwolph
 */

public class DatabaseManager {

    private DataSource dataSource;
    private final String url;
    private final String name;
    private final String password;
    private final int minPoolSize;
    private final int maxPoolSize;
    private final BungeeOTL plugin;

    public DatabaseManager(String url, String name, String password, int minPoolSize, int maxPoolSize, BungeeOTL plugin) {
        this.url = url;
        this.name = name;
        this.password = password;
        this.minPoolSize = minPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.plugin = plugin;
        this.setupDataSource();
    }

    public void setupDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl(this.url);
        dataSource.setUsername(this.name);
        dataSource.setPassword(this.password);
        dataSource.setInitialSize(this.minPoolSize);
        dataSource.setMaxTotal(this.maxPoolSize);
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void getSourcesStats(DataSource dataSource)
    {
        BasicDataSource basicDataSource = (BasicDataSource) dataSource;
        System.out.println("Number of active: " + basicDataSource.getNumActive());
        System.out.println("Number of idle: " + basicDataSource.getNumIdle());
        System.out.println("================================================================================");
    }

    public void shutdownDataSource(DataSource dataSource) throws Exception
    {
        BasicDataSource basicDataSource = (BasicDataSource) dataSource;
        basicDataSource.close();
    }

    public void sendLog(String log) {
        try {
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO `logs` (`network`, `time`, `log`) VALUES (?, NOW(), ?);");
            statement.setString(1, plugin.network);
            statement.setString(2, log);
            statement.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
