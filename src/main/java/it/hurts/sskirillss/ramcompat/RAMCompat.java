package it.hurts.sskirillss.ramcompat;

import it.hurts.sskirillss.ramcompat.init.ItemRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RAMCompat.MODID)
public class RAMCompat {
    public static final String MODID = "ramcompat";

    public RAMCompat() {
        MinecraftForge.EVENT_BUS.register(this);

        ItemRegistry.register();
    }
}