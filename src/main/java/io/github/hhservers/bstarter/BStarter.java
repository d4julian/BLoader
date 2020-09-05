package io.github.hhservers.bstarter;

import com.google.inject.Inject;
import io.github.hhservers.bstarter.commands.Base;
import io.github.hhservers.bstarter.config.ConfigHandler;
import io.github.hhservers.bstarter.config.MainPluginConfig;
import lombok.Getter;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;

@Plugin(
        id = "bstarter",
        name = "BStarter",
        description = "Starter plugin",
        authors = {
                "blvxr"
        }
)
public class BStarter {

    @Getter
    private static BStarter instance;
    @Getter
    @Inject
    private Logger logger;
    @Getter
    private static MainPluginConfig mainPluginConfig;
    private final GuiceObjectMapperFactory factory;
    private final File configDir;
    private static ConfigHandler configHandler;


    @Inject
    public BStarter(GuiceObjectMapperFactory factory, @ConfigDir(sharedRoot = false) File configDir) {
        this.factory=factory;
        this.configDir=configDir;
        instance=this;
    }

    @Listener
    public void onGamePreInit(GamePreInitializationEvent e) throws IOException, ObjectMappingException {
        reloadConfig();
    }

    @Listener
    public void onGameInit(GameInitializationEvent e){
        instance = this;
        Sponge.getCommandManager().register(instance, Base.build(), "base");
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
    }

    @Listener
    public void onGameReload(GameReloadEvent e) throws IOException, ObjectMappingException {
        reloadConfig();
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
}
