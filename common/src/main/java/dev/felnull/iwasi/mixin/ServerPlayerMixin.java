package dev.felnull.iwasi.mixin;

import dev.felnull.iwasi.entity.IIWCashServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements IIWCashServerPlayer {
    private int lastSelected = -1;
    private ItemStack lastMainHandItem = ItemStack.EMPTY;
    private ItemStack lastOffHandItem = ItemStack.EMPTY;
    private boolean lastHold;

    @Override
    public int getLastSelected() {
        return lastSelected;
    }

    @Override
    public void setLastSelected(int num) {
        this.lastSelected = num;
    }

    @Override
    public ItemStack getLastHandItem(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? lastMainHandItem : lastOffHandItem;
    }

    @Override
    public void setLastHandItem(InteractionHand hand, ItemStack stack) {
        if (hand == InteractionHand.MAIN_HAND) {
            lastMainHandItem = stack;
        } else {
            lastOffHandItem = stack;
        }
    }

    @Override
    public boolean getLastContinuousHold() {
        return lastHold;
    }

    @Override
    public void setLastContinuousHold(boolean hold) {
        lastHold = hold;
    }
}