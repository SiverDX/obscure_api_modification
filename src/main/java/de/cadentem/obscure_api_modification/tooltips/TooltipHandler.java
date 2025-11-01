package de.cadentem.obscure_api_modification.tooltips;

import com.google.common.collect.Multimap;
import com.obscuria.obscureapi.api.client.tooltips.Tooltip;
import com.obscuria.obscureapi.api.utils.Icons;
import com.obscuria.obscureapi.util.TextUtils;
import de.cadentem.obscure_api_modification.OAM;
import de.cadentem.obscure_api_modification.config.ClientConfig;
import de.cadentem.obscure_api_modification.data.OAMItemTags;
import de.cadentem.obscure_api_modification.mixin.client.TooltipBuilder$ModulesAccessor;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.logic.WeaponRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TooltipHandler {
    private static final DecimalFormat DECIMAL = new DecimalFormat("##.#");

    private static final String MAX_HEALTH = "max_health";
    private static final String ARMOR = "armor";
    private static final String ARMOR_TOUGHNESS = "armor_toughness";
    private static final String KNOCKBACK_RESISTANCE = "knockback_resistance";

    private static final String ATTACK_DAMAGE = "attack_damage";
    private static final String ATTACK_SPEED = "attack_speed";

    @SuppressWarnings("ConstantValue")
    public static void buildEquipmentIcons(final Player player, final List<Component> tooltips, final ItemStack stack, final boolean isCurio) {
        if (stack.is(OAMItemTags.BLACKLIST_TOOLTIP)) {
            return;
        }

        if (Utils.isValidEquipment(stack) || TooltipBuilder$ModulesAccessor.obscure_api_modification$hasTooltip(stack.getItem(), Tooltip.Type.ICONS_START) || TooltipBuilder$ModulesAccessor.obscure_api_modification$hasTooltip(stack.getItem(), Tooltip.Type.ICONS_END)) {
            putIcons(player, tooltips, stack, isCurio);
        }
    }

    public static void putFoodIcons(final List<Component> tooltips, final ItemStack stack) {
        String icons = "";

        Item item = stack.getItem();
        FoodProperties properties = item.getFoodProperties(stack, Minecraft.getInstance().player);

        if (properties == null) {
            return;
        }

        if (properties.getNutrition() > 0) {
            icons = icons + Icons.FOOD.get() + properties.getNutrition() + " ";
        }

        if (properties.getSaturationModifier() > 0) {
            icons = icons + Icons.FOOD_SATURATION.get() + DECIMAL.format(properties.getSaturationModifier() * 100.0F) + "% ";
        }

        if (!icons.isEmpty()) {
            tooltips.add(1, TextUtils.component(icons));
        }
    }

    private static void putIcons(final Player player, final List<Component> tooltips, final ItemStack stack, final boolean isCurio) {
        String icons = TooltipBuilder$ModulesAccessor.obscure_api_modification$getLine(stack, Tooltip.Type.ICONS_START);
        Map<String, Collection<AttributeModifier>> offensiveModifiers;

        if (isCurio) {
            //noinspection removal,deprecation,UnstableApiUsage -> ignore
            offensiveModifiers = CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).stream().findFirst().map(slot -> getOffensiveAttributes(CuriosApi.getCuriosHelper().getAttributeModifiers(new SlotContext(slot, player, 0, false, true), UUID.randomUUID(), stack))).orElse(null);
        } else {
            offensiveModifiers = getOffensiveAttributes(stack.getAttributeModifiers(EquipmentSlot.MAINHAND));
        }

        Collection<AttributeModifier> damageCollection = new ArrayList<>(offensiveModifiers.get(ATTACK_DAMAGE));
        Collection<AttributeModifier> attackSpeedCollection = new ArrayList<>(offensiveModifiers.get(ATTACK_SPEED));

        icons = icons + getHarvestIcon(stack);

        if (!damageCollection.isEmpty()) {
            icons = icons + getAttackDamageIcon(player, damageCollection, stack, isCurio) + getAttackSpeedIcon(player, attackSpeedCollection);
            icons = icons + getAttackRangeIcon(player, stack);
        }

        if (!isWeapon(stack, isCurio)) {
            // To keep the original behaviour, only show defensive attributes for non-weapons
            Map<String, Collection<AttributeModifier>> defensiveAttributes;

            if (isCurio) {
                //noinspection removal,deprecation,UnstableApiUsage -> ignore
                defensiveAttributes = CuriosApi.getCuriosHelper().getCurioTags(stack.getItem()).stream().findFirst().map(slot -> getDefensiveAttributes(CuriosApi.getCuriosHelper().getAttributeModifiers(new SlotContext(slot, player, 0, false, true), UUID.randomUUID(), stack))).orElse(null);
            } else {
                defensiveAttributes = getDefensiveAttributes(stack.getAttributeModifiers(LivingEntity.getEquipmentSlotForItem(stack)));
            }

            if (defensiveAttributes == null) {
                return;
            }

            icons = icons + getIcon(false, Icons.HEART.get(), defensiveAttributes.get(MAX_HEALTH), stack);
            icons = icons + getIcon(false, Icons.ARMOR.get(), defensiveAttributes.get(ARMOR), stack);
            icons = icons + getIcon(false, Icons.ARMOR_TOUGHNESS.get(), defensiveAttributes.get(ARMOR_TOUGHNESS), stack);
            icons = icons + getIcon(true, Icons.KNOCKBACK_RESISTANCE.get(), defensiveAttributes.get(KNOCKBACK_RESISTANCE), stack);
        }

        icons = icons + getDurabilityIcon(stack);
        icons = icons + TooltipBuilder$ModulesAccessor.obscure_api_modification$getLine(stack, Tooltip.Type.ICONS_END);

        if (!icons.isBlank()) {
            tooltips.add(1, TextUtils.component(icons));
        }
    }

    private static String getIcon(boolean isPercentage, final String icon, final Collection<AttributeModifier> modifiers, final ItemStack stack) {
        return getIcon(isPercentage, icon, modifiers, stack, 0);
    }

    private static String getIcon(boolean isPercentage, final String icon, final Collection<AttributeModifier> modifiers, final ItemStack stack, final double base) {
        if (modifiers != null && !modifiers.isEmpty()) {
            boolean forcePercentage = true;

            // If there are only multiplicative attribute modifiers, then it's probably meant to apply on a global scale
            for (AttributeModifier modifier : modifiers) {
                if (modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
                    forcePercentage = false;
                    break;
                }
            }

            double value;

            if (forcePercentage) {
                value = Utils.calculateAttributes(1, modifiers) /* Remove the fake base */ - 1;
                isPercentage = true;
            } else {
                value = Utils.calculateAttributes(base, modifiers);
            }

            if (icon.equals(Icons.DAMAGE.get())) {
                value += EnchantmentHelper.getDamageBonus(stack, MobType.UNDEFINED);
            }

            if (value != 0) {
                if (isPercentage) {
                    return icon + DECIMAL.format(value * 100).replace(".0", "") + "% ";
                } else {
                    return icon + DECIMAL.format(value).replace(".0", "") + " ";
                }
            }
        }

        return "";
    }

    private static String getAttackDamageIcon(final Player player, final Collection<AttributeModifier> modifiers, final ItemStack stack, final boolean isCurio) {
        if (modifiers != null && !modifiers.isEmpty()) {
            // Only add the player base attack damage to actual weapons (to match the minecraft tooltip)
            double base = isWeapon(stack, isCurio) ? player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) : 0;
            return getIcon(false, Icons.DAMAGE.get(), modifiers, stack, base);
        } else {
            return "";
        }
    }

    private static String getAttackRangeIcon(final Player player, final ItemStack stack) {
        if (!ClientConfig.ATTACK_RANGE_ICON.get()) {
            return "";
        }

        Item item = stack.getItem();

        if (item instanceof TieredItem || stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE).stream().anyMatch(modifier -> modifier.getAmount() > 0)) {
            double attackRange;

            if (OAM.IS_BETTER_COMBAT_LOADED) {
                WeaponAttributes attributes = WeaponRegistry.getAttributes(stack);
                attackRange = attributes != null ? attributes.attackRange() : Utils.getAttackRange(player, stack);
            } else {
                attackRange = Utils.getAttackRange(player, stack);
            }

            return Component.translatable("icon.attack_range").withStyle(TextUtils.ICONS).getString() + " " + attackRange + " ";
        } else {
            return "";
        }
    }

    private static String getHarvestIcon(final ItemStack stack) {
        if (!ClientConfig.HARVEST_LEVEL_ICON.get()) {
            return "";
        }

        Item item = stack.getItem();

        if (item instanceof TieredItem tieredItem) {
            if (!(stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.AXES))) {
                return "";
            }

            int harvestLevel = Mth.clamp(tieredItem.getTier().getLevel(), 0, 4);
            return Component.translatable("icon.harvest." + harvestLevel).withStyle(TextUtils.ICONS).getString() + " ";
        }

        return "";
    }

    private static String getAttackSpeedIcon(final Player player, final Collection<AttributeModifier> modifiers) {
        if (modifiers != null && !modifiers.isEmpty()) {
            double value = Utils.calculateAttributes(player.getAttributeBaseValue(Attributes.ATTACK_SPEED), modifiers);
            String icon;

            if (value <= 0.6) {
                icon = Icons.ATTACK_SPEED_VERY_SLOW.get();
            } else if (value <= 1) {
                icon = Icons.ATTACK_SPEED_SLOW.get();
            } else if (value <= 2) {
                icon = Icons.ATTACK_SPEED_MEDIUM.get();
            } else if (value <= 3) {
                icon = Icons.ATTACK_SPEED_FAST.get();
            } else {
                icon = Icons.ATTACK_SPEED_VERY_FAST.get();
            }

            return icon + " ";
        } else {
            return "";
        }
    }

    private static String getDurabilityIcon(final ItemStack stack) {
        if (stack.isDamageableItem()) {
            return Icons.DURABILITY.get() + (stack.getMaxDamage() - stack.getDamageValue()) + "§8/" + stack.getMaxDamage() + "§f ";
        } else {
            return "";
        }
    }

    private static Map<String, Collection<AttributeModifier>> getDefensiveAttributes(final Multimap<Attribute, AttributeModifier> attributeModifiers) {
        Map<String, Collection<AttributeModifier>> modifiers = new HashMap<>();

        Collection<AttributeModifier> armorModifiers = new ArrayList<>();
        Collection<AttributeModifier> armorToughnessModifiers = new ArrayList<>();
        Collection<AttributeModifier> knockbackResistanceModifiers = new ArrayList<>();
        Collection<AttributeModifier> maxHealthModifiers = new ArrayList<>();

        for (Attribute attribute : attributeModifiers.keySet()) {
            ResourceLocation key = ForgeRegistries.ATTRIBUTES.getKey(attribute);

            if (key == null) {
                continue;
            }

            if (Utils.ARMOR_ATTRIBUTES.contains(key.toString())) {
                armorModifiers.addAll(attributeModifiers.get(attribute));
            } else if (Utils.ARMOR_TOUGHNESS_ATTRIBUTES.contains(key.toString())) {
                armorToughnessModifiers.addAll(attributeModifiers.get(attribute));
            } else if (Utils.KNOCKBACK_RESISTANCE_ATTRIBUTES.contains(key.toString())) {
                knockbackResistanceModifiers.addAll(attributeModifiers.get(attribute));
            } else if (Utils.MAX_HEALTH_ATTRIBUTES.contains(key.toString())) {
                maxHealthModifiers.addAll(attributeModifiers.get(attribute));
            }
        }

        modifiers.put(MAX_HEALTH, maxHealthModifiers);
        modifiers.put(ARMOR, armorModifiers);
        modifiers.put(ARMOR_TOUGHNESS, armorToughnessModifiers);
        modifiers.put(KNOCKBACK_RESISTANCE, knockbackResistanceModifiers);

        return modifiers;
    }

    private static Map<String, Collection<AttributeModifier>> getOffensiveAttributes(final Multimap<Attribute, AttributeModifier> attributeModifiers) {
        Map<String, Collection<AttributeModifier>> modifiers = new HashMap<>();
        modifiers.put(ATTACK_DAMAGE, new ArrayList<>());
        modifiers.put(ATTACK_SPEED, new ArrayList<>());

        for (Attribute attribute : attributeModifiers.keySet()) {
            ResourceLocation key = ForgeRegistries.ATTRIBUTES.getKey(attribute);

            if (key == null) {
                continue;
            }

            if (Utils.DAMAGE_ATTRIBUTES.contains(key.toString())) {
                modifiers.get(ATTACK_DAMAGE).addAll(attributeModifiers.get(attribute));
            } else if (Utils.ATTACK_SPEED_ATTRIBUTES.contains(key.toString())) {
                modifiers.get(ATTACK_SPEED).addAll(attributeModifiers.get(attribute));
            }
        }

        return modifiers;
    }

    private static boolean isWeapon(final ItemStack stack, final boolean isCurio) {
        if (isCurio) {
            return false;
        }

        return LivingEntity.getEquipmentSlotForItem(stack) == EquipmentSlot.MAINHAND;
    }
}
