package io.github.hhservers.bloader.config;

import com.flowpowered.math.vector.Vector3i;
import lombok.Data;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;
import java.util.UUID;

@ConfigSerializable
@Data
public class Chunkloader {

    @Setting(value = "owner")
    private UUID owner;
    @Setting(value="loaderID")
    private UUID loaderID;
    @Setting(value="coords")
    private Vector3i coords;
    @Setting(value="chunk")
    private Vector3i chunk;
    @Setting(value="radius")
    private Integer radius;
    @Setting(value="worldUUID")
    private UUID world;
    @Setting(value="offline")
    private Boolean offline;
    @Setting(value="serverName")
    private String serverName;
    @Setting(value="creationDate")
    private Long creationDate;

    public boolean isLoadable() {
        Optional<Player> player = Sponge.getServer().getPlayer(owner);
        return (player.isPresent() && player.get().isOnline() || (this.offline));
    }

}
