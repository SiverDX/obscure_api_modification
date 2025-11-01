package de.cadentem.obscure_api_modification.data;

import de.cadentem.obscure_api_modification.OAM;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class OAMItemTags extends ItemTagsProvider {
    public static final TagKey<Item> FORCE_TOOLTIP = ItemTags.create(OAM.location("force_tooltip"));
    public static final TagKey<Item> BLACKLIST_TOOLTIP = ItemTags.create(OAM.location("blacklist_tooltip"));

    public OAMItemTags(final PackOutput output, final CompletableFuture<HolderLookup.Provider> lookup, @Nullable final ExistingFileHelper helper) {
        super(output, lookup, /* There are no block tags */ CompletableFuture.completedFuture(null), OAM.MODID, helper);
    }

    @Override
    protected void addTags(@NotNull final HolderLookup.Provider lookup) {
        tag(FORCE_TOOLTIP);
        tag(BLACKLIST_TOOLTIP);
    }
}
