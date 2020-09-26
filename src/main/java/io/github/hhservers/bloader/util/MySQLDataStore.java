package io.github.hhservers.bloader.util;

import com.flowpowered.math.vector.Vector3i;
import io.github.hhservers.bloader.BLoader;
import io.github.hhservers.bloader.config.Chunkloader;
import io.github.hhservers.bloader.config.MainPluginConfig;

import java.sql.*;
import java.util.*;

public class MySQLDataStore {

    private final BLoader plugin = BLoader.getInstance();

    // I don't get my connection like this so double check to make sure this works.
    public Connection getConnection() throws SQLException {

        final Properties connectionProps = new Properties();
        MainPluginConfig config = BLoader.getMainPluginConfig();
        String user = config.getUser();
        String password = config.getPassword();
        String hostname = config.getHostname();
        String database = config.getDbName();

        connectionProps.put("user", user);
        connectionProps.put("password", password);

        try {
            return DriverManager.getConnection("jdbc:mysql://"+hostname+"/"+database, user, password);
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    public boolean removeChunkLoader(UUID uuid) throws SQLException {
        boolean success;
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("DELETE FROM bloader_loaders WHERE uuid = ? LIMIT 1")) {
            ps.setString(1, uuid.toString());
            success = ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            plugin.getLogger().error("MySQL: Error removing ChunkLoader.", ex);
            return false;
        }
        return success;
    }

    public boolean updateCreditBal(UUID uuid, Integer bal) throws SQLException {
        boolean success;
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("UPDATE bloader_loaders SET credits = '"+bal+"' WHERE uuid = '" + uuid + "'")) {
            ps.setInt(1, bal);
            ps.setString(2, uuid.toString());
            success = ps.executeUpdate() > 0;
        } catch (SQLException e){
            plugin.getLogger().error("MySQL: Could not update credit baalance.", e);
            return false;
        }
        return success;
    }

    public boolean addChunkLoaderData(Chunkloader chunkloader) throws SQLException {
        boolean success;
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("INSERT INTO bloader_loaders VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, chunkloader.getLoaderID().toString());
            ps.setString(2, chunkloader.getOwner().toString());
            ps.setString(3, chunkloader.getWorld().toString());
            ps.setObject(4, chunkloader.getCoords().toString());
            ps.setObject(5, chunkloader.getChunk().toString());
            ps.setInt(6, chunkloader.getRadius());
            ps.setLong(7, chunkloader.getCreationDate());
            ps.setBoolean(8, chunkloader.getOffline());
            ps.setString(9, chunkloader.getServerName());
            success = ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            plugin.getLogger().error("MySQL: Error adding playerdata", ex);
            return false;
        }
        return success;
    }

    public boolean load() throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS bloader_loaders ("
                    + "uuid VARCHAR(36) NOT NULL PRIMARY KEY, "
                    + "owner VARCHAR(36) NOT NULL, "
                    + "world VARCHAR(36) NOT NULL, "
                    + "location VARCHAR(1000) NOT NULL, "
                    + "chunk VARCHAR(1000) NOT NULL, "
                    + "radius TINYINT(3) UNSIGNED NOT NULL, "
                    + "creation BIGINT(20) NOT NULL, "
                    + "offline BOOLEAN NOT NULL, "
                    + "server VARCHAR(36) NOT NULL)");
        } catch (SQLException exception) {
            plugin.getLogger().error("Cannot connect to database or unable to create table", exception);
            return false;
        }
        return true;
    }

    public List<Chunkloader> getChunkLoaderData() throws SQLException {
        List<Chunkloader> cList = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM bloader_loaders WHERE server = ?")) {
            ps.setString(1, BLoader.getMainPluginConfig().getServerName());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Chunkloader chunkLoader = new Chunkloader();
                    String[] coords = rs.getString("location").replace("(", "").replace(")", "").replace(" ", "").split(",");
                    String[] chunk = rs.getString("chunk").replace("(", "").replace(")", "").replace(" ", "").split(",");
                    chunkLoader.setLoaderID(UUID.fromString(rs.getString("uuid")));
                    chunkLoader.setWorld(UUID.fromString(rs.getString("world")));
                    chunkLoader.setOwner(UUID.fromString(rs.getString("owner")));
                    chunkLoader.setCoords(Vector3i.from(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2])));
                    chunkLoader.setChunk(Vector3i.from(Integer.parseInt(chunk[0]), 0, Integer.parseInt(chunk[2])));
                    chunkLoader.setRadius(Integer.parseInt(rs.getString("radius")));
                    chunkLoader.setCreationDate(rs.getLong("creation"));
                    chunkLoader.setServerName(rs.getString("server"));
                    chunkLoader.setOffline(rs.getBoolean("offline"));
                    cList.add(chunkLoader);
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().info("MySQL: Couldn't read chunkloaders from MySQL database.", ex);
        }
        return cList;
    }

}
