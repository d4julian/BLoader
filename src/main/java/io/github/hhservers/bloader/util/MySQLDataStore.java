package io.github.hhservers.bloader.util;

import com.flowpowered.math.vector.Vector3i;
import io.github.hhservers.bloader.BLoader;
import io.github.hhservers.bloader.config.Chunkloader;
import io.github.hhservers.bloader.config.MainPluginConfig;

import java.sql.*;
import java.util.*;

public class MySQLDataStore {

    private final BLoader plugin = BLoader.getInstance();

    private Connection dbConnection;

    public void refreshConnection() throws SQLException {
        try {
            if (this.dbConnection != null && !this.dbConnection.isClosed()){
                this.dbConnection.close();
            }
        }catch(SQLException ignored){

        }

        final Properties connectionProps = new Properties();
        MainPluginConfig config = BLoader.getMainPluginConfig();
        String user = config.getUser();
        String password = config.getPassword();
        String hostname = config.getHostname();
        String database = config.getDbName();

        connectionProps.put("user", user);
        connectionProps.put("password", password);

        try {
            this.dbConnection = DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+database, user, password);
        } catch (SQLException exception) {
            new RuntimeException(exception);
        }
    }

    private Statement statement() throws SQLException {
        try {
            this.refreshConnection();
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return this.dbConnection.createStatement();
    }

    public boolean removeChunkLoader(UUID uuid) throws SQLException {
        refreshConnection();
        try (Connection connection = dbConnection) {
            return connection.createStatement().executeUpdate("DELETE FROM bloader_loaders WHERE uuid = '" + uuid + "' LIMIT 1") > 0;
        } catch (SQLException ex) {
            plugin.getLogger().error("MySQL: Error removing ChunkLoader.", ex);
        }
        return false;
    }

    public boolean updateCreditBal(UUID uuid, Integer bal) throws SQLException {
        refreshConnection();
        try (Connection connection = dbConnection) {
            PreparedStatement statement = connection.prepareStatement("UPDATE bloader_loaders SET credits = '"+bal+"' WHERE uuid = '" + uuid + "'");
            return statement.executeUpdate() > 0;
        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean addChunkLoaderData(Chunkloader chunkloader) throws SQLException {
        refreshConnection();
        try (Connection connection = dbConnection) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO bloader_loaders VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
            statement.setString(1, chunkloader.getLoaderID().toString());
            statement.setString(2, chunkloader.getOwner().toString());
            statement.setString(3, chunkloader.getWorld().toString());
            statement.setObject(4, chunkloader.getCoords().toString());
            statement.setObject(5, chunkloader.getChunk().toString());
            statement.setInt(6, chunkloader.getRadius());
            statement.setLong(7, chunkloader.getCreationDate());
            statement.setBoolean(8, chunkloader.getOffline());
            statement.setString(9, chunkloader.getServerName());
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            plugin.getLogger().error("MySQL: Error adding playerdata", ex);
        }
        return false;
    }

    public boolean load() throws SQLException {
        try {
            this.refreshConnection();
        }catch (SQLException e) {
            BLoader.getInstance().getLogger().info("Cannot connect to databse");
        }
        //create table if not exists
        try {
            this.statement().executeUpdate("CREATE TABLE IF NOT EXISTS bloader_loaders ("
                    + "uuid VARCHAR(36) NOT NULL PRIMARY KEY, "
                    + "owner VARCHAR(36) NOT NULL, "
                    + "world VARCHAR(36) NOT NULL, "
                    + "location VARCHAR(1000) NOT NULL, "
                    + "chunk VARCHAR(1000) NOT NULL, "
                    + "radius TINYINT(3) UNSIGNED NOT NULL, "
                    + "creation BIGINT(20) NOT NULL, "
                    + "offline BOOLEAN NOT NULL, "
                    + "server VARCHAR(36) NOT NULL)");
        }catch (SQLException e){
            plugin.getLogger().error("unable to create table", e);
            return false;
        }

        return true;
    }

    public List<Chunkloader> getChunkLoaderData() throws SQLException {
        List<Chunkloader> cList = new ArrayList<>();
        refreshConnection();
        try (Connection connection = dbConnection) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM bloader_loaders WHERE server = '"+BLoader.getMainPluginConfig().getServeName()+"'");
            while (rs.next()) {
                    Chunkloader chunkLoader = new Chunkloader();
                    String[] coords = rs.getString("location").replace("(", "").replace(")","").replace(" ", "").split(",");
                    String[] chunk = rs.getString("chunk").replace("(", "").replace(")","").replace(" ","").split(",");
                    chunkLoader.setLoaderID(UUID.fromString(rs.getString("uuid")));
                    chunkLoader.setWorld(UUID.fromString(rs.getString("world")));
                    chunkLoader.setOwner(UUID.fromString(rs.getString("owner")));
                    chunkLoader.setCoords(Vector3i.from(Integer.parseInt(coords[0]),Integer.parseInt(coords[1]),Integer.parseInt(coords[2])));
                    chunkLoader.setChunk(Vector3i.from(Integer.parseInt(chunk[0]), 0, Integer.parseInt(chunk[2])));
                    chunkLoader.setRadius(Integer.parseInt(rs.getString("radius")));
                    chunkLoader.setCreationDate(rs.getLong("creation"));
                    chunkLoader.setServerName(rs.getString("server"));
                    chunkLoader.setOffline(rs.getBoolean("offline"));
                    cList.add(chunkLoader);
            }
            return cList;
        } catch (SQLException ex) {
            plugin.getLogger().info("MySQL: Couldn't read chunkloaders from MySQL database.", ex);
            return new ArrayList<>();
        }
    }

}
