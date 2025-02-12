package moveIt;

import necesse.engine.GameEvents;
import necesse.engine.events.players.ItemPlaceEvent;
import necesse.engine.input.Control;
import necesse.engine.localization.Localization;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.registries.ItemRegistry;
import necesse.engine.registries.ObjectRegistry;
import necesse.engine.util.GameBlackboard;
import necesse.engine.window.GameWindow;
import necesse.entity.mobs.MaskShaderOptions;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.objectEntity.InventoryObjectEntity;
import necesse.entity.objectEntity.ObjectEntity;
import necesse.entity.pickup.ItemPickupEntity;
import necesse.gfx.Renderer;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.itemAttack.ItemAttackDrawOptions;
import necesse.gfx.gameTexture.GameSprite;
import necesse.gfx.gameTooltips.GameTooltipManager;
import necesse.gfx.gameTooltips.InputTooltip;
import necesse.gfx.gameTooltips.ListGameTooltips;
import necesse.gfx.gameTooltips.TooltipLocation;
import necesse.inventory.InventoryItem;
import necesse.inventory.PlayerInventorySlot;
import necesse.inventory.item.ItemInteractAction;
import necesse.inventory.item.placeableItem.PlaceableItem;
import necesse.level.maps.Level;
import necesse.level.maps.TilePosition;
import necesse.level.maps.light.GameLight;


public class MovingBoxChest extends PlaceableItem implements ItemInteractAction {
    public MovingBoxChest() {
        super(1, true);
        this.controllerIsTileBasedPlacing = true;
        this.rarity = Rarity.COMMON;
        this.attackXOffset = 8;
        this.attackYOffset = 8;
        this.worldDrawSize = 32;
        this.incinerationTimeMillis = 90000;
        this.stackSize = 1;
    }

    public boolean canCombineItem(Level level, PlayerMob player, InventoryItem me, InventoryItem them, String purpose) {
        return false;
    }

    public boolean holdItemInFrontOfArms(InventoryItem item, PlayerMob player, int spriteX, int spriteY, int drawX, int drawY, int width, int height, boolean mirrorX, boolean mirrorY, GameLight light, float alpha, MaskShaderOptions mask) {
        return true;
    }

    public String canAttack(Level level, int x, int y, PlayerMob player, InventoryItem item) {
        return null;
    }

    public GameSprite getAttackSprite(InventoryItem item, PlayerMob player) {
        return this.getItemSprite(item, player);
    }

    public boolean canLevelInteract(Level level, int x, int y, PlayerMob player, InventoryItem item) {
        int tileX = x / 32;
        int tileY = y / 32;
        if (level.isProtected(tileX, tileY)) {
            return false;
        } else {
            if (player.getPositionPoint().distance((double) (tileX * 32 + 16), (double) (tileY * 32 + 16)) < (double) this.getPlaceRange(item, player)) {
                return level.getObject(tileX, tileY).getID() == 0;
            } else {
                return false;
            }
        }
    }

    public String canInteractError(Level level, int x, int y, PlayerMob player, InventoryItem item) {
        int tileX = x / 32;
        int tileY = y / 32;
        if (level.getObject(tileX, tileY).getID() == 0 && player.getPositionPoint().distance((double) (tileX * 32 + 16), (double) (tileY * 32 + 16)) < (double) this.getPlaceRange(item, player)) {
            return null;
        } else if (level.isProtected(tileX, tileY)) {
            return "protected";
        } else {
            return "blocked";
        }
    }

    public boolean overridesObjectInteract(Level level, PlayerMob player, InventoryItem item) {
        return false;
    }

    public InventoryItem onLevelInteract(Level level, int x, int y, PlayerMob player, int attackHeight, InventoryItem item, PlayerInventorySlot slot, int seed, PacketReader contentReader) {
        int tileX = x / 32;
        int tileY = y / 32;
        if (!level.isServer()) {
            boolean modAbort = false;
            if (level.getObject(tileX, tileY).getID() == 0) {
                if (ObjectRegistry.getObjectID(item.getGndData().getString("objectItem")) != -1 || player.buffManager.hasBuff("MovingBoxChestOverride")) {
                    if (player.buffManager.hasBuff("MovingBoxChestOverride") && ObjectRegistry.getObjectID(item.getGndData().getString("objectItem")) == -1) {
                        level.setObject(tileX, tileY, ObjectRegistry.getObjectID("storagebox"), player.getDir());
                    } else {
                        level.setObject(tileX, tileY, ObjectRegistry.getObjectID(item.getGndData().getString("objectItem")), player.getDir());
                    }
                    ObjectEntity objEnt = level.entityManager.getObjectEntity(tileX, tileY);
                    if (objEnt instanceof InventoryObjectEntity) {
                        InventoryObjectEntity invEnt = (InventoryObjectEntity) objEnt;
                        invEnt.setInventoryName(item.getGndData().getString("objectName", invEnt.getInventoryName().toString()));
                        int slots = invEnt.slots;
                        for (int i = 0; i <= slots; i++) {
                            if (!item.getGndData().getString("slot" + i + "Item").isEmpty()) {
                                if (ItemRegistry.getItem(item.getGndData().getString("slot" + i + "Item")) != null) {
                                    InventoryItem invItem = new InventoryItem(ItemRegistry.getItem(item.getGndData().getString("slot" + i + "Item")), item.getGndData().getInt("slot" + i + "Amount"));
                                    if (item.getGndData().getInt("slot" + i + "GNDSize") > 0) {
                                        for (int j = 0; j < item.getGndData().getInt("slot" + i + "GNDSize"); j++) {
                                            int key = item.getGndData().getInt("slot" + i + "GNDKey" + j);
                                            invItem.setGndData(invItem.getGndData().setItem(key, item.getGndData().getItem("slot" + i + "GNDData" + j)));
                                        }
                                    }
                                    invEnt.inventory.setItem(i, invItem);
                                } else {
                                    System.err.println("Unloaded mod item found!");
                                    modAbort = true;
                                    if (!level.isServer()) {
                                        player.getClient().chat.addMessage("§#FF0000WARNING: Mod item found. Item id: §#FFFFFF" + item.getGndData().getString("slot" + i + "Item"));
                                    }
                                }
                            }
                        }
                        if (!modAbort || player.buffManager.hasBuff("MovingBoxItemOverride")) {
                            player.buffManager.removeBuff("MovingBoxSlow", true);
                            player.getClient().network.sendPacket(new ChangeObjectPacket(tileX, tileY, false, level.getObject(tileX, tileY), player.getDir(),item,player.buffManager.hasBuff("MovingBoxItemOverride"),player.buffManager.hasBuff("MovingBoxChestOverride"),player.getPlayerSlot()));
                        } else {
                            level.setObject(tileX, tileY, 0);
                            if (!level.isServer()) {
                                player.getClient().chat.addMessage("§#F2800DChest placement aborted due to mod item inside.");
                                player.getClient().chat.addMessage("§#F2800DUse \"/safetyoverride item\" to override abort.");
                            }
                        }
                    }
                } else {
                    if (!level.isServer()) {
                        player.getClient().chat.addMessage("§#FF0000WARNING: Mod chest found. Chest id: §#FFFFFF" + item.getGndData().getString("objectItem"));
                        player.getClient().chat.addMessage("§#F2800DChest placement aborted due to mod chest.");
                        player.getClient().chat.addMessage("§#F2800DUse \"/safetyoverride chest\" to override abort.");
                    }
                }
            }
        }
        return item;
    }

    public String canPlace(Level level, int x, int y, PlayerMob player, InventoryItem item, PacketReader contentReader) {
        int tileX = x / 32;
        int tileY = y / 32;
        if (level.getObject(tileX, tileY).getID() == 0 && player.getPositionPoint().distance((double) (tileX * 32 + 16), (double) (tileY * 32 + 16)) < (double) this.getPlaceRange(item, player)) {
            return null;
        } else if (level.isProtected(tileX, tileY)) {
            return "protected";
        } else {
            return "blocked";
        }
    }

    public void setupAttackContentPacket(PacketWriter writer, Level level, int x, int y, PlayerMob player, InventoryItem item) {
        super.setupAttackContentPacket(writer, level, x, y, player, item);
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
        tooltips.add(Localization.translate("itemtooltip", "movingboxchesttip"));
        tooltips.add(Localization.translate("itemtooltip", "movingboxchesttip2"));
        tooltips.add(Localization.translate("itemtooltip", "movingboxchestnametip","key",item.getGndData().getString("objectName")));
        return tooltips;
    }

    public void setDrawAttackRotation(InventoryItem item, ItemAttackDrawOptions drawOptions, float attackDirX, float attackDirY, float attackProgress) {
        drawOptions.swingRotation(attackProgress);
    }

    public void drawPlacePreview(Level level, int x, int y, GameCamera camera, PlayerMob player, InventoryItem item, PlayerInventorySlot slot) {
        int tileX = x / 32;
        int tileY = y / 32;
        if (this.canPlace(level, x, y, player, item, (PacketReader)null) == null) {
            if (ItemRegistry.getItem(item.getGndData().getString("objectItem")) != null) {
                ObjectRegistry.getObject(item.getGndData().getString("objectItem")).drawPreview(level, tileX, tileY, player.getDir(), 0.5F, player, camera);
            }
        }
    }

    public void onMouseHoverTile(InventoryItem item, GameCamera camera, PlayerMob perspective, int mouseX, int mouseY, TilePosition pos, boolean isDebug) {
        super.onMouseHoverTile(item, camera, perspective, mouseX, mouseY, pos, isDebug);
        String interactError = this.canInteractError(pos.level, mouseX, mouseY, perspective, item);
        if (interactError == null) {
            Renderer.setCursor(GameWindow.CURSOR.INTERACT);
            GameTooltipManager.addTooltip(new InputTooltip(Control.MOUSE2, Localization.translate("controls", "unpack")), TooltipLocation.INTERACT_FOCUS);
        }
    }
    public ItemPickupEntity getPickupEntity(Level level, InventoryItem item, float x, float y, float dx, float dy) {
        return new MovingBoxPickupEntity(level, item, x, y, dx, dy);
    }
}