package it.hurts.sskirillss.ramcompat.items;

import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityTendonSegment;
import com.github.alexthe666.alexsmobs.entity.util.TendonWhipUtil;
import it.hurts.sskirillss.relics.client.tooltip.base.RelicStyleData;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.base.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.AbilityCastType;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicAbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicAbilityEntry;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicAbilityStat;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicLevelingData;
import it.hurts.sskirillss.relics.items.relics.base.utils.AbilityUtils;
import it.hurts.sskirillss.relics.items.relics.base.utils.LevelingUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;

public class TendonLumpItem extends RelicItem {
    @Override
    public RelicData constructRelicData() {
        return RelicData.builder()
                .abilityData(RelicAbilityData.builder()
                        .ability("tendon", RelicAbilityEntry.builder()
                                .maxLevel(10)
                                .active(AbilityCastType.TOGGLEABLE)
                                .stat("distance", RelicAbilityStat.builder()
                                        .initialValue(3D, 5D)
                                        .upgradeModifier(RelicAbilityStat.Operation.MULTIPLY_BASE, 0.25D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat("cooldown", RelicAbilityStat.builder()
                                        .initialValue(15D, 7.5D)
                                        .upgradeModifier(RelicAbilityStat.Operation.MULTIPLY_BASE, -0.075D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat("damage", RelicAbilityStat.builder()
                                        .initialValue(3D, 5D)
                                        .upgradeModifier(RelicAbilityStat.Operation.MULTIPLY_BASE, 0.075D)
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
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);

        if (!AbilityUtils.canUseAbility(stack, "tendon") || !AbilityUtils.isAbilityTicking(stack, "tendon")
                || !(slotContext.entity() instanceof Player player) || player.tickCount % Math.round(AbilityUtils.getAbilityValue(stack, "tendon", "cooldown") * 20) != 0)
            return;

        Level level = player.level();

        if (level.isClientSide())
            return;

        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(AbilityUtils.getAbilityValue(stack, "tendon", "distance")))
                .stream().filter(entry -> entry.attackable() && entry.isAlive() && !entry.getUUID().equals(player.getUUID()) && entry.hasLineOfSight(player)).toList();

        if (targets.isEmpty())
            return;

        LivingEntity target = targets.get(level.getRandom().nextInt(targets.size()));

        EntityTendonSegment segment = AMEntityRegistry.TENDON_SEGMENT.get().create(level);

        segment.copyPosition(player);

        level.addFreshEntity(segment);

        segment.getPersistentData().putFloat("relics_damage", (float) AbilityUtils.getAbilityValue(stack, "tendon", "damage"));
        segment.setCreatorEntityUUID(player.getUUID());
        segment.setFromEntityID(player.getId());
        segment.setToEntityID(target.getId());
        segment.copyPosition(player);
        segment.setProgress(0F);

        TendonWhipUtil.setLastTendon(player, segment);

        LevelingUtils.addExperience(player, stack, 1);
    }
}