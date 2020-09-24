package io.github.hhservers.bloader;

import com.google.inject.Inject;
import io.github.hhservers.bloader.commands.Base;
import io.github.hhservers.bloader.config.Chunkloader;
import io.github.hhservers.bloader.config.ConfigHandler;
import io.github.hhservers.bloader.config.MainPluginConfig;
import io.github.hhservers.bloader.util.*;
import lombok.Getter;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Plugin(
        id = "bloader",
        name = "BLoader",
        description = "B's Chunkloader Plugin",
        authors = {
                "blvxr"
        }
)
public class BLoader {

    //TODO: limit on how many with meta?

    @Getter
    private static BLoader instance;
    @Getter
    @Inject
    private Logger logger;
    @Getter
    private static MainPluginConfig mainPluginConfig;
    private final GuiceObjectMapperFactory factory;
    private final File configDir;
    private static ConfigHandler configHandler;
    private ChunkManager chunkManager;
    @Getter
    private List<Chunkloader> activeLoaderMasterList = new ArrayList<>();
    @Getter
    private List<Chunkloader> cachedLoaderList;
    @Getter
    private MySQLDataStore dataStore = new MySQLDataStore();

    @Inject
    public BLoader(GuiceObjectMapperFactory factory, @ConfigDir(sharedRoot = false) File configDir) {
        this.factory=factory;
        this.configDir=configDir;
        instance=this;
    }

    @Listener
    public void onGamePreInit(GamePreInitializationEvent e) throws IOException, ObjectMappingException {

    }

    @Listener
    public void onGameInit(GameInitializationEvent e) throws IOException, ObjectMappingException {
        instance = this;
        reloadConfig();
        if(configChecker()) {
            Sponge.getCommandManager().register(instance, Base.build(), "bloader");
        } else { logger.info("YOU MUST EDIT THE BLoader CONFIG WITH YOUR DATABASE CREDENTIALS!"); }
    }

    @Listener
    public void onServerAboutStart(GameAboutToStartServerEvent e) throws IOException, ObjectMappingException {
        reloadConfig();
        if(configChecker()) {
            new PlayerListener(this).register();
        } else { logger.info("YOU MUST EDIT THE BLoader CONFIG WITH YOUR DATABASE CREDENTIALS!"); }
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) throws SQLException {
        chunkManager = new ChunkManager(this);
        if(dataStore.load()) {
            new CreditManager().tickLoaders();
            this.cachedLoaderList = dataStore.getChunkLoaderData();
            int count = 0;
            for (Chunkloader chunkloader : cachedLoaderList) {
                if (chunkloader.getOffline()) {
                    count++;
                    if(getChunkManager().loadChunkLoader(chunkloader)) {
                        activeLoaderMasterList.add(chunkloader);
                        logger.info("loaded");
                    } else {logger.info("error loading");}
                }
            }
            logger.info("Loaded: "+count);
            logger.info("MasterList:"+activeLoaderMasterList.size());
            logger.info("Cached list:"+getCachedLoaderList().size());
        } else {logger.info("YOU MUST EDIT THE BLoader CONFIG WITH YOUR DATABASE CREDENTIALS!");}
    }

    @Listener
    public void onPlayerLogin(ClientConnectionEvent.Join e){
        Util util = new Util();
        Player p = e.getTargetEntity();
        if(!util.getAllLoadersByPlayer(p).isEmpty()) {
            int count = 0;
            for (Chunkloader chunkloader : util.getAllLoadersByPlayer(p)) {
                if(!chunkloader.getOffline()) {
                    count++;
                    if(getChunkManager().loadChunkLoader(chunkloader)) {
                        //logger.info("loadchunkloader worked");
                        activeLoaderMasterList.add(chunkloader);
                    }
                }
            }
            logger.info("Loaded " + p.getName() + "'s chunkloaders: " + count);
        }
    }

    @Listener
    public void onPlayerLogout(ClientConnectionEvent.Disconnect e){
        Player p = e.getTargetEntity();
        Util util = new Util();
        if(!util.getAllLoadersByPlayer(p).isEmpty()) {
            int count = 0;
            for (Chunkloader chunkloader : util.getAllLoadersByPlayer(p)) {
                if(!chunkloader.getOffline()) {
                    count++;
                    if(getChunkManager().unloadChunkLoader(chunkloader)) {
                        activeLoaderMasterList.remove(chunkloader);
                    }
                }
            }
            logger.info("Unloaded " + p.getName() + "'s chunkloaders: " + count);
        }
    }

    @Listener
    public void onGameReload(GameReloadEvent e) throws IOException, ObjectMappingException {

    }

    public void reloadConfig() throws IOException, ObjectMappingException {
        configHandler=new ConfigHandler(this);
        if (configHandler.loadConfig()) {mainPluginConfig = configHandler.getPluginConf();}
    }

    public GuiceObjectMapperFactory getFactory() {
        return factory;
    }

    public File getConfigDir() {
        return configDir;
    }

    public ChunkManager getChunkManager() {
        return chunkManager;
    }

    public boolean configChecker(){
        if(BLoader.getMainPluginConfig().getServeName().equalsIgnoreCase("serverName")){
            return false;
        }
        if(BLoader.getMainPluginConfig().getHostname().equalsIgnoreCase("hostname")){
            return false;
        }
        if(BLoader.getMainPluginConfig().getDbName().equalsIgnoreCase("dbName")){
            return false;
        }
        return true;
    }

}
