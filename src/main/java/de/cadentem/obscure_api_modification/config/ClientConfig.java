package de.cadentem.obscure_api_modification.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue HARVEST_LEVEL_ICON;
    public static final ForgeConfigSpec.BooleanValue ATTACK_RANGE_ICON;

    static {
        HARVEST_LEVEL_ICON = BUILDER.comment("Enable / disable the harvest level icon").define("harvest_level_icon", true);
        ATTACK_RANGE_ICON = BUILDER.comment("Enable / disable the attack range icon").define("attack_rage_icon", true);

        SPEC = BUILDER.build();
    }
}
