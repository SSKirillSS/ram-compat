package it.hurts.sskirillss.ramcompat.items;

import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityIceShard;
import it.hurts.sskirillss.ramcompat.init.ItemRegistry;
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
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.SlotContext;

public class FrostRobeItem extends RelicItem {
    @Override
    public RelicData constructRelicData() {
        return RelicData.builder()
                .abilityData(RelicAbilityData.builder()
                        .ability("warming", RelicAbilityEntry.builder()
                                .maxLevel(0)
                                .build())
                        .ability("icicle", RelicAbilityEntry.builder()
                                .maxLevel(10)
                                .active(AbilityCastType.INSTANTANEOUS)
                                .stat("chance", RelicAbilityStat.builder()
                                        .initialValue(0.4D, 0.75D)
                                        .upgradeModifier(RelicAbilityStat.Operation.MULTIPLY_BASE, -0.035D)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .stat("amount", RelicAbilityStat.builder()
                                        .initialValue(3D, 7D)
                                        .upgradeModifier(RelicAbilityStat.Operation.MULTIPLY_BASE, 0.5D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat("cooldown", RelicAbilityStat.builder()
                                        .initialValue(7.5D, 5D)
                                        .upgradeModifier(RelicAbilityStat.Operation.MULTIPLY_BASE, -0.05D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .ability("freeze", RelicAbilityEntry.builder()
                                .requiredLevel(5)
                                .maxLevel(10)
                                .stat("chance", RelicAbilityStat.builder()
                                        .initialValue(0.05D, 0.15D)
                                        .upgradeModifier(RelicAbilityStat.Operation.MULTIPLY_BASE, 0.1D)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .stat("duration", RelicAbilityStat.builder()
                                        .initialValue(0.75D, 1D)
                                        .upgradeModifier(RelicAbilityStat.Operation.MULTIPLY_BASE, 0.075D)
                                        .formatValue(value -> MathUtils.round(value, 2))
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
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);

        if (!(slotContext.entity() instanceof Player player))
            return;

        if (AbilityUtils.canUseAbility(stack, "warming"))
            player.setTicksFrozen(0);
    }

    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, AbilityCastType type, AbilityCastStage stage) {
        if (!AbilityUtils.canUseAbility(stack, "icicle"))
            return;

        throwIcicles(player, stack);

        AbilityUtils.addAbilityCooldown(stack, "icicle", (int) Math.round(AbilityUtils.getAbilityValue(stack, "icicle", "cooldown") * 20));
    }

    private static void throwIcicles(Player player, ItemStack stack) {
        Level level = player.getCommandSenderWorld();
        RandomSource random = level.getRandom();

        boolean spawned = false;

        for (int i = 0; i < (int) (AbilityUtils.getAbilityValue(stack, "icicle", "amount")); i++) {
            if (i != 0 && random.nextDouble() > AbilityUtils.getAbilityValue(stack, "icicle", "chance"))
                continue;

            EntityIceShard shard = new EntityIceShard(AMEntityRegistry.ICE_SHARD.get(), level);

            shard.setShooter(player);
            shard.setPos(player.position().add(0, player.getBbHeight() / 2F, 0));
            shard.setDeltaMovement(MathUtils.randomFloat(random) * 0.35F, 0.1F + (random.nextFloat() * 0.2F), MathUtils.randomFloat(random) * 0.35F);

            level.addFreshEntity(shard);

            spawned = true;
        }

        if (spawned) {
            LevelingUtils.addExperience(player, stack, 1);

            level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.MASTER, 1F, 2F);
        }
    }

    @Override
    public boolean canWalkOnPowderedSnow(SlotContext slotContext, ItemStack stack) {
        return AbilityUtils.canUseAbility(stack, "warming");
    }

    @Mod.EventBusSubscriber
    public static class FrostRobeEvents {
        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            if (event.getEntity() instanceof Player player) {
                ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.FROST_ROBE.get());

                if (stack.isEmpty())
                    return;

                Level level = player.getCommandSenderWorld();

                if (level.isClientSide())
                    return;

                RandomSource random = level.getRandom();

                if (AbilityUtils.canUseAbility(stack, "freeze")) {
                    if (random.nextDouble() > AbilityUtils.getAbilityValue(stack, "freeze", "chance"))
                        return;

                    if (event.getSource().getEntity() instanceof LivingEntity source)
                        source.setTicksFrozen((int) (source.getTicksFrozen() + Math.ceil(AbilityUtils.getAbilityValue(stack, "freeze", "duration") * 20)));

                    throwIcicles(player, stack);
                }
            } else if (event.getSource().getDirectEntity() instanceof EntityIceShard shard) {
                if (!(shard.getOwner() instanceof Player player))
                    return;

                ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.FROST_ROBE.get());

                if (stack.isEmpty())
                    return;

                LevelingUtils.addExperience(player, stack, 1);
            }
        }
    }
}