package me.TreeOfSelf.pandacrafter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class StubScreenHandler extends AbstractContainerMenu {
	protected StubScreenHandler() {
		super(null, 0);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return false;
	}
}
