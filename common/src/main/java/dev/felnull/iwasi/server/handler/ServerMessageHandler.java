package dev.felnull.iwasi.server.handler;

import dev.architectury.networking.NetworkManager;
import dev.felnull.iwasi.data.IWPlayerData;
import dev.felnull.iwasi.item.GunItem;
import dev.felnull.iwasi.networking.IWPackets;
import dev.felnull.iwasi.util.IWPlayerUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class ServerMessageHandler {
    public static void onContinuousActionInputMessage(IWPackets.ContinuousActionInputMessage message, NetworkManager.PacketContext packetContext) {
        packetContext.queue(() -> packetContext.getPlayer().getEntityData().set(IWPlayerData.CONTINUOUS_ACTION, message.data));
    }

    public static void onActionInputMessage(IWPackets.ActionInputMessage message, NetworkManager.PacketContext packetContext) {
        packetContext.queue(() -> {
            switch (message.action) {
                case RELOAD -> {
                    boolean flg = false;
                    for (InteractionHand hand : InteractionHand.values()) {
                        var ogt = IWPlayerData.getGunTrans(packetContext.getPlayer(), hand);
                        if (ogt != null)
                            flg = true;
                    }
                    if (flg) return;
                    for (InteractionHand hand : InteractionHand.values()) {
                        var item = packetContext.getPlayer().getItemInHand(hand);
                        if (item.getItem() instanceof GunItem gunItem) {
                            var ri = gunItem.getGun().getReloadedItem((ServerPlayer) packetContext.getPlayer(), hand, item);
                            if (!ri.isEmpty()) {
                                ItemStack mg = GunItem.getMagazine(item);

                                IWPlayerData.setGunTrans((ServerPlayer) packetContext.getPlayer(), hand, gunItem.getGun().getReloadTrans(mg.isEmpty()));
                                break;
                            }
                        }
                    }
                }
                case TRIGGER -> {
                    for (InteractionHand hand : InteractionHand.values()) {
                        IWPlayerUtil.shotGun((ServerPlayer) packetContext.getPlayer(), hand, true, false);
                    }
                }
            }
        });
    }
}
