package it.hurts.sskirillss.ramcompat.mixin;

import com.github.alexthe666.alexsmobs.entity.EntityIceShard;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityIceShard.class)
public class EntityIceShardMixin {
    @Inject(method = "onEntityHit", at = @At("HEAD"), cancellable = true, remap = false)
    public void onHitEntity(EntityHitResult hit, CallbackInfo ci) {
        EntityIceShard entity = (EntityIceShard) (Object) this;

        if (EntityUtils.isAlliedTo(entity.getOwner(), hit.getEntity()))
            ci.cancel();
    }
}