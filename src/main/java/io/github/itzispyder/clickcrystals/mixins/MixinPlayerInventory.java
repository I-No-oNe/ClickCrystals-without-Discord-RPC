package io.github.itzispyder.clickcrystals.mixins;

import io.github.itzispyder.clickcrystals.events.events.client.InventoryAddItemEvent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.github.itzispyder.clickcrystals.ClickCrystals.system;

@Mixin(PlayerInventory.class)
public abstract class MixinPlayerInventory {

    @Inject(method = "addStack(ILnet/minecraft/item/ItemStack;)I", at = @At("HEAD"))
    public void addStack(int slot, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        final InventoryAddItemEvent event = new InventoryAddItemEvent(slot, stack);

        system.eventBus.pass(event);
    }
}
