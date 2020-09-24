package io.github.hhservers.bloader.config;

import com.google.common.reflect.TypeToken;
import io.github.hhservers.bloader.BLoader;
import lombok.Data;
import lombok.Getter;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.File;
import java.io.IOException;

@Data
public class ConfigHandler {

    private BLoader plugin = BLoader.getInstance();
    private File confFile =new File(plugin.getConfigDir(), "BLoader.conf");
    private ConfigurationLoader<CommentedConfigurationNode> configLoad;
    @Getter
    private MainPluginConfig pluginConf;

    public ConfigHandler(BLoader plugin) {
        this.plugin = plugin;
        if (!plugin.getConfigDir().exists()) {
            plugin.getConfigDir().mkdirs();
        }
    }

    public boolean loadConfig() throws IOException, ObjectMappingException {
        //File file = new File(plugin.getConfigDir(), "BStarter.conf");
        if (!confFile.exists()) {
            confFile.createNewFile();
        }
        configLoad = HoconConfigurationLoader.builder().setFile(confFile).build();
        CommentedConfigurationNode config = configLoad.load(ConfigurationOptions.defaults().setObjectMapperFactory(plugin.getFactory()).setShouldCopyDefaults(true));
        pluginConf = config.getValue(TypeToken.of(MainPluginConfig.class), new MainPluginConfig());
        configLoad.save(config);
        return true;
    }

    public void saveConfig(MainPluginConfig newConfig) throws IOException, ObjectMappingException {
            if (!confFile.exists()) {
                confFile.createNewFile();
            }
            CommentedConfigurationNode config = configLoad.load(ConfigurationOptions.defaults().setObjectMapperFactory(plugin.getFactory()).setShouldCopyDefaults(true));
            config.setValue(TypeToken.of(MainPluginConfig.class), newConfig);
            configLoad.save(config);
    }
}