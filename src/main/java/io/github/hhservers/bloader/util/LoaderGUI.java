package io.github.hhservers.bloader.util;

import io.github.hhservers.bloader.BLoader;
import io.github.hhservers.bloader.config.Chunkloader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class LoaderGUI {

    private Util util = new Util();
    private CreditManager creditManager = new CreditManager();

    private Inventory loaderGUI = Inventory.builder().of(InventoryArchetypes.CHEST)
            .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9, 4))
            .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(TextSerializers.FORMATTING_CODE.deserialize("&l&8[&r&aB&dLoader&l&8]&r")))
            .listener(ClickInventoryEvent.Primary.class, clickInventoryEvent -> loaderGUIListener(clickInventoryEvent))
            .listener(ClickInventoryEvent.class, clickInventoryEvent -> clickInventoryEvent.setCancelled(true))
            .build(BLoader.getInstance());


    private void loaderGUIListener(ClickInventoryEvent e) {
        e.setCancelled(true);
        Player p = e.getCause().first(Player.class).get();
        if (!e.getTransactions().isEmpty()) {
            Transaction<ItemStackSnapshot> clickTransaction = e.getTransactions().get(0);
            SlotTransaction slotTransaction = e.getTransactions().get(0);
            Slot slotClicked = slotTransaction.getSlot();
            SlotIndex index = slotClicked.getInventoryProperty(SlotIndex.class).get();
            ItemStack item = clickTransaction.getOriginal().createStack();
            if(item.get(Keys.ITEM_LORE).isPresent()){
                String loaderID = TextSerializers.PLAIN.serialize(item.get(Keys.ITEM_LORE).get().get(5));
                if(item.getType().equals(ItemTypes.IRON_BLOCK)){
                    p.closeInventory();
                    p.sendMessage(util.getLoaderText(p, UUID.fromString(loaderID)));
                }
                if(item.getType().equals(ItemTypes.DIAMOND_BLOCK)){
                    p.closeInventory();
                    p.sendMessage(util.getLoaderText(p, UUID.fromString(loaderID)));
                }
            }
        }
    }

    private List<ItemStack> genItems(Player p) {
        List<Chunkloader> loaderList = BLoader.getInstance().getActiveLoaderMasterList();
        List<ItemStack> itemList = new ArrayList<>();
        for (Chunkloader chunkloader : loaderList) {
            if (chunkloader.getOwner().equals(p.getUniqueId())) {
                if (util.checkWorld(chunkloader.getWorld())) {
                    World w = Sponge.getServer().loadWorld(chunkloader.getWorld()).get();
                    Location<World> loc = w.getLocation(chunkloader.getCoords());
                    if (chunkloader.getOffline()) {
                        itemList.add(ItemStack.builder()
                                .itemType(ItemTypes.DIAMOND_BLOCK)
                                .add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize("&l&6" + p.getName() + "'s&r &l&8[&r&aB&dLoader&r&l&8]&r"))
                                .add(Keys.ITEM_LORE, Arrays.asList(
                                        TextSerializers.FORMATTING_CODE.deserialize(
                                                "&bWorld: &a" + loc.getExtent().getName()),

                                        TextSerializers.FORMATTING_CODE.deserialize("&bPos: &a " + loc.getBlockPosition()),

                                        TextSerializers.FORMATTING_CODE.deserialize("&bOffline Loader: &a" + chunkloader.getOffline()),

                                        TextSerializers.FORMATTING_CODE.deserialize("&bRadius: &a" + getRadius(chunkloader.getRadius())),

                                        TextSerializers.FORMATTING_CODE.deserialize("&6" + getCreditsPerHour(chunkloader.getRadius(), chunkloader.getOffline()) + " &aB&dLoader&bCredits &ap/h"),

                                        TextSerializers.FORMATTING_CODE.deserialize(chunkloader.getLoaderID().toString())
                                ))
                                .build());
                    } else {
                        itemList.add(ItemStack.builder()
                                .itemType(ItemTypes.IRON_BLOCK)
                                .add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize("&l&6" + p.getName() + "'s&r &l&8[&r&aB&dLoader&r&l&8]&r"))
                                .add(Keys.ITEM_LORE, Arrays.asList(
                                        TextSerializers.FORMATTING_CODE.deserialize(
                                                "&bWorld: &a" + loc.getExtent().getName()),

                                        TextSerializers.FORMATTING_CODE.deserialize("&bPos: &a " + loc.getBlockPosition()),

                                        TextSerializers.FORMATTING_CODE.deserialize("&bOffline Loader: &c" + chunkloader.getOffline()),

                                        TextSerializers.FORMATTING_CODE.deserialize("&bRadius: &a" + getRadius(chunkloader.getRadius())),

                                        TextSerializers.FORMATTING_CODE.deserialize("&6" + getCreditsPerHour(chunkloader.getRadius(), chunkloader.getOffline()) + " &aB&dLoader&bCredits &ap/h"),

                                        TextSerializers.FORMATTING_CODE.deserialize(chunkloader.getLoaderID().toString())
                                ))
                                .build());
                    }
                }
            }
        }

        return itemList;
    }

    private String getRadius(Integer radius){
        String returnRadius = "1x1";
        if(radius.equals(0)){
            returnRadius="1x1";
        }
        if(radius.equals(1)){
            returnRadius="3x3";
        }
        if(radius.equals(2)){
            returnRadius="5x5";
        }
        if(radius.equals(3)){
            returnRadius="7x7";
        }
        return returnRadius;
    }

    private String getCreditsPerHour(Integer radius, Boolean offline){
        String cph = "";
        if(radius.equals(0)){
            if(offline){
                cph="10";
            } else { cph="5"; }
        }
        if(radius.equals(1)){
            if(offline){
                cph="90";
            } else { cph="45"; }
        }
        if(radius.equals(2)){
            if(offline){
                cph="250";
            } else { cph="125"; }
        }
        if(radius.equals(3)){
            if(offline){
                cph="490";
            } else { cph="245"; }
        }
        return cph;
    }

    public void openGUI(Player p) {
        loaderGUI.clear();
        Inventory pGUI = util.twoRowInventoryBuilder(genItems(p), loaderGUI, p);
        p.openInventory(pGUI);
        pGUI.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(31))).set(genBalButton(p));
    }

    private ItemStack genBalButton(Player player){
        ItemStack balButton = ItemStack.of(ItemTypes.CLOCK, 1);
        Integer bal = new CreditManager().getCredits(player);
        balButton.offer(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize("&l&8[&r&bBalance: &r&l&6"+bal+"&r&aB&dLoader&bCredits&r&l&8]&r"));
        return balButton;
    }

}
