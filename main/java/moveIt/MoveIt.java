package moveIt;


import necesse.engine.commands.CommandsManager;
import necesse.engine.modLoader.*;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.registries.*;

import increasedStackSize.Config;
import necesse.inventory.recipe.Ingredient;
import necesse.inventory.recipe.Recipe;
import necesse.inventory.recipe.Recipes;
import necesse.level.gameObject.GameObject;
import necesse.level.gameObject.furniture.InventoryObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@ModEntry
public class MoveIt {

    //public static boolean itemOverride;
    //public static boolean chestOverride;

    public void init() {
        System.out.println("Move-It! Loaded!");
        //itemOverride = false;
        //chestOverride = false;

        ItemRegistry.registerItem("movingBox", new MovingBoxItem(),5f,true,true);
        ItemRegistry.registerItem("movingBoxPacked", new MovingBoxChest(),1f,false,false);

        BuffRegistry.registerBuff("MovingBoxSlow", new MovingBoxSlow());
        BuffRegistry.registerBuff("MovingBoxItemOverride", new MovingBoxItemOverride());
        BuffRegistry.registerBuff("MovingBoxChestOverride", new MovingBoxChestOverride());

        PickupRegistry.registerPickup("movingBoxpickupentity", MovingBoxPickupEntity.class);

        PacketRegistry.registerPacket(ChangeObjectPacket.class);
        modifyBlacklist();

        Iterator objects = ObjectRegistry.getObjects().iterator();
        while (objects.hasNext()) {
            GameObject object = (GameObject)objects.next();
            if (object instanceof InventoryObject) {
                if (!object.getStringID().equalsIgnoreCase("barrel")) {
                    object.getObjectItem().addGlobalIngredient(new String[]{"anychest"});
                }
            }
        }
    }

    //Written by @ferrenfx (UserId: 493642279592919050) on the Necesse Discord
    //modified (slightly) by me
    public static void modifyBlacklist() {
        try {
            boolean found = false;
            Iterator mods = ModLoader.getEnabledMods().iterator();
            while (mods.hasNext()) {
                LoadedMod mod = (LoadedMod)mods.next();
                if (mod.id.equalsIgnoreCase("dianchia.increasedstacksize")) {
                    found = true;
                    break;
                }
            }
            if (found) {
                Field blacklistField = Config.class.getDeclaredField("blacklist");
                blacklistField.setAccessible(true);

                // Get current blacklist and modify it
                List<Class<?>> blacklist = new ArrayList<>((List<Class<?>>) blacklistField.get(null));
                blacklist.add(MovingBoxItem.class);
                blacklist.add(MovingBoxChest.class);

                // Replace the static field value
                blacklistField.set(null, blacklist);
                //System.out.println("Blacklist modified successfully! New Contents: " + new String(blacklist.toString()));
            }
        } catch (Exception ignored) {

        }
    }

    public void postInit() {
        Recipes.registerModRecipe(new Recipe(
            "movingBox",
            1,
            RecipeTechRegistry.DEMONIC_WORKSTATION,
            new Ingredient[]{
                    new Ingredient("anychest", 1),
                    new Ingredient("leather", 5)
            }
        ));

        CommandsManager.registerServerCommand(new SafetyOverrideCommand());
    }
}
