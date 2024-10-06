package de.cadentem.obscure_api_modification.mixin.client;

import com.obscuria.obscureapi.util.TextUtils;
import de.cadentem.obscure_api_modification.OAM;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TextUtils.class)
public abstract class TextUtilsMixin {
    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Style;withFont(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/network/chat/Style;"))
    private static ResourceLocation obscure_api_modification$useCustomFont(final ResourceLocation pFontId) {
        return OAM.location("icons");
    }
}
