package io.github.hhservers.bloader.util;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import io.github.hhservers.bloader.BLoader;
import io.github.hhservers.bloader.config.Chunkloader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class Util {

    private ChunkManager chunkManager = BLoader.getInstance().getChunkManager();

    public boolean checkWorld(UUID uuid){
        for(WorldProperties properties : Sponge.getServer().getAllWorldProperties()){
            if(properties.getUniqueId().equals(uuid)){
                return true;
            }
        }
        return false;
    }

    public Optional<WorldProperties> getWorldProperties(UUID uuid){
        for(WorldProperties properties : Sponge.getServer().getAllWorldProperties()) {
            if (properties.getUniqueId().equals(uuid)) {
                return Optional.of(Sponge.getServer().getWorldProperties(uuid).get());
            }
        }
        return Optional.empty();
    }

    public List<Chunkloader> getChunkLoaders(World w){
        List<Chunkloader> clList = BLoader.getInstance().getActiveLoaderMasterList().stream()
                .filter(chunkloader -> checkWorld(chunkloader.getWorld()))
                .collect(Collectors.toList());
        return clList;
    }

    public Optional<Chunkloader> getChunkloaderAt(Location<World> location){
        List<Chunkloader> clList = BLoader.getInstance().getActiveLoaderMasterList();
        if (clList == null || clList.isEmpty()) {
            return Optional.empty();
        }
        for (Chunkloader chunkLoader : clList) {
            if (chunkLoader.getChunk().equals(location.getChunkPosition())) {
                return Optional.of(chunkLoader);
            }
        }
        return Optional.empty();
    }

    public Optional<User> getUser(UUID uuid){
        Optional<UserStorageService> userStorageService = Sponge.getServiceManager().provide(UserStorageService.class);
        return userStorageService.get().get(uuid);
    }

    public List<Chunkloader> getActiveLoadersByPlayer(User p){
        List<Chunkloader> chunkloaders = new ArrayList<>();
        for (Chunkloader chunkloader : BLoader.getInstance().getActiveLoaderMasterList()) {
            if(chunkloader.getOwner().equals(p.getUniqueId())){
                chunkloaders.add(chunkloader);
            }
        }
        return chunkloaders;
    }

    public List<Chunkloader> getAllLoadersByPlayer(User p){
        List<Chunkloader> chunkloaders = new ArrayList<>();
        for (Chunkloader chunkloader : BLoader.getInstance().getCachedLoaderList()) {
            if(chunkloader.getOwner().equals(p.getUniqueId())){
                chunkloaders.add(chunkloader);
            }
        }
        return chunkloaders;
    }

    /*public List<Chunkloader> getAllLoadersByPlayer(Player p) {
        List<Chunkloader> chunkloaders = BLoader.getInstance().getCachedLoaderList();
        List<Chunkloader> playerLoaders = new ArrayList<>();
        if (!chunkloaders.isEmpty()) {
            for (Chunkloader chunkloader : chunkloaders) {
                if (chunkloader.getOwner().equals(p.getUniqueId())) {
                    playerLoaders.add(chunkloader);
                }
            }
        }
        return playerLoaders;
    }*/

    public List<Text> loaderList(Player p) {
        List<Text> pageList = new ArrayList<>();
        for (Chunkloader chunkloader : getActiveLoadersByPlayer(p)) {
            if (chunkloader.getOwner().equals(p.getUniqueId())) {
                if (getWorldProperties(chunkloader.getWorld()).isPresent()) {
                    WorldProperties w = getWorldProperties(chunkloader.getWorld()).get();
                    if (chunkloader.getOffline()) {
                        pageList.add(Text.builder().append(textSerializer("&d[Offline]" + "&b | " + "&a" + w.getWorldName() + chunkloader.getCoords())).build()
                        .toBuilder()
                                .append(Text.of(TextColors.AQUA, " | "))
                                .append(Text.builder().append(textSerializer("&l&c[REMOVE]&r"))
                                .onClick(TextActions.executeCallback(source -> {
                                    chunkManager.unloadChunkLoader(chunkloader);
                                    try {
                                        chunkManager.removeFromList(chunkloader);
                                        source.sendMessage(Text.of("Removed"));
                                    } catch (SQLException exception) {
                                        exception.printStackTrace();
                                    }
                                })).onHover(TextActions.showText(textSerializer("&cRemove this chunkloader"))).build())

                                .append(Text.of(TextColors.AQUA, " | "))

                                .append(Text.builder().append(textSerializer("&l&9[TP]&r"))
                                .onClick(TextActions.executeCallback(source -> {teleportPlayer(p,chunkloader.getWorld(),chunkloader.getCoords());}))
                                .onHover(TextActions.showText(textSerializer("&9Safely teleport to this chunkloader")))
                                .build())

                                .build());
                    } else {
                        pageList.add(Text.builder().append(textSerializer("&a[Online]" + "&b | " + "&a" + w.getWorldName() + chunkloader.getCoords())).build()
                                .toBuilder()
                                .append(Text.of(TextColors.AQUA, " | "))
                                .append(Text.builder().append(textSerializer("&l&c[REMOVE]&r"))
                                        .onClick(TextActions.executeCallback(source -> {
                                            chunkManager.unloadChunkLoader(chunkloader);
                                            try {
                                                chunkManager.removeFromList(chunkloader);
                                                source.sendMessage(Text.of("Removed"));
                                            } catch (SQLException exception) {
                                                exception.printStackTrace();
                                            }
                                        })).onHover(TextActions.showText(textSerializer("&cRemove this chunkloader"))).build())

                                .append(Text.of(TextColors.AQUA, " | "))

                                .append(Text.builder().append(textSerializer("&l&9[TP]&r"))
                                        .onClick(TextActions.executeCallback(source -> {teleportPlayer(p,chunkloader.getWorld(),chunkloader.getCoords());}))
                                        .onHover(TextActions.showText(textSerializer("&9Safely teleport to this chunkloader")))
                                        .build())

                                .build());
                    }
                }
            }
        }
        return pageList;
    }

    public List<Text> offlineUserLoaderList(User p, Player viewer) {
        List<Text> pageList = new ArrayList<>();
        for (Chunkloader chunkloader : getAllLoadersByPlayer(p)) {
            if (chunkloader.getOwner().equals(p.getUniqueId())) {
                if (getWorldProperties(chunkloader.getWorld()).isPresent()) {
                    WorldProperties w = getWorldProperties(chunkloader.getWorld()).get();
                    if (chunkloader.getOffline()) {
                        pageList.add(Text.builder().append(textSerializer("&d[Offline]" + "&b | " + "&a" + w.getWorldName() + chunkloader.getCoords())).build()
                                .toBuilder()
                                .append(Text.of(TextColors.AQUA, " | "))
                                .append(Text.builder().append(textSerializer("&l&c[REMOVE]&r"))
                                        .onClick(TextActions.executeCallback(source -> {
                                            chunkManager.unloadChunkLoader(chunkloader);
                                            try {
                                                chunkManager.removeFromList(chunkloader);
                                                source.sendMessage(Text.of("Removed"));
                                            } catch (SQLException exception) {
                                                exception.printStackTrace();
                                            }
                                        })).onHover(TextActions.showText(textSerializer("&cRemove this chunkloader"))).build())

                                .append(Text.of(TextColors.AQUA, " | "))

                                .append(Text.builder().append(textSerializer("&l&9[TP]&r"))
                                        .onClick(TextActions.executeCallback(source -> {teleportPlayer(viewer,chunkloader.getWorld(),chunkloader.getCoords());}))
                                        .onHover(TextActions.showText(textSerializer("&9Safely teleport to this chunkloader")))
                                        .build())

                                .build());
                    } else {
                        pageList.add(Text.builder().append(textSerializer("&a[Online]" + "&b | " + "&a" + w.getWorldName() + chunkloader.getCoords())).build()
                                .toBuilder()
                                .append(Text.of(TextColors.AQUA, " | "))
                                .append(Text.builder().append(textSerializer("&l&c[REMOVE]&r"))
                                        .onClick(TextActions.executeCallback(source -> {
                                            chunkManager.unloadChunkLoader(chunkloader);
                                            try {
                                                chunkManager.removeFromList(chunkloader);
                                                source.sendMessage(Text.of("Removed"));
                                            } catch (SQLException exception) {
                                                exception.printStackTrace();
                                            }
                                        })).onHover(TextActions.showText(textSerializer("&cRemove this chunkloader"))).build())

                                .append(Text.of(TextColors.AQUA, " | "))

                                .append(Text.builder().append(textSerializer("&l&9[TP]&r"))
                                        .onClick(TextActions.executeCallback(source -> {teleportPlayer(viewer,chunkloader.getWorld(),chunkloader.getCoords());}))
                                        .onHover(TextActions.showText(textSerializer("&9Safely teleport to this chunkloader")))
                                        .build())

                                .build());
                    }
                }
            }
        }
        return pageList;
    }

    public Text getLoaderText (Player p, UUID loaderID){
        Text text = textSerializer("");
        List<Chunkloader> chunkloaderList = BLoader.getInstance().getActiveLoaderMasterList();
        ListIterator<Chunkloader> iterator = chunkloaderList.listIterator();
        while(iterator.hasNext()){
            Chunkloader chunkloader = iterator.next();
            if(chunkloader.getLoaderID().equals(loaderID)){
                if(getWorldProperties(chunkloader.getWorld()).isPresent()){
                    WorldProperties w = getWorldProperties(chunkloader.getWorld()).get();
                    text = prefixSerializer("&bChunkloader at &d| &a"+w.getWorldName()+" "+chunkloader.getCoords()+"&d | ")
                    .toBuilder()
                            .append(
                                    Text.builder()
                                            .append(textSerializer("&9[TP]&r"))
                                            .onHover(TextActions.showText(textSerializer("&9Teleport to this loader")))
                                            .onClick(TextActions.executeCallback(source -> {
                                                p.setLocationSafely(Sponge.getServer().loadWorld(w).get().getLocation(chunkloader.getCoords()));
                                            }))
                                            .build()
                            )
                            .append(textSerializer(" &d| "))
                            .append(
                                    Text.builder()
                                    .append(textSerializer("&c[REMOVE]&r"))
                                    .onHover(TextActions.showText(textSerializer("&cRemove this loader")))
                                    .onClick(TextActions.executeCallback(source -> {
                                        if(chunkManager.unloadChunkLoader(chunkloader)){
                                            try {
                                                chunkManager.removeFromList(chunkloader);
                                            } catch (SQLException exception) {
                                                exception.printStackTrace();
                                            }
                                        }
                                    }))
                                    .build()
                            )
                            .build();
                }
            }
        }
        return text;
    }

    private void teleportPlayer(Player p, UUID uuid, Vector3i pos){
        if(checkWorld(uuid)){
            if(getWorldProperties(uuid).isPresent()){
                World w = Sponge.getServer().loadWorld(uuid).get();
                p.setLocationSafely(w.getLocation(pos));
            }
        }
    }

    public void manualTickLoaders(){
        BLoader.getInstance().getLogger().info("Ticking loaders");
        List<Chunkloader> chunkloaderList = BLoader.getInstance().getActiveLoaderMasterList();
        ListIterator<Chunkloader> iterator = chunkloaderList.listIterator();
        while (iterator.hasNext()) {
            Chunkloader chunkloader = iterator.next();
            if (chunkloader != null) {
                ChunkManager chunkManager = BLoader.getInstance().getChunkManager();
                CreditManager creditManager = new CreditManager();
                //Player p = Sponge.getServer().getPlayer(chunkloader.getOwner()).get();
                User p = getUser(chunkloader.getOwner()).get();
                if (getWorldProperties(chunkloader.getWorld()).isPresent()) {
                    WorldProperties world = getWorldProperties(chunkloader.getWorld()).get();
                    if (chunkloader.getOffline()) {
                        if (!creditManager.removeCredits(p, 10)) {
                            if (chunkManager.unloadChunkLoader(chunkloader)) {
                                try {
                                    iterator.remove();
                                    chunkManager.removeFromList(chunkloader);
                                } catch (SQLException exception) {
                                    exception.printStackTrace();
                                }
                                if (p.isOnline()) {
                                    p.getPlayer().get().sendMessage(prefixSerializer("&bYour chunkloader at&d | &a" + world.getWorldName() + " " + chunkloader.getCoords() + "&d | &bhas expired."));
                                }
                            }
                        }
                    } else {
                        if (!creditManager.removeCredits(p, 5)) {
                            if (chunkManager.unloadChunkLoader(chunkloader)) {
                                try {
                                    iterator.remove();
                                    chunkManager.removeFromList(chunkloader);
                                } catch (SQLException exception) {
                                    exception.printStackTrace();
                                }
                                if (p.isOnline()) {
                                    p.getPlayer().get().sendMessage(prefixSerializer("&bYour chunkloader at&d | &a" + world.getWorldName() + " " + chunkloader.getCoords() + "&d | &bhas expired."));
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
    }

    public Text textSerializer(String s){
        return TextSerializers.FORMATTING_CODE.deserialize(s);
    }

    public Text prefixSerializer(String s){return TextSerializers.FORMATTING_CODE.deserialize("&l&8[&r&aB&dLoader&l&8]&r "+s);}


    public Inventory inventoryBuilder(List<ItemStack> items, Inventory inv, Player p){
        ItemStack border = ItemStack.of(ItemTypes.STAINED_GLASS_PANE, 1);
        border.offer(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize("&l&8[&r&aB&dUtils&r&l&8]"));
        border.offer(Keys.DYE_COLOR, DyeColors.BLACK);

        ItemStack back = ItemStack.of(ItemTypes.ARROW, 1);
        back.offer(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize("&l&8[&r&aBack&l&8]&r"));

        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(0, 1))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(8, 1))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(0, 2))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(1, 2))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(2, 2))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(3, 2))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(4, 2))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(5, 2))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(6, 2))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(7, 2))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(8, 2))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(9, 2))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(0, 0))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(1, 0))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(2, 0))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(3, 0))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(4, 0))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(5, 0))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(6, 0))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(7, 0))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(8, 0))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(9, 0))).set(border);

        for(ItemStack item : items){
            inv.offer(item);
        }
        return inv;
    }

    public Inventory twoRowInventoryBuilder(List<ItemStack> items, Inventory inv, Player p){
        ItemStack border = ItemStack.of(ItemTypes.STAINED_GLASS_PANE, 1);
        border.offer(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize("&l&8[&r&aB&dUtils&r&l&8]"));
        border.offer(Keys.DYE_COLOR, DyeColors.BLACK);

        ItemStack back = ItemStack.of(ItemTypes.ARROW, 1);
        back.offer(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize("&l&8[&r&aBack&l&8]&r"));

        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(0))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(1))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(2))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(3))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(4))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(5))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(6))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(7))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(8))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(9))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(17))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(18))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(26))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(27))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(28))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(29))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(30))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(31))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(32))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(33))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(34))).set(border);
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(35))).set(border);

        for(ItemStack item : items){
            inv.offer(item);
        }
        return inv;
    }

}
