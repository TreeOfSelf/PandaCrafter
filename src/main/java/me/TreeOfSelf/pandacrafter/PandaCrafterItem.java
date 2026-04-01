package me.TreeOfSelf.pandacrafter;

import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class PandaCrafterItem extends PolymerBlockItem {
	public PandaCrafterItem(Block block, Properties settings) {
		super(block, settings, Items.DROPPER);
	}
}
