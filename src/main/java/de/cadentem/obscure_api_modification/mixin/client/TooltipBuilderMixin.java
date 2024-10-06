package de.cadentem.obscure_api_modification.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.obscuria.obscureapi.ObscureAPIConfig;
import com.obscuria.obscureapi.client.TooltipBuilder;
import de.cadentem.obscure_api_modification.tooltips.TooltipHandler;
import de.cadentem.obscure_api_modification.tooltips.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = TooltipBuilder.class, remap = false)
public abstract class TooltipBuilderMixin {
    @Redirect(method = "buildTooltip", at = @At(value = "INVOKE", target = "Lcom/obscuria/obscureapi/client/TooltipBuilder$Modules;buildIcons(Lnet/minecraft/world/item/ItemStack;Ljava/util/List;)V"))
    private static void obscure_api_modification$buildIcons(ItemStack stack, List<Component> tooltips, @Local(argsOnly = true) final ItemTooltipEvent event) {
        if (ObscureAPIConfig.Client.foodIcons.get() && stack.getItem().isEdible()) {
            TooltipHandler.putFoodIcons(tooltips, stack);
        }

        if (ObscureAPIConfig.Client.equipmentIcons.get()) {
            TooltipHandler.buildEquipmentIcons(event.getEntity(), tooltips, stack, Utils.isCurios(stack));
        }
    }
}
