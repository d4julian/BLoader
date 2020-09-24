package io.github.hhservers.bloader.util;

import io.github.hhservers.bloader.BLoader;
import io.github.hhservers.bloader.config.Chunkloader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class PlayerListener {

    private final BLoader plugin;

    public PlayerListener(BLoader plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Sponge.getEventManager().registerListeners(plugin, this);
    }

    @Listener
    public void onPlayerLogin(ClientConnectionEvent.Join event) {

    }

    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect event, @Root Player player) {

    }

    @Listener
    public void onPlayerRightClick(InteractBlockEvent.Secondary.MainHand e, @Root Player p) {
        if (!e.getCause().containsType(Player.class)) {
            return;
        }
        BlockSnapshot clickedBlock = e.getTargetBlock();
        if (clickedBlock == null || p == null) {
            return;
        }
        if (!clickedBlock.getState().getType().equals(BlockTypes.DIAMOND_BLOCK) && !clickedBlock.getState().getType().equals(BlockTypes.IRON_BLOCK)) {
            return;
        }
        Chunkloader chunkloader = new Chunkloader();
        if (p.getItemInHand(HandTypes.MAIN_HAND).isPresent() && p.getItemInHand(HandTypes.MAIN_HAND).get().getType().equals(ItemTypes.BLAZE_ROD)) {
            if (p.hasPermission("bloader.admin")) {
                e.setCancelled(true);
                if (clickedBlock.getState().getType().equals(BlockTypes.DIAMOND_BLOCK)) {
                    chunkloader.setOffline(true);
                    chunkloader.setCoords(clickedBlock.getPosition());
                    chunkloader.setChunk(clickedBlock.getLocation().get().getChunkPosition());
                    chunkloader.setWorld(p.getLocation().getExtent().getUniqueId());
                    new CreateLoaderGUI().openGUI(p, chunkloader);
                }
                if (clickedBlock.getState().getType().equals(BlockTypes.IRON_BLOCK)) {
                    chunkloader.setOffline(false);
                    chunkloader.setCoords(clickedBlock.getPosition());
                    chunkloader.setChunk(clickedBlock.getLocation().get().getChunkPosition());
                    chunkloader.setWorld(p.getLocation().getExtent().getUniqueId());
                    new CreateLoaderGUI().openGUI(p, chunkloader);
                }
            }else{p.sendMessage(Text.of(TextColors.RED, "You do not have permission to create a BLoader"));}
        }
    }

}
