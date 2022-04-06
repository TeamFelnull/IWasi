package dev.felnull.iwasi.data;

import dev.felnull.iwasi.gun.trans.GunTrans;
import dev.felnull.iwasi.gun.trans.GunTransRegistry;
import dev.felnull.iwasi.item.GunItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record GunTransData(int transId, int progress, int step, int updateId) {
    public GunTransData(GunTrans gunTrans, int progress, int step, int updateID) {
        this(GunTransRegistry.getId(gunTrans), progress, step, updateID);
    }

    public GunTransData() {
        this(-1, 0, 0, 0);
    }

    @NotNull
    public static GunTransData read(@NotNull FriendlyByteBuf buf) {
        return new GunTransData(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    public void write(@NotNull FriendlyByteBuf buf) {
        buf.writeInt(transId);
        buf.writeInt(progress);
        buf.writeInt(step);
        buf.writeInt(updateId);
    }

    @NotNull
    public GunTransData copy() {
        return new GunTransData(transId, progress, step, updateId);
    }

    @Nullable
    public GunTrans getGunTrans() {
        return GunTransRegistry.getById(transId);
    }


    public GunTransData tickNext(Player player, InteractionHand hand, ItemStack stack) {
        if (!(stack.getItem() instanceof GunItem gunItem)) return null;
        var gs = getGunTrans();
        if (gs == null) return null;
        if (player instanceof ServerPlayer serverPlayer)
            gs.tick(serverPlayer, hand, gunItem.getGun(), stack, progress(), step());
        int mp = gs.getProgress(gunItem.getGun(), step());
        GunTransData nd;
        if (mp - 1 <= progress()) {
            if (player instanceof ServerPlayer serverPlayer)
                gs.stepEnd(serverPlayer, hand, gunItem.getGun(), stack, step());
            if (gs.getStep() - 1 > step())
                nd = new GunTransData(gs, 0, step() + 1, 0);
            else
                nd = new GunTransData(null, 0, 0, 0);
        } else {
            nd = new GunTransData(gs, progress() + 1, step(), 0);
        }
        return nd;
    }

}
