package io.github.hhservers.bloader.util;

import io.github.hhservers.bloader.BLoader;
import io.github.hhservers.bloader.config.Chunkloader;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.World;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CreateLoaderGUI {

    private Util util = new Util();
    private ChunkManager chunkManager = BLoader.getInstance().getChunkManager();
    private CreditManager creditManager = new CreditManager();

    private ItemStack genBalButton(Player player){
        ItemStack balButton = ItemStack.of(ItemTypes.CLOCK, 1);
        Integer bal = creditManager.getCredits(player);
        balButton.offer(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize("&l&8[&r&bBalance: &r&l&6"+bal+"&r&aB&dLoader&bCredits&r&l&8]&r"));
        return balButton;
    }

    private List<ItemStack> genItems(Chunkloader chunkloader) {
        if (chunkloader.getOffline()) {
            ItemStack radius1 = ItemStack.builder()
                    .add(Keys.DISPLAY_NAME, util.textSerializer("&b1x1 &l&8[&r&aB&dLoader&l&8]&r"))
                    .add(Keys.ITEM_LORE, Arrays.asList(util.textSerializer("&610&aB&dLoader&bCredits &ap/h")))
                    .itemType(ItemTypes.DIAMOND_BLOCK)
                    .build();
            ItemStack radius3 = ItemStack.builder()
                    .add(Keys.DISPLAY_NAME, util.textSerializer("&b3x3 &l&8[&r&aB&dLoader&l&8]&r"))
                    .add(Keys.ITEM_LORE, Arrays.asList(util.textSerializer("&690&aB&dLoader&bCredits &ap/h")))
                    .itemType(ItemTypes.DIAMOND_BLOCK)
                    .build();
            ItemStack radius5 = ItemStack.builder()
                    .add(Keys.DISPLAY_NAME, util.textSerializer("&b5x5 &l&8[&r&aB&dLoader&l&8]&r"))
                    .add(Keys.ITEM_LORE, Arrays.asList(util.textSerializer("&6250&aB&dLoader&bCredits &ap/h")))
                    .itemType(ItemTypes.DIAMOND_BLOCK)
                    .build();
            ItemStack radius7 = ItemStack.builder()
                    .add(Keys.DISPLAY_NAME, util.textSerializer("&b7x7 &l&8[&r&aB&dLoader&l&8]&r"))
                    .add(Keys.ITEM_LORE, Arrays.asList(util.textSerializer("&6490&aB&dLoader&bCredits &ap/h")))
                    .itemType(ItemTypes.DIAMOND_BLOCK)
                    .build();
            return Arrays.asList(radius1, radius3, radius5, radius7);
        } else {
            ItemStack radius1 = ItemStack.builder()
                    .add(Keys.DISPLAY_NAME, util.textSerializer("&b1x1 &l&8[&r&aB&dLoader&l&8]&r"))
                    .add(Keys.ITEM_LORE, Arrays.asList(util.textSerializer("&65&aB&dLoader&bCredits &ap/h")))
                    .itemType(ItemTypes.IRON_BLOCK)
                    .build();
            ItemStack radius3 = ItemStack.builder()
                    .add(Keys.DISPLAY_NAME, util.textSerializer("&b3x3 &l&8[&r&aB&dLoader&l&8]&r"))
                    .add(Keys.ITEM_LORE, Arrays.asList(util.textSerializer("&645&aB&dLoader&bCredits &ap/h")))
                    .itemType(ItemTypes.IRON_BLOCK)
                    .build();
            ItemStack radius5 = ItemStack.builder()
                    .add(Keys.DISPLAY_NAME, util.textSerializer("&b5x5 &l&8[&r&aB&dLoader&l&8]&r"))
                    .add(Keys.ITEM_LORE, Arrays.asList(util.textSerializer("&6125&aB&dLoader&bCredits &ap/h")))
                    .itemType(ItemTypes.IRON_BLOCK)
                    .build();
            ItemStack radius7 = ItemStack.builder()
                    .add(Keys.DISPLAY_NAME, util.textSerializer("&b7x7 &l&8[&r&aB&dLoader&l&8]&r"))
                    .add(Keys.ITEM_LORE, Arrays.asList(util.textSerializer("&6245&aB&dLoader&bCredits &ap/h")))
                    .itemType(ItemTypes.IRON_BLOCK)
                    .build();
            return Arrays.asList(radius1, radius3, radius5, radius7);
        }
    }

    public void openGUI(Player p, Chunkloader chunkloader) {
        Inventory loaderGUI = Inventory.builder().of(InventoryArchetypes.CHEST)
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9, 3))
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(TextSerializers.FORMATTING_CODE.deserialize("&l&8[&r&aB&dLoader&l&8]&r")))
                .listener(ClickInventoryEvent.class, clickInventoryEvent -> clickInventoryEvent.setCancelled(true))
                .listener(ClickInventoryEvent.Primary.class, clickInventoryEvent -> {
                    try {
                        loaderGUIListener(clickInventoryEvent, chunkloader);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }).build(BLoader.getInstance());
        loaderGUI.clear();
        Inventory pGUI = util.inventoryBuilder(genItems(chunkloader), loaderGUI, p);
        p.openInventory(pGUI);
        pGUI.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(4, 2))).set(genBalButton(p));
    }

    private void loaderGUIListener(ClickInventoryEvent e, Chunkloader chunkloader) throws SQLException {
        e.setCancelled(true);
        Player p = e.getCause().first(Player.class).get();
        Transaction<ItemStackSnapshot> clickTransaction = e.getTransactions().get(0);
        SlotTransaction slotTransaction = e.getTransactions().get(0);
        Slot slotClicked = slotTransaction.getSlot();
        SlotIndex index = slotClicked.getInventoryProperty(SlotIndex.class).get();
        ItemStack item = clickTransaction.getOriginal().createStack();
        World w = p.getWorld();

        chunkloader.setServerName(BLoader.getMainPluginConfig().getServeName());
        chunkloader.setOwner(p.getUniqueId());
        chunkloader.setLoaderID(UUID.randomUUID());
        chunkloader.setCreationDate(System.currentTimeMillis());
        if (item.getType().equals(ItemTypes.DIAMOND_BLOCK)) {
                if (index.getValue().equals(10)) {
                    p.closeInventory();
                    chunkloader.setRadius(0);
                    if (creditManager.enoughCredits(p, 10)) {
                        if (chunkManager.loadChunkLoader(chunkloader)) {
                            addToList(chunkloader);
                            p.sendMessage(util.prefixSerializer("&bChunk loaded!"));
                            p.sendMessage(util.prefixSerializer("&bChunk loaded!"));
                        }
                    } else {
                        p.sendMessage(util.prefixSerializer("&bYou do not have enough BLoader credits to last the hour if you create this loader!"));
                    }
                }
                if (index.getValue().equals(11)) {
                    p.closeInventory();
                    chunkloader.setRadius(1);
                    if (creditManager.enoughCredits(p, 90)) {
                        if (chunkManager.loadChunkLoader(chunkloader)) {
                            addToList(chunkloader);
                            p.sendMessage(util.prefixSerializer("&bChunk loaded!"));
                        }
                    } else {
                        p.sendMessage(util.prefixSerializer("&bYou do not have enough BLoader credits to last the hour if you create this loader!"));
                    }
                }
                if (index.getValue().equals(12)) {
                    p.closeInventory();
                    chunkloader.setRadius(2);
                    if (creditManager.enoughCredits(p, 250)) {
                        if (chunkManager.loadChunkLoader(chunkloader)) {
                            addToList(chunkloader);
                            p.sendMessage(util.prefixSerializer("&bChunk loaded!"));
                        }
                    } else {
                        p.sendMessage(util.prefixSerializer("&bYou do not have enough BLoader credits to last the hour if you create this loader!"));
                    }
                }
                if (index.getValue().equals(13)) {
                    p.closeInventory();
                    chunkloader.setRadius(3);
                    if (creditManager.enoughCredits(p, 490)) {
                        if (chunkManager.loadChunkLoader(chunkloader)) {
                            addToList(chunkloader);
                            p.sendMessage(util.prefixSerializer("&bChunk loaded!"));
                        }
                    } else {
                        p.sendMessage(util.prefixSerializer("&bYou do not have enough BLoader credits to last the hour if you create this loader!"));
                    }
                }

        }
        if (item.getType().equals(ItemTypes.IRON_BLOCK)) {
                if (index.getValue().equals(10)) {
                    p.closeInventory();
                    chunkloader.setRadius(0);
                    if (creditManager.enoughCredits(p, 5)) {
                        if (chunkManager.loadChunkLoader(chunkloader)) {
                            addToList(chunkloader);
                            p.sendMessage(util.prefixSerializer("&bChunk loaded!"));
                        }
                    } else {
                        p.sendMessage(util.prefixSerializer("&bYou do not have enough BLoader credits to last the hour if you create this loader!"));
                    }
                }
                if (index.getValue().equals(11)) {
                    p.closeInventory();
                    chunkloader.setRadius(1);
                    if (creditManager.enoughCredits(p, 45)) {
                        if (chunkManager.loadChunkLoader(chunkloader)) {
                            addToList(chunkloader);
                            p.sendMessage(util.prefixSerializer("&bChunk loaded!"));
                        }
                    } else {
                        p.sendMessage(util.prefixSerializer("&bYou do not have enough BLoader credits to last the hour if you create this loader!"));
                    }
                }
                if (index.getValue().equals(12)) {
                    p.closeInventory();
                    chunkloader.setRadius(2);
                    if (creditManager.enoughCredits(p, 125)) {
                        if (chunkManager.loadChunkLoader(chunkloader)) {
                            addToList(chunkloader);
                            p.sendMessage(util.prefixSerializer("&bChunk loaded!"));
                        }
                    } else {
                        p.sendMessage(util.prefixSerializer("&bYou do not have enough BLoader credits to last the hour if you create this loader!"));
                    }
                }
                if (index.getValue().equals(13)) {
                    p.closeInventory();
                    chunkloader.setRadius(3);
                    if (creditManager.enoughCredits(p, 245)) {
                        if (chunkManager.loadChunkLoader(chunkloader)) {
                            addToList(chunkloader);
                            p.sendMessage(util.prefixSerializer("&bChunk loaded!"));
                        }
                    } else {
                        p.sendMessage(util.prefixSerializer("&bYou do not have enough BLoader credits to last the hour if you create this loader!"));
                    }
                }
        }
    }

    private void addToList(Chunkloader chunkloader) throws SQLException {
        BLoader.getInstance().getCachedLoaderList().add(chunkloader);
        BLoader.getInstance().getActiveLoaderMasterList().add(chunkloader);
        BLoader.getInstance().getDataStore().addChunkLoaderData(chunkloader);
    }

}
