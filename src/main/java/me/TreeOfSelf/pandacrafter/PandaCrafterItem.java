package me.TreeOfSelf.pandacrafter;

import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import net.minecraft.block.Block;
import net.minecraft.item.Items;

public class PandaCrafterItem extends PolymerBlockItem {
	public PandaCrafterItem(Block block, Settings settings) {
		super(block, settings, Items.DROPPER);
	}
}