package io.github.hhservers.bloader.config;

import lombok.Data;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable @Data
public class MainPluginConfig {

    @Setting(value = "user")
    private String user = "user";

    @Setting(value = "password")
    private String password = "password";

    @Setting(value = "hostname")
    private String hostname = "hostname";

    @Setting(value = "dbName")
    private String dbName = "dbName";

    @Setting(value = "serverName")
    private String serverName = "serverName";

}
