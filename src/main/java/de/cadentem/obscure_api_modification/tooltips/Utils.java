package de.cadentem.obscure_api_modification.tooltips;

import de.cadentem.obscure_api_modification.OAM;
import de.cadentem.obscure_api_modification.config.ClientConfig;
import de.cadentem.obscure_api_modification.data.OAMItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeMod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Collection;
import java.util.List;

public class Utils {
    public static final List<String> DAMAGE_ATTRIBUTES = List.of(
            "minecraft:generic.attack_damage",
            // FIXME :: unsure but does this cause the attribute value calculation to be wrong?
            "attributeslib:fire_damage",
            "attributeslib:cold_damage"
    );

    public static final List<String> ATTACK_SPEED_ATTRIBUTES = List.of(
            "minecraft:generic.attack_speed"
    );

    public static final List<String> ARMOR_ATTRIBUTES = List.of(
            "minecraft:generic.armor"
    );

    public static final List<String> ARMOR_TOUGHNESS_ATTRIBUTES = List.of(
            "minecraft:generic.armor_toughness"
    );

    public static final List<String> KNOCKBACK_RESISTANCE_ATTRIBUTES = List.of(
            "minecraft:generic.knockback_resistance"
    );

    public static final List<String> MAX_HEALTH_ATTRIBUTES = List.of(
            "minecraft:generic.max_health"
    );

    public static double calculateAttributes(final double base, final Collection<AttributeModifier> modifiers) {
        double value = base;

        if (!modifiers.isEmpty()) {
            for (AttributeModifier modifier : modifiers) {
                if (modifier.getOperation() == AttributeModifier.Operation.ADDITION)
                    value += modifier.getAmount();
            }

            double rawValue = value;

            for (AttributeModifier modifier : modifiers) {
                if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE)
                    value += rawValue * modifier.getAmount();
            }

            for (AttributeModifier modifier : modifiers) {
                if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL)
                    value += value * modifier.getAmount();
            }
        }

        return value;
    }

    public static boolean isValidEquipment(final ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof ArmorItem || item instanceof ShieldItem || item instanceof ProjectileWeaponItem || item instanceof TieredItem) {
            return true;
        }

        if (stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).stream().anyMatch(modifier -> modifier.getAmount() > 0)) {
            return true;
        }

        if (ClientConfig.ENABLE_CURIOS_ICONS.get() && isCurios(stack)) {
            return true;
        }

        return stack.is(OAMItemTags.FORCE_TOOLTIP);
    }

    public static double getAttackRange(final Player player, final ItemStack stack) {
        return calculateAttributes(player.getAttributeBaseValue(ForgeMod.ENTITY_REACH.get()), stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(ForgeMod.ENTITY_REACH.get()));
    }

    public static boolean isCurios(final ItemStack stack) {
        if (!OAM.IS_CURIOS_LOADED) {
            return false;
        }

        return CuriosApi.getCurio(stack).isPresent();
    }
}
