package it.hurts.sskirillss.ramcompat.mixin;

import com.github.alexthe666.alexsmobs.entity.EntityFart;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityFart.class)
public class EntityFartMixin {
    @Inject(method = "onEntityHit", at = @At("HEAD"), cancellable = true, remap = false)
    public void onHitEntity(EntityHitResult hit, CallbackInfo ci) {
        EntityFart entity = (EntityFart) (Object) this;

        if (EntityUtils.isAlliedTo(entity.getShooter(), hit.getEntity()))
            ci.cancel();
    }
}