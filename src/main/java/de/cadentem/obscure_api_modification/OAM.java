package de.cadentem.obscure_api_modification;

import de.cadentem.obscure_api_modification.config.ClientConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(OAM.MODID)
public class OAM {
    public static final String MODID = "obscure_api_modification";
    public static boolean IS_CURIOS_LOADED = ModList.get().isLoaded("curios");
    public static boolean IS_BETTER_COMBAT_LOADED =  ModList.get().isLoaded("bettercombat");

    public OAM() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }

    public static ResourceLocation location(final String path) {
        return new ResourceLocation(MODID, path);
    }

    /* TODO :: ideas
        - movement speed icon
        - experience icon
        - attack knockback icon
        - attack range modifier icon for non-weapons
    */
}
