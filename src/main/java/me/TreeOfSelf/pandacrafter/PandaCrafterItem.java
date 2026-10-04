package me.TreeOfSelf.pandacrafter;

import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class PandaCrafterItem extends PolymerBlockItem {
	public static final Component NAME = Component.literal("Easy Crafter");

	public PandaCrafterItem(Block block, Properties settings) {
		super(block, settings, Items.DROPPER);
	}

	// Clients don't have our lang file, so use a plain name (Polymer sends this one)
	@Override
	public Component getName(ItemStack stack) {
		return NAME;
	}
}
