package me.TreeOfSelf.pandacrafter;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

public class PandaCrafterScreenHandler extends ScreenHandler {
	private final Inventory inventory;
	public static ScreenHandlerType<PandaCrafterScreenHandler> PANDA_CRAFTER_SCREEN_HANDLER_TYPE;

	public PandaCrafterScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
		super(PANDA_CRAFTER_SCREEN_HANDLER_TYPE, syncId);
		this.inventory = inventory;

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				this.addSlot(new Slot(inventory, i * 3 + j, 62 + j * 18, 17 + i * 18));
			}
		}

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 140 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 198));
		}
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int slot) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slotObj = this.slots.get(slot);

		if (slotObj.hasStack()) {
			ItemStack itemStack2 = slotObj.getStack();
			itemStack = itemStack2.copy();

			if (slot < 9) {
				if (!this.insertItem(itemStack2, 9, 45, true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.insertItem(itemStack2, 0, 9, false)) {
				return ItemStack.EMPTY;
			}

			if (itemStack2.isEmpty()) {
				slotObj.setStack(ItemStack.EMPTY);
			} else {
				slotObj.markDirty();
			}
		}

		return itemStack;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return this.inventory.canPlayerUse(player);
	}
}
