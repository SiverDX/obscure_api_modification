package de.cadentem.obscure_api_modification.mixin.client;

import com.obscuria.obscureapi.api.client.tooltips.Tooltip;
import com.obscuria.obscureapi.client.TooltipBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = TooltipBuilder.Modules.class, remap = false)
public interface TooltipBuilder$ModulesAccessor {
    @Invoker("getLine")
    static String obscure_api_modification$getLine(final ItemStack stack, final Tooltip.Type type) {
        throw new AssertionError();
    }

    @Invoker("hasTooltip")
    static boolean obscure_api_modification$hasTooltip(final Item item, final Tooltip.Type type) {
        throw new AssertionError();
    }
}
