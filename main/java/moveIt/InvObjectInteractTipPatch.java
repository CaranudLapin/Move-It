package moveIt;

import necesse.engine.localization.Localization;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.registries.ItemRegistry;
import necesse.entity.mobs.PlayerMob;
import necesse.inventory.item.Item;
import necesse.inventory.item.miscItem.SeedPouch;
import necesse.level.gameObject.furniture.InventoryObject;
import necesse.level.maps.Level;
import net.bytebuddy.asm.Advice;

@ModMethodPatch(target = InventoryObject.class, name = "getInteractTip", arguments = {Level.class,int.class,int.class,PlayerMob.class,boolean.class})
public class InvObjectInteractTipPatch {
    @Advice.OnMethodEnter(
            skipOn = Advice.OnNonDefaultValue.class
    )
    static boolean onEnter() {
        return true;
    }
    @Advice.OnMethodExit()
    static String onExit(@Advice.This InventoryObject object, @Advice.Argument(0) Level level, @Advice.Argument(1) int x, @Advice.Argument(2) int y, @Advice.Argument(3) PlayerMob perspective, @Advice.Argument(4) boolean debug, @Advice.Return(readOnly = false) String tip){
        if (perspective.getSelectedItem() != null) {
            if (perspective.getSelectedItem().item.getStringID().equalsIgnoreCase("movingBox")) {
                //if (perspective.getInv().getAmount(ItemRegistry.getItem("movingBox"),true,true,true,true,"box") > 0) {
                return tip;
            } else {
                tip = Localization.translate("controls", "opentip");
            }
        } else {
            tip = Localization.translate("controls", "opentip");
        }
        return tip;
    }
}
