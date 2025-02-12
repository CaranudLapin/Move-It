package moveIt;

import necesse.engine.GameEvents;
import necesse.engine.events.players.ItemPlaceEvent;
import necesse.engine.input.Control;
import necesse.engine.localization.Localization;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.registries.ItemRegistry;
import necesse.engine.util.GameBlackboard;
import necesse.engine.window.GameWindow;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.objectEntity.InventoryObjectEntity;
import necesse.entity.objectEntity.ObjectEntity;
import necesse.entity.pickup.ItemPickupEntity;
import necesse.gfx.Renderer;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.itemAttack.ItemAttackDrawOptions;
import necesse.gfx.forms.presets.sidebar.WireEditSidebarForm;
import necesse.gfx.gameTexture.GameSprite;
import necesse.gfx.gameTooltips.GameTooltipManager;
import necesse.gfx.gameTooltips.InputTooltip;
import necesse.gfx.gameTooltips.ListGameTooltips;
import necesse.gfx.gameTooltips.TooltipLocation;
import necesse.inventory.InventoryItem;
import necesse.inventory.PlayerInventorySlot;
import necesse.inventory.item.Item;
import necesse.inventory.item.ItemInteractAction;
import necesse.inventory.item.placeableItem.PlaceableItem;
import necesse.level.maps.Level;
import necesse.level.maps.TilePosition;


public class MovingBoxItem extends PlaceableItem implements ItemInteractAction {
    public MovingBoxItem() {
        super(1, true);
        this.controllerIsTileBasedPlacing = true;
        this.rarity = Rarity.COMMON;
        this.attackXOffset = 8;
        this.attackYOffset = 8;
        this.worldDrawSize = 32;
        this.incinerationTimeMillis = 30000;
        this.stackSize = 1;
    }

    public boolean canCombineItem(Level level, PlayerMob player, InventoryItem me, InventoryItem them, String purpose) {
        return false;
    }

    public GameSprite getAttackSprite(InventoryItem item, PlayerMob player) {
        return this.getItemSprite(item, player);
    }

    public boolean canLevelInteract(Level level, int x, int y, PlayerMob player, InventoryItem item) {
        int tileX = x / 32;
        int tileY = y / 32;
        ObjectEntity entity = level.entityManager.getObjectEntity(tileX, tileY);
        if (entity instanceof InventoryObjectEntity) {
            InventoryObjectEntity invEnt = (InventoryObjectEntity)entity;
            if (invEnt.inventory.getAmount(level,player,ItemRegistry.getItem("movingBoxPacked"),"box") <= 0) {
                return !(player.getPositionPoint().distance((double) (tileX * 32 + 16), (double) (tileY * 32 + 16)) > (double) this.getPlaceRange(item, player));
            } else {
                return false;
            }
        } else if (level.isProtected(tileX, tileY)) {
            return false;
        } else {
            return false;
        }
    }

    public String canInteractError(Level level, int x, int y, PlayerMob player, InventoryItem item) {
        int tileX = x / 32;
        int tileY = y / 32;
        ObjectEntity entity = level.entityManager.getObjectEntity(tileX, tileY);

        if (entity instanceof InventoryObjectEntity) {
            InventoryObjectEntity invEnt = (InventoryObjectEntity)entity;
            if (invEnt.inventory.getAmount(level,player,ItemRegistry.getItem("movingBoxPacked"),"box") <= 0) {
                return player.getPositionPoint().distance((double) (tileX * 32 + 16), (double) (tileY * 32 + 16)) > (double) this.getPlaceRange(item, player) ? "outofrange" : null;
            } else {
                return "chest";
            }
        } else if (level.isProtected(tileX, tileY)) {
            return "protected";
        } else {
            return "blocked";
        }
    }

    public boolean overridesObjectInteract(Level level, PlayerMob player, InventoryItem item) {
        return true;
    }

    public InventoryItem onLevelInteract(Level level, int x, int y, PlayerMob player, int attackHeight, InventoryItem item, PlayerInventorySlot slot, int seed, PacketReader contentReader) {
        int tileX = x / 32;
        int tileY = y / 32;
        if (level.isServer()) {
            ObjectEntity entity = level.entityManager.getObjectEntity(tileX, tileY);
            if (entity instanceof InventoryObjectEntity) {
                InventoryObjectEntity invEnt = (InventoryObjectEntity) entity;
                if (invEnt.inventory.getAmount(level,player,ItemRegistry.getItem("movingBoxPacked"),"box") <= 0) {

                    ItemRegistry.getItem(invEnt.getObject().getObjectItem().getStringID());
                    int slots = invEnt.slots;
                    Item storageCrateItem = ItemRegistry.getItem("movingBoxPacked");

                    InventoryItem storageCrateInvItem = new InventoryItem(storageCrateItem, 1);
                    storageCrateInvItem.setGndData(storageCrateInvItem.getGndData().setString("objectItem", invEnt.getObject().getObjectItem().getStringID()));
                    storageCrateInvItem.setGndData(storageCrateInvItem.getGndData().setString("objectName", invEnt.getInventoryName().translate()));
                    storageCrateInvItem.setGndData(storageCrateInvItem.getGndData().setInt("slots", slots));
                    for (int i = 0; i <= slots; i++) {
                        if (!invEnt.inventory.isSlotClear(i)) {
                            storageCrateInvItem.setGndData(storageCrateInvItem.getGndData().setString("slot" + i + "Item", invEnt.inventory.getItemStringID(i)));
                            storageCrateInvItem.setGndData(storageCrateInvItem.getGndData().setInt("slot" + i + "Amount", invEnt.inventory.getAmount(i)));
                            storageCrateInvItem.setGndData(storageCrateInvItem.getGndData().setInt("slot" + i + "GNDSize", invEnt.inventory.getItem(i).getGndData().getMapSize()));

                            if (invEnt.inventory.getItem(i).getGndData().getMapSize() > 0) {
                                Object[] keys = invEnt.inventory.getItem(i).getGndData().getKeySet().toArray();
                                for (int j = 0; j < invEnt.inventory.getItem(i).getGndData().getKeySet().size(); j++) {
                                    String key = keys[j].toString();

                                    storageCrateInvItem.setGndData(storageCrateInvItem.getGndData().setInt("slot" + i + "GNDKey" + j, (Integer) keys[j]));
                                    storageCrateInvItem.setGndData(storageCrateInvItem.getGndData().setItem("slot" + i + "GNDData" + j, invEnt.inventory.getItem(i).getGndData().getItem(key)));
                                }
                            }
                        }
                    }
                    level.entityManager.pickups.add(new ItemPickupEntity(level, storageCrateInvItem, x, y, 0, 0));
                    level.setObject(tileX, tileY, 0);
                    BackupFileGenerator.generateNewFile(tileX,tileY,storageCrateInvItem);
                    level.getServer().network.sendToAllClients(new ChangeObjectPacket(tileX,tileY,true,level.getObject(tileX,tileY),-1, storageCrateInvItem,player.buffManager.hasBuff("MovingBoxItemOverride"),player.buffManager.hasBuff("MovingBoxChestOverride"),player.getPlayerSlot()));
                    player.getInv().removeItems(this, 1, true, true, true, true, "used");
                }
            }
        }
        return item;
    }

    public String canPlace(Level level, int x, int y, PlayerMob player, InventoryItem item, PacketReader contentReader) {
        return "blocked";
    }

    public void setupAttackContentPacket(PacketWriter writer, Level level, int x, int y, PlayerMob player, InventoryItem item) {
        super.setupAttackContentPacket(writer, level, x, y, player, item);
        for(int i = 0; i < 4; ++i) {
            writer.putNextBoolean(WireEditSidebarForm.isEditing(i));
        }
    }

    public InventoryItem onPlace(Level level, int x, int y, PlayerMob player, InventoryItem item, PacketReader contentReader) {
        int tileX = x / 32;
        int tileY = y / 32;
        ItemPlaceEvent event = new ItemPlaceEvent(level, tileX, tileY, player, item);
        GameEvents.triggerEvent(event);
        return item;
    }

    public void draw(InventoryItem item, PlayerMob perspective, int x, int y, boolean inInventory) {
        super.draw(item, perspective, x, y, inInventory);
    }

    public ListGameTooltips getTooltips(InventoryItem item, PlayerMob perspective, GameBlackboard blackboard) {
        ListGameTooltips tooltips = super.getTooltips(item, perspective, blackboard);
        tooltips.add(Localization.translate("itemtooltip", "movingboxitemtip"));
        tooltips.add(Localization.translate("itemtooltip", "movingboxitemtip2"));
        tooltips.add(Localization.translate("itemtooltip", "movingboxitemtip3"));
        tooltips.add(Localization.translate("itemtooltip", "movingboxitemtip4"));
        return tooltips;
    }

    public void setDrawAttackRotation(InventoryItem item, ItemAttackDrawOptions drawOptions, float attackDirX, float attackDirY, float attackProgress) {
        drawOptions.swingRotation(attackProgress);
    }

    public String canAttack(Level level, int x, int y, PlayerMob player, InventoryItem item) {
        return null;
    }

    public void drawPlacePreview(Level level, int x, int y, GameCamera camera, PlayerMob player, InventoryItem item, PlayerInventorySlot slot) {}

    public void onMouseHoverTile(InventoryItem item, GameCamera camera, PlayerMob perspective, int mouseX, int mouseY, TilePosition pos, boolean isDebug) {
        String interactError = this.canInteractError(pos.level, mouseX, mouseY, perspective, item);
        if (interactError == null) {
            Renderer.setCursor(GameWindow.CURSOR.INTERACT);
            GameTooltipManager.addTooltip(new InputTooltip(Control.MOUSE2, Localization.translate("controls", "pickup")), TooltipLocation.INTERACT_FOCUS);
        }
    }
}