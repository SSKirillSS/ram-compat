package it.hurts.sskirillss.ramcompat.items;

import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityFart;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import it.hurts.sskirillss.relics.client.tooltip.base.RelicStyleData;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.base.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.AbilityCastStage;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.AbilityCastType;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicAbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicAbilityEntry;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicAbilityStat;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicLevelingData;
import it.hurts.sskirillss.relics.items.relics.base.utils.AbilityUtils;
import it.hurts.sskirillss.relics.items.relics.base.utils.LevelingUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.NBTUtils;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.SlotContext;

public class StinkGlandItem extends RelicItem {
    private static final String TAG_DURATION = "duration";
    private static final String TAG_COOLDOWN = "cooldown";

    @Override
    public RelicData constructRelicData() {
        return RelicData.builder()
                .abilityData(RelicAbilityData.builder()
                        .ability("defense", RelicAbilityEntry.builder()
                                .maxLevel(10)
                                .stat("duration", RelicAbilityStat.builder()
                                        .initialValue(2D, 5D)
                                        .upgradeModifier(RelicAbilityStat.Operation.MULTIPLY_BASE, 0.1D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat("radius", RelicAbilityStat.builder()
                                        .initialValue(1.5D, 3D)
                                        .upgradeModifier(RelicAbilityStat.Operation.MULTIPLY_BASE, 0.1D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat("cooldown", RelicAbilityStat.builder()
                                        .initialValue(20D, 15D)
                                        .upgradeModifier(RelicAbilityStat.Operation.MULTIPLY_BASE, -0.05D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .ability("dash", RelicAbilityEntry.builder()
                                .maxLevel(10)
                                .active(AbilityCastType.INSTANTANEOUS)
                                .stat("power", RelicAbilityStat.builder()
                                        .initialValue(0.75D, 1.75D)
                                        .upgradeModifier(RelicAbilityStat.Operation.MULTIPLY_BASE, 0.075D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .stat("cooldown", RelicAbilityStat.builder()
                                        .initialValue(10D, 7D)
                                        .upgradeModifier(RelicAbilityStat.Operation.MULTIPLY_BASE, -0.075D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .levelingData(new RelicLevelingData(100, 10, 200))
                .styleData(RelicStyleData.builder()
                        .borders("#dc41ff", "#832698")
                        .build())
                .build();
    }

    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, AbilityCastType type, AbilityCastStage stage) {
        if (ability.equals("dash")) {
            Level level = player.getCommandSenderWorld();
            RandomSource random = level.getRandom();

            Vec3 angle = player.getLookAngle();

            double power = AbilityUtils.getAbilityValue(stack, "dash", "power");

            player.setDeltaMovement(angle.normalize().scale(power));
            player.startAutoSpinAttack((int) Math.ceil(power * 5));

            player.fallDistance = 0F;

            angle = angle.scale(-1);

            for (int i = 0; i < 5; i++) {
                EntityFart fart = new EntityFart(AMEntityRegistry.FART.get(), level);

                fart.setPos(player.position().add(0, player.getBbHeight() / 2F, 0));
                fart.shoot(angle.x(), angle.y(), angle.z(), 0.25F, 15F);
                fart.setShooter(player);

                level.addFreshEntity(fart);
            }

            level.playSound(null, player.blockPosition(), AMSoundRegistry.STINK_RAY.get(), SoundSource.MASTER, 1F, 0.9F + (MathUtils.randomFloat(random) * 0.2F));

            if (!level.isClientSide())
                LevelingUtils.addExperience(player, stack, 1);

            AbilityUtils.addAbilityCooldown(stack, "dash", (int) Math.round(AbilityUtils.getAbilityValue(stack, "dash", "cooldown") * 20));
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);

        if (!(slotContext.entity() instanceof Player player))
            return;

        Level level = player.getCommandSenderWorld();
        RandomSource random = level.getRandom();

        int duration = NBTUtils.getInt(stack, TAG_DURATION, 0);
        int cooldown = NBTUtils.getInt(stack, TAG_COOLDOWN, 0);

        if (cooldown > 0)
            return;

        double radius = AbilityUtils.getAbilityValue(stack, "defense", "radius");

        if (duration <= 0) {
            if (!level.isClientSide())
                if (player.getHealth() <= player.getMaxHealth() * 0.2F)
                    NBTUtils.setInt(stack, TAG_DURATION, (int) Math.ceil(AbilityUtils.getAbilityValue(stack, "defense", "duration") * 20));
        } else {
            for (Mob target : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(radius))) {
                if (target.getTarget() != null && target.getTarget().getUUID().equals(player.getUUID())) {
                    target.setLastHurtByMob(null);
                    target.setTarget(null);
                }

                target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));

                for (Mob mob : level.getEntitiesOfClass(Mob.class, target.getBoundingBox().inflate(16))) {
                    if (mob.getUUID().equals(target.getUUID()))
                        continue;

                    if (mob.getTarget() == null || mob.getTarget().getUUID().equals(player.getUUID())) {
                        mob.setLastHurtByMob(target);
                        mob.setTarget(target);
                    }
                }
            }

            if (!level.isClientSide() && duration % 20 == 0)
                LevelingUtils.addExperience(player, stack, 1);

            if (player.tickCount % 3 == 0)
                level.playSound(null, player.blockPosition(), AMSoundRegistry.SKUNK_SPRAY.get(), SoundSource.MASTER, 1F, 1F);

            for (int i = 0; i < (int) (Math.ceil(radius * 2F)); i++)
                level.addParticle(AMParticleRegistry.SMELLY.get(), player.getX(), player.getY() + (player.getBbHeight() / 2F), player.getZ(),
                        MathUtils.randomFloat(random) * (radius * 0.075F), -0.05F, MathUtils.randomFloat(random) * (radius * 0.075F));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);

        if (level.isClientSide())
            return;

        int duration = NBTUtils.getInt(stack, TAG_DURATION, 0);
        int cooldown = NBTUtils.getInt(stack, TAG_COOLDOWN, 0);

        if (duration > 0) {
            NBTUtils.setInt(stack, TAG_DURATION, --duration);

            if (duration <= 0)
                NBTUtils.setInt(stack, TAG_COOLDOWN, (int) Math.ceil(AbilityUtils.getAbilityValue(stack, "defense", "cooldown") * 20));
        }

        if (cooldown > 0)
            NBTUtils.setInt(stack, TAG_COOLDOWN, --cooldown);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }
}