package io.github.hhservers.bloader.util;

import io.github.hhservers.bloader.BLoader;
import io.github.hhservers.bloader.config.Chunkloader;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.storage.WorldProperties;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CreditManager {

    private Util util = new Util();
    //private ChunkManager chunkManager = BLoader.getInstance().getChunkManager();

    private Set<Context> serverContext() {
        return Collections.singleton(new Context("server", BLoader.getMainPluginConfig().getServerName()));
    }

    public Integer getCredits(User p) {
        if (p.getSubjectData().getOptions(serverContext()).containsKey("bloader_credits")) {
            String bal = p.getSubjectData().getOptions(serverContext()).get("bloader_credits");

            return Integer.valueOf(bal);
        } else {
            p.getSubjectData().setOption(serverContext(), "bloader_credits", "0");
            return 0;
        }
    }

    public boolean enoughCredits(Player p, Integer credits){
        if(getCredits(p) >= credits){
            return true;
        }
        return false;
    }

    public void tickLoaders() {
        Task.builder()
                .delay(15, TimeUnit.MINUTES)
                .interval(1, TimeUnit.HOURS)
                .execute(() -> {
                    BLoader.getInstance().getLogger().info("Ticking loaders");
                    List<Chunkloader> chunkloaderList = BLoader.getInstance().getActiveLoaderMasterList();
                    ListIterator<Chunkloader> iterator = chunkloaderList.listIterator();
                    while (iterator.hasNext()) {
                        Chunkloader chunkloader = iterator.next();
                        if (chunkloader != null) {
                            ChunkManager chunkManager = BLoader.getInstance().getChunkManager();
                            //Player p = Sponge.getServer().getPlayer(chunkloader.getOwner()).get();
                            User p = util.getUser(chunkloader.getOwner()).get();
                            if (util.getWorldProperties(chunkloader.getWorld()).isPresent()) {
                                WorldProperties world = util.getWorldProperties(chunkloader.getWorld()).get();
                                if (chunkloader.getOffline()) {
                                    if (!removeCredits(p, 10)) {
                                        if (chunkManager.unloadChunkLoader(chunkloader)) {
                                            try {
                                                iterator.remove();
                                                chunkManager.removeFromList(chunkloader);
                                            } catch (SQLException exception) {
                                                exception.printStackTrace();
                                            }
                                            if (p.isOnline()) {
                                                p.getPlayer().get().sendMessage(util.prefixSerializer("&bYour chunkloader at&d | &a" + world.getWorldName() + " " + chunkloader.getCoords() + "&d | &bhas expired."));
                                            }
                                        }
                                    }
                                } else {
                                    if (!removeCredits(p, 5)) {
                                        if (chunkManager.unloadChunkLoader(chunkloader)) {
                                            try {
                                                iterator.remove();
                                                chunkManager.removeFromList(chunkloader);
                                            } catch (SQLException exception) {
                                                exception.printStackTrace();
                                            }
                                            if (p.isOnline()) {
                                                p.getPlayer().get().sendMessage(util.prefixSerializer("&bYour chunkloader at&d | &a" + world.getWorldName() + " " + chunkloader.getCoords() + "&d | &bhas expired."));
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (chunkManager.unloadChunkLoader(chunkloader)) {
                                    try {
                                        chunkManager.removeFromList(chunkloader);
                                    } catch (SQLException exception) {
                                        exception.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }).name("BLoaderTickLoaders").submit(BLoader.getInstance());
    }


    public boolean removeCredits(User p, Integer amount) {
        int bal = getCredits(p);
        if (bal >= amount) {
            Integer newBal = bal - amount;
            p.getSubjectData().setOption(serverContext(), "bloader_credits", newBal.toString());
            if (p.isOnline()) {
                p.getPlayer().get().sendMessage(util.prefixSerializer("&c-&6" + amount + " &aB&dLoader&bCredits &b~&cServer").toBuilder()
                        .onHover(TextActions.showText(TextSerializers.FORMATTING_CODE.deserialize("&b/bloader"))).build());
            }
            return true;
        } else {
            if (p.isOnline()) {
                p.getPlayer().get().sendMessage(util.prefixSerializer("&bYou do not have enough &aB&dLoader&b credits."));
            }
            return false;
        }
    }

    public void addCredits(User p, Integer amount) {
        int bal = getCredits(p);
        Integer newBal = bal + amount;
        p.getSubjectData().setOption(serverContext(), "bloader_credits", newBal.toString());
        if(p.isOnline()) {
            p.getPlayer().get().sendMessage(util.prefixSerializer("&a+&6" + amount + " &aB&dLoader&bCredits &b~&cServer").toBuilder()
                    .onHover(TextActions.showText(TextSerializers.FORMATTING_CODE.deserialize("&b/bloader"))).build());
        }
    }

    public Boolean payCredits(Player sender, Player receiver, Integer amount) {
        Integer senderBal = getCredits(sender);
        Integer receiverBal = getCredits(receiver);
        if (!(senderBal < amount)) {
            Integer newSendBal = senderBal - amount;
            Integer newRecBal = receiverBal + amount;
            receiver.getSubjectData().setOption(serverContext(), "bloader_credits", newRecBal.toString());
            sender.getSubjectData().setOption(serverContext(), "bloader_credits", newSendBal.toString());
            receiver.sendMessage(util.prefixSerializer("+&6" + amount + "&aB&dLoader&r &b~&a" + sender.getName()).toBuilder()
                    .onHover(TextActions.showText(TextSerializers.FORMATTING_CODE.deserialize("&b/bloader"))).build());
            sender.sendMessage(util.prefixSerializer("&c -&6" + amount + "&aB&dLoader &b~&a" + receiver.getName()).toBuilder()
                    .onHover(TextActions.showText(TextSerializers.FORMATTING_CODE.deserialize("&b/bloader"))).build());
        } else {
            sender.sendMessage(Text.of(TextColors.AQUA, "You can't afford this!"));
        }
        return true;
    }

}
