package io.github.hhservers.bloader.util;

import io.github.hhservers.bloader.BLoader;
import io.github.hhservers.bloader.config.Chunkloader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.ChunkTicketManager;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;

public class ChunkManager {


    private final BLoader plugin;

    private final Optional<ChunkTicketManager> ticketManager;

    public final HashMap<UUID, Optional<ChunkTicketManager.LoadingTicket>> tickets = new HashMap<>();

    public ChunkManager(BLoader plugin) {
        this.plugin = plugin;
        try {
            Class forgeChunkManager = Class.forName("net.minecraftforge.common.ForgeChunkManager");
            boolean overridesEnabled = getField(forgeChunkManager, "overridesEnabled").getBoolean(null);

            if (!overridesEnabled) {
                getField(forgeChunkManager, "overridesEnabled").set(null, true);
            }

            Map<String, Integer> ticketConstraints = (Map<String, Integer>) getField(forgeChunkManager, "ticketConstraints").get(null);
            Map<String, Integer> chunkConstraints = (Map<String, Integer>) getField(forgeChunkManager, "chunkConstraints").get(null);

            ticketConstraints.put("bloader", Integer.MAX_VALUE);
            chunkConstraints.put("bloader", Integer.MAX_VALUE);

        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | ClassNotFoundException ex) {
            plugin.getLogger().error("ChunkManager failed to force chunk constraints", ex);
        }
        ticketManager = Sponge.getServiceManager().provide(ChunkTicketManager.class);
        if (ticketManager.isPresent()) {
            ticketManager.get().registerCallback(plugin, new ChunkLoadingCallback(plugin));
        }
    }

    public boolean loadChunkLoader(Chunkloader chunkLoader) {
        Optional<WorldProperties> world = Sponge.getServer().getWorldProperties(chunkLoader.getWorld());
        if (!world.isPresent()) {
            BLoader.getInstance().getLogger().info("world not present");
            return false;
        }
        World w = Sponge.getServer().loadWorld(world.get()).get();
        Optional<Chunk> mainChunk = w.loadChunk(chunkLoader.getChunk(), false);
        if (!mainChunk.isPresent()) {
            BLoader.getInstance().getLogger().info("mainchunk not present for"+chunkLoader.getCoords()+w.getName());
            return false;
        }
        if(mainChunk.isPresent() && world.isPresent()) {
            List<Chunk> chunks = getChunks(chunkLoader.getRadius(), mainChunk.get());
            chunks.forEach((chunk) -> {
                BLoader.getInstance().getLogger().info("loaded chunk at: " + chunkLoader.getChunk().toString());
                loadChunk(chunkLoader, chunk);
            });
            return true;
        }
        return false;
    }

    public boolean unloadChunkLoader(Chunkloader chunkLoader) {
        Optional<WorldProperties> world = Sponge.getServer().getWorldProperties(chunkLoader.getWorld());
        if (!world.isPresent()) {
            BLoader.getInstance().getLogger().info("world not present");
            return false;
        }
        World w = Sponge.getServer().loadWorld(world.get()).get();
        Optional<Chunk> mainChunk = w.loadChunk(chunkLoader.getChunk(), false);
        if (!mainChunk.isPresent()) {
            BLoader.getInstance().getLogger().info("mainchunk not present for"+chunkLoader.getCoords()+w.getName());
            return false;
        }
        List<Chunk> chunks = getChunks(chunkLoader.getRadius(), mainChunk.get());
        for (Chunk chunk : chunks) {
            unloadChunk(chunkLoader, chunk);
        }
        //BLoader.getInstance().getActiveLoaderMasterList().remove(chunkLoader);
        return true;
    }

    public void addToList(Chunkloader chunkloader) throws SQLException {
        BLoader.getInstance().getCachedLoaderList().add(chunkloader);
        BLoader.getInstance().getActiveLoaderMasterList().add(chunkloader);
        BLoader.getInstance().getDataStore().addChunkLoaderData(chunkloader);
    }

    public void removeFromList(Chunkloader chunkloader) throws SQLException {
        BLoader.getInstance().getCachedLoaderList().remove(chunkloader);
        BLoader.getInstance().getActiveLoaderMasterList().remove(chunkloader);
        BLoader.getInstance().getDataStore().removeChunkLoader(chunkloader.getLoaderID());
    }

    /**
     *
     * Loads chunk using old or new ticket.
     *
     * @param chunkLoader
     * @param chunk
     * @return
     */
    private boolean loadChunk(Chunkloader chunkLoader, Chunk chunk) {
        if (!ticketManager.isPresent()) {
            BLoader.getInstance().getLogger().info("ticket manager not present");
            return false;
        }
        Optional<ChunkTicketManager.LoadingTicket> ticket;
        if (tickets.containsKey(chunkLoader.getLoaderID()) && tickets.get(chunkLoader.getLoaderID()).isPresent()) {
            ticket = tickets.get(chunkLoader.getLoaderID());
        } else {
            ticket = ticketManager.get().createTicket(plugin, chunk.getWorld());
            tickets.put(chunkLoader.getLoaderID(), ticket);
        }
        if (ticket.isPresent() && chunk != null) {
            ticket.get().forceChunk(chunk.getPosition());
            //TODO: DEBUG BLoader.getInstance().getLogger().info("force loaded chunk at "+chunk.getPosition().toString());
            return true;
        }
        return false;
    }

    /**
     * Unloads chunk using tickets.
     *
     * @param chunkLoader
     * @param chunk
     * @return
     */
    private boolean unloadChunk(Chunkloader chunkLoader, Chunk chunk) {
        if (!ticketManager.isPresent()) {
            return false;
        }
        if (tickets.containsKey(chunkLoader.getLoaderID())) {
            Optional<ChunkTicketManager.LoadingTicket> ticket = tickets.get(chunkLoader.getLoaderID());
            if (ticket.isPresent() && chunk != null) {
                ticket.get().unforceChunk(chunk.getPosition());
                chunk.unloadChunk();
                return true;
            }
        }
        return false;
    }

    public List<Chunk> getChunks(Integer radius, Chunk chunk) {
        List<Chunk> chunks = new ArrayList<>(Arrays.asList());
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Optional<Chunk> found = chunk.getWorld().getChunk(chunk.getPosition().add(x, 0, z));
                found.ifPresent(chunks::add);
            }
        }
        return chunks;
    }

    private Field getField(Class<?> targetClass, String fieldName) throws NoSuchFieldException, SecurityException {
        Field field = targetClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

}
