package io.github.hhservers.bloader.util;

import com.google.common.collect.ImmutableList;
import io.github.hhservers.bloader.BLoader;
import org.spongepowered.api.world.ChunkTicketManager;
import org.spongepowered.api.world.World;

public class ChunkLoadingCallback implements ChunkTicketManager.Callback {

    private final BLoader plugin;

    public ChunkLoadingCallback(BLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoaded(ImmutableList<ChunkTicketManager.LoadingTicket> tickets, World world) {

    }
}
