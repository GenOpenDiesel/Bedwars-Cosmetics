package xyz.iamthedefender.cosmetics.data.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.bukkit.Bukkit;
import xyz.iamthedefender.cosmetics.CosmeticsPlugin;
import xyz.iamthedefender.cosmetics.api.database.DatabaseType;
import xyz.iamthedefender.cosmetics.api.database.IDatabase;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite implements IDatabase {

    private final CosmeticsPlugin plugin;
    @Getter
    public HikariDataSource dataSource;
    public SQLite(CosmeticsPlugin plugin){
        this.plugin = plugin;
        connect();
        createTable();
    }


    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.SQLITE;
    }

    public void connect(){
        boolean needConnecting = dataSource == null;
        if (!needConnecting){
            // The health check must release its connection, otherwise every call to
            // connect() permanently burns one slot out of the pool.
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute("SELECT 1");
            } catch (SQLException e) {
                needConnecting = true;
            }
        }

        if (needConnecting){
            String jdbcUrl = "jdbc:sqlite:" + plugin.getHandler().getAddonPath() + "/cosmeticsData.db";
            try{
                Class.forName("org.sqlite.JDBC");
            }catch (ClassNotFoundException e) {
               Bukkit.getLogger().severe("SQLite class not found! SQLite is not supported!");
            }
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername("");
            config.setPassword("");
            config.setConnectionTestQuery("SELECT 1");
            // A finite timeout means an exhausted pool throws a recoverable exception
            // instead of blocking the calling thread forever (Integer.MAX_VALUE ms ~= 24
            // days), which previously froze the main thread and tripped the watchdog.
            config.setConnectionTimeout(30_000);
            config.setLeakDetectionThreshold(10_000);
            // SQLite is a single-writer file database; a large pool serves no purpose.
            config.setMaximumPoolSize(10);
            config.setPoolName("COSMETICS-SQLITE");
            dataSource = new HikariDataSource(config);
            // Probe the pool once at startup; close it again so the slot stays available.
            try (Connection ignored = dataSource.getConnection()) {
                // connection is valid
            } catch (SQLException e) {
                Bukkit.getLogger().severe("There was an error getting the connection for database! error: " + e.getMessage());
            }
        }
    }

    public void createTable(){
        if (dataSource != null){
            try (Connection connection = dataSource.getConnection()) {
                Statement statement = connection.createStatement();
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS cosmetics_player_data (" +
                        "uuid TEXT PRIMARY KEY," +
                        "bed_destroy TEXT," +
                        "wood_skin TEXT," +
                        "victory_dance TEXT," +
                        "shopkeeper_skin TEXT," +
                        "glyph TEXT," +
                        "spray TEXT," +
                        "projectile_trail TEXT," +
                        "kill_message TEXT," +
                        "final_kill_effect TEXT," +
                        "island_topper TEXT," +
                        "death_cry TEXT" +
                        ")");
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS player_owned_data (" +
                        "uuid TEXT PRIMARY KEY," +
                        "bed_destroy INT," +
                        "death_cry INT," +
                        "final_kill_effect INT," +
                        "glyph INT," +
                        "island_topper INT," +
                        "kill_message INT," +
                        "projectile_trail INT," +
                        "shopkeeper_skin INT," +
                        "spray INT," +
                        "victory_dance INT," +
                        "wood_skin INT" +
                        ")");
            } catch (SQLException e) {
                Bukkit.getLogger().severe("Failed to create player-data table: " + e.getMessage());
            }
        }
    }

    @Override
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
