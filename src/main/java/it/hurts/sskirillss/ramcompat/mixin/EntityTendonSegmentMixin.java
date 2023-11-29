package it.hurts.sskirillss.ramcompat.mixin;

import com.github.alexthe666.alexsmobs.entity.EntityTendonSegment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityTendonSegment.class)
public class EntityTendonSegmentMixin {
    @Inject(method = "getBaseDamage", at = @At("HEAD"), cancellable = true, remap = false)
    public void getBaseDamage(CallbackInfoReturnable<Float> cir) {
        EntityTendonSegment entity = (EntityTendonSegment) (Object) this;

        if (entity.getPersistentData().contains("relics_damage"))
            cir.setReturnValue(entity.getPersistentData().getFloat("relics_damage"));
    }
}