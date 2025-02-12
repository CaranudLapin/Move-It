package moveIt;

import necesse.engine.modLoader.ModSettings;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;

public class Settings extends ModSettings {
    public boolean itemOverride = false;
    public boolean chestOverride = false;

    @Override
    public void addSaveData(SaveData save) {
        save.addBoolean("itemOverride", itemOverride);
        save.addBoolean("chestOverride", chestOverride);
    }

    @Override
    public void applyLoadData(LoadData save) {
        if (save == null)
            return;
        itemOverride = save.getBoolean("itemOverride",false);
        chestOverride = save.getBoolean("chestOverride",false);
    }


}
