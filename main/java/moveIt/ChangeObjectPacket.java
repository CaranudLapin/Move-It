package moveIt;

import necesse.engine.network.NetworkPacket;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerClient;
import necesse.engine.registries.ItemRegistry;
import necesse.engine.registries.ObjectRegistry;
import necesse.entity.objectEntity.InventoryObjectEntity;
import necesse.entity.objectEntity.ObjectEntity;
import necesse.inventory.InventoryItem;
import necesse.level.gameObject.GameObject;
import necesse.level.maps.Level;

public class ChangeObjectPacket extends Packet {
    public final int tileX;
    public final int tileY;
    public final boolean remove;
    public final String object;
    public final int dir;
    public final boolean itemOverride;
    public final boolean chestOverride;
    public final int playerSlot;
    public final Packet content;

    public ChangeObjectPacket(byte[] data) {
        super(data);
        PacketReader reader = new PacketReader(this);
        this.tileX = reader.getNextInt();
        this.tileY = reader.getNextInt();
        this.remove = reader.getNextBoolean();
        this.object = reader.getNextString();
        this.dir = reader.getNextInt();
        this.itemOverride = reader.getNextBoolean();
        this.chestOverride = reader.getNextBoolean();
        this.playerSlot = reader.getNextInt();
        this.content = reader.getNextContentPacket();
    }

    public ChangeObjectPacket(int tileX, int tileY, boolean remove, GameObject object, int dir, InventoryItem item, boolean itemOverride, boolean chestOverride, int playerSlot) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.remove = remove;
        this.object = object.getStringID();
        this.dir = dir;
        this.itemOverride = itemOverride;
        this.chestOverride = chestOverride;
        this.playerSlot = playerSlot;
        this.content = new Packet();
        item.addPacketContent(new PacketWriter(this.content));

        PacketWriter writer = new PacketWriter(this);
        writer.putNextInt(tileX);
        writer.putNextInt(tileY);
        writer.putNextBoolean(remove);
        writer.putNextString(object.getStringID());
        writer.putNextInt(dir);
        writer.putNextBoolean(itemOverride);
        writer.putNextBoolean(chestOverride);
        writer.putNextInt(playerSlot);
        writer.putNextContentPacket(this.content);
    }

    public void processServer(NetworkPacket packet, Server server, ServerClient client) {
        Level level = client.getLevel();
        InventoryItem item = InventoryItem.fromContentPacket(this.content);
        if (this.remove) {
            level.setObject(this.tileX, this.tileY,0);
        } else {
            boolean modAbort = false;
            if (level.getObject(tileX, tileY).getID() == 0) {
                if (ObjectRegistry.getObjectID(item.getGndData().getString("objectItem")) != -1 || this.chestOverride) {
                    if (this.chestOverride && ObjectRegistry.getObjectID(item.getGndData().getString("objectItem")) == -1) {
                        level.setObject(tileX, tileY, ObjectRegistry.getObjectID("storagebox"), this.dir);
                    } else {
                        level.setObject(tileX, tileY, ObjectRegistry.getObjectID(item.getGndData().getString("objectItem")), this.dir);
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
                                    System.err.println("Unloaded mod item found! " + item.getGndData().getString("slot" + i + "Item"));
                                    modAbort = true;
                                }
                            }
                        }
                        if (modAbort && !this.itemOverride) {
                            level.setObject(tileX, tileY, 0);
                        } else {
                            server.getPlayer(this.playerSlot).getInv().removeItems(server.getPlayer(this.playerSlot).getSelectedItem().item, 1, true, true, true, true, "used");
                        }
                    }
                }
            }
            server.network.sendToAllClientsExcept(this, client);
        }
    }
    public void processClient(NetworkPacket packet, Client client) {
        Level level = client.getLevel();
        InventoryItem item = InventoryItem.fromContentPacket(this.content);
        if (this.remove) {
            level.setObject(this.tileX, this.tileY,0);
        } else {
            boolean modAbort = false;
            if (level.getObject(tileX, tileY).getID() == 0) {
                if (ObjectRegistry.getObjectID(item.getGndData().getString("objectItem")) != -1 || this.chestOverride) {
                    if (this.chestOverride && ObjectRegistry.getObjectID(item.getGndData().getString("objectItem")) == -1) {
                        level.setObject(tileX, tileY, ObjectRegistry.getObjectID("storagebox"), this.dir);
                    } else {
                        level.setObject(tileX, tileY, ObjectRegistry.getObjectID(item.getGndData().getString("objectItem")), this.dir);
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
                                    System.err.println("Unloaded mod item found! " + item.getGndData().getString("slot" + i + "Item"));
                                    modAbort = true;
                                }
                            }
                        }
                        if (modAbort && !this.itemOverride) {
                            level.setObject(tileX, tileY, 0);
                        }
                    }
                }
            }
        }
    }
}