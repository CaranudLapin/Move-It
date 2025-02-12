package moveIt;

import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.registries.ItemRegistry;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.inventory.PlayerInventoryManager;
import necesse.inventory.item.Item;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(target = PlayerInventoryManager.class, name = "tick", arguments = {})
public class PlayerInventoryPatch {
    @Advice.OnMethodExit()
    static void onExit(@Advice.This PlayerInventoryManager playerInv) {
        if (playerInv.getAmount(ItemRegistry.getItem("movingBoxPacked"),true,true,true,true,"box") > 0) {
            ActiveBuff ab = new ActiveBuff("MovingBoxSlow", playerInv.player, 9999F, playerInv.player);
            playerInv.player.addBuff(ab, true);
        } else if (playerInv.player.buffManager.hasBuff("MovingBoxSlow") && playerInv.getAmount(ItemRegistry.getItem("movingBoxPacked"),true,true,true,true,"box") == 0) {
            playerInv.player.buffManager.removeBuff("MovingBoxSlow",true);
        }
    }
}
