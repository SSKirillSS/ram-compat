package it.hurts.sskirillss.ramcompat.items;

import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityIceShard;
import it.hurts.sskirillss.ramcompat.init.ItemRegistry;
import it.hurts.sskirillss.relics.client.tooltip.base.RelicStyleData;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastStage;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilitiesData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootCollections;
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
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("warming")
                                .maxLevel(0)
                                .build())
                        .ability(AbilityData.builder("icicle")
                                .maxLevel(10)
                                .active(CastType.INSTANTANEOUS)
                                .stat(StatData.builder("chance")
                                        .initialValue(0.4D, 0.75D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, -0.035D)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .stat(StatData.builder("amount")
                                        .initialValue(3D, 7D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.5D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatData.builder("cooldown")
                                        .initialValue(7.5D, 5D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, -0.05D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .ability(AbilityData.builder("freeze")
                                .requiredLevel(5)
                                .maxLevel(10)
                                .stat(StatData.builder("chance")
                                        .initialValue(0.05D, 0.15D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.1D)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .stat(StatData.builder("duration")
                                        .initialValue(0.75D, 1D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.075D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .build())
                        .build())
                .leveling(new LevelingData(100, 10, 200))
                .style(RelicStyleData.builder()
                        .borders("#dc41ff", "#832698")
                        .build())
                .loot(LootData.builder()
                        .entry(LootCollections.COLD)
                        .build())
                .build();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);

        if (!(slotContext.entity() instanceof Player player))
            return;

        if (canUseAbility(stack, "warming"))
            player.setTicksFrozen(0);
    }

    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {
        if (!canUseAbility(stack, "icicle"))
            return;

        throwIcicles(player, stack);

        addAbilityCooldown(stack, "icicle", (int) Math.round(getAbilityValue(stack, "icicle", "cooldown") * 20));
    }

    private void throwIcicles(Player player, ItemStack stack) {
        Level level = player.getCommandSenderWorld();
        RandomSource random = level.getRandom();

        boolean spawned = false;

        for (int i = 0; i < (int) (getAbilityValue(stack, "icicle", "amount")); i++) {
            if (i != 0 && random.nextDouble() > getAbilityValue(stack, "icicle", "chance"))
                continue;

            EntityIceShard shard = new EntityIceShard(AMEntityRegistry.ICE_SHARD.get(), level);

            shard.setShooter(player);
            shard.setPos(player.position().add(0, player.getBbHeight() / 2F, 0));
            shard.setDeltaMovement(MathUtils.randomFloat(random) * 0.35F, 0.1F + (random.nextFloat() * 0.2F), MathUtils.randomFloat(random) * 0.35F);

            level.addFreshEntity(shard);

            spawned = true;
        }

        if (spawned) {
            addExperience(player, stack, 1);

            level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.MASTER, 1F, 2F);
        }
    }

    @Override
    public boolean canWalkOnPowderedSnow(SlotContext slotContext, ItemStack stack) {
        return canUseAbility(stack, "warming");
    }

    @Mod.EventBusSubscriber
    public static class FrostRobeEvents {
        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            if (event.getEntity() instanceof Player player) {
                ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.FROST_ROBE.get());

                if (!(stack.getItem() instanceof FrostRobeItem relic))
                    return;

                Level level = player.getCommandSenderWorld();

                if (level.isClientSide())
                    return;

                RandomSource random = level.getRandom();

                if (relic.canUseAbility(stack, "freeze")) {
                    if (random.nextDouble() > relic.getAbilityValue(stack, "freeze", "chance"))
                        return;

                    if (event.getSource().getEntity() instanceof LivingEntity source)
                        source.setTicksFrozen((int) (source.getTicksFrozen() + Math.ceil(relic.getAbilityValue(stack, "freeze", "duration") * 20)));

                    relic.throwIcicles(player, stack);
                }
            } else if (event.getSource().getDirectEntity() instanceof EntityIceShard shard) {
                if (!(shard.getOwner() instanceof Player player))
                    return;

                ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.FROST_ROBE.get());

                if (!(stack.getItem() instanceof FrostRobeItem relic))
                    return;

                relic.addExperience(player, stack, 1);
            }
        }
    }
}