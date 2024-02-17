package it.hurts.sskirillss.ramcompat.items;

import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityTendonSegment;
import com.github.alexthe666.alexsmobs.entity.util.TendonWhipUtil;
import it.hurts.sskirillss.relics.client.tooltip.base.RelicStyleData;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;

public class TendonLumpItem extends RelicItem {
    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("tendon")
                                .maxLevel(10)
                                .active(CastType.TOGGLEABLE)
                                .stat(StatData.builder("distance")
                                        .initialValue(3D, 5D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.25D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatData.builder("cooldown")
                                        .initialValue(15D, 7.5D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, -0.075D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatData.builder("damage")
                                        .initialValue(3D, 5D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.075D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(new LevelingData(100, 10, 200))
                .style(RelicStyleData.builder()
                        .borders("#dc41ff", "#832698")
                        .build())
                .loot(LootData.builder()
                        .entry(LootCollections.ANTHROPOGENIC)
                        .build())
                .build();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);

        if (!canUseAbility(stack, "tendon") || !isAbilityTicking(stack, "tendon")
                || !(slotContext.entity() instanceof Player player) || player.tickCount % Math.round(getAbilityValue(stack, "tendon", "cooldown") * 20) != 0)
            return;

        Level level = player.level();

        if (level.isClientSide())
            return;

        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(getAbilityValue(stack, "tendon", "distance")))
                .stream().filter(entry -> entry.attackable() && entry.isAlive() && !entry.getUUID().equals(player.getUUID())
                        && entry.hasLineOfSight(player) && !EntityUtils.isAlliedTo(player, entry)).toList();

        if (targets.isEmpty())
            return;

        LivingEntity target = targets.get(level.getRandom().nextInt(targets.size()));

        EntityTendonSegment segment = AMEntityRegistry.TENDON_SEGMENT.get().create(level);

        segment.copyPosition(player);

        level.addFreshEntity(segment);

        segment.getPersistentData().putFloat("relics_damage", (float) getAbilityValue(stack, "tendon", "damage"));
        segment.setCreatorEntityUUID(player.getUUID());
        segment.setFromEntityID(player.getId());
        segment.setToEntityID(target.getId());
        segment.copyPosition(player);
        segment.setProgress(0F);

        TendonWhipUtil.setLastTendon(player, segment);

        addExperience(player, stack, 1);
    }
}