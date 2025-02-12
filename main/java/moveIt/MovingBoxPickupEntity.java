package moveIt;

import necesse.engine.util.GameLinkedList;
import necesse.engine.util.GameRandom;
import necesse.entity.pickup.ItemPickupEntity;
import necesse.inventory.InventoryItem;
import necesse.level.maps.Level;

public class MovingBoxPickupEntity extends ItemPickupEntity {
    public MovingBoxPickupEntity() {
        this.reservedPickups = new GameLinkedList();
        this.bouncy = 0.75F;
    }

    public MovingBoxPickupEntity(Level level, InventoryItem item, float x, float y, float dx, float dy, float height, float dh) {
        super(level, item, x, y, dx, dy);
        this.reservedPickups = new GameLinkedList();
        this.height = height;
        this.dh = dh;
        this.item = item;
        this.item.setLocked(false);
        this.bouncy = 0.75F;
    }

    public MovingBoxPickupEntity(Level level, InventoryItem item, float x, float y, float dx, float dy) {
        this(level, item, x, y, dx, dy, GameRandom.globalRandom.getFloatBetween(5.0F, 15.0F), GameRandom.globalRandom.getFloatBetween(20.0F, 30.0F));
    }

    public boolean canBePickedUpBySettlers() {
        return false;
    }
}
