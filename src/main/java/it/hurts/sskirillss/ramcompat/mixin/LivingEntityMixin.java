package it.hurts.sskirillss.ramcompat.mixin;

import it.hurts.sskirillss.ramcompat.init.ItemRegistry;
import it.hurts.sskirillss.relics.items.relics.base.utils.AbilityUtils;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "canFreeze", at = @At("HEAD"), cancellable = true)
    public void canFreeze(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        ItemStack stack = EntityUtils.findEquippedCurio(entity, ItemRegistry.FROST_ROBE.get());

        if (!stack.isEmpty() && AbilityUtils.canUseAbility(stack, "warming"))
            cir.setReturnValue(false);
    }
}