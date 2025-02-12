package moveIt;

import necesse.engine.commands.CmdParameter;
import necesse.engine.commands.CommandLog;
import necesse.engine.commands.ModularChatCommand;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.commands.parameterHandlers.PresetStringParameterHandler;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.entity.mobs.buffs.ActiveBuff;

public class SafetyOverrideCommand extends ModularChatCommand {

    public SafetyOverrideCommand() {
        super("safetyoverride", "Override checks to prevent mod items from being lost.", PermissionLevel.USER, false, new CmdParameter("item/chest", new PresetStringParameterHandler("item", "chest"), false));
    }
    @Override
    public void runModular(Client client, Server server, ServerClient serverClient, Object[] args, String[] errors, CommandLog commandLog) {
        String string = (String) args[0];
        if (string.equalsIgnoreCase("item")) {
            if (!serverClient.playerMob.buffManager.hasBuff("MovingBoxItemOverride")) {
                ActiveBuff ab = new ActiveBuff("MovingBoxItemOverride", serverClient.playerMob, 9999F, serverClient.playerMob);
                serverClient.playerMob.addBuff(ab,true);
                commandLog.add("Mod item override enabled. Mod items will be deleted.");
            } else {
                serverClient.playerMob.buffManager.removeBuff("MovingBoxItemOverride",true);
                commandLog.add("Mod item override disabled.");
            }
        } else if (string.equalsIgnoreCase("chest")) {
            if (!serverClient.playerMob.buffManager.hasBuff("MovingBoxChestOverride")) {
                ActiveBuff ab = new ActiveBuff("MovingBoxChestOverride", serverClient.playerMob, 9999F, serverClient.playerMob);
                serverClient.playerMob.addBuff(ab,true);
                commandLog.add("Mod chest override enabled. Mod chests will be turned into a Storagebox.");
            } else {
                serverClient.playerMob.buffManager.removeBuff("MovingBoxChestOverride",true);
                commandLog.add("Mod chest override disabled.");
            }
        }
    }
}