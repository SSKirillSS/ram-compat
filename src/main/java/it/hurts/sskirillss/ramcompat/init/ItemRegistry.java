package it.hurts.sskirillss.ramcompat.init;

import it.hurts.sskirillss.ramcompat.RAMCompat;
import it.hurts.sskirillss.ramcompat.items.FrostRobeItem;
import it.hurts.sskirillss.ramcompat.items.StinkGlandItem;
import it.hurts.sskirillss.ramcompat.items.TendonLumpItem;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RAMCompat.MODID);

    public static final RegistryObject<Item> TENDON_LUMP = ITEMS.register("tendon_lump", TendonLumpItem::new);
    public static final RegistryObject<Item> FROST_ROBE = ITEMS.register("frost_robe", FrostRobeItem::new);
    public static final RegistryObject<Item> STINK_GLAND = ITEMS.register("stink_gland", StinkGlandItem::new);

    public static void register() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}