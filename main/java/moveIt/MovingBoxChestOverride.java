package moveIt;

import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.mobs.buffs.staticBuffs.Buff;

public class MovingBoxChestOverride extends Buff {
    public MovingBoxChestOverride() {
        this.canCancel = false;
        this.isImportant = true;
        this.isPassive = true;
        this.isVisible = false;
        this.shouldSave = false;
    }

    public void init(ActiveBuff buff, BuffEventSubscriber eventSubscriber) {
        buff.setModifier(BuffModifiers.SLOW, 0.0F);
    }
}