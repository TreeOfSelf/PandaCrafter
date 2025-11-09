package me.TreeOfSelf.pandacrafter;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class PandaCrafterBlockEntity extends BlockEntity implements Inventory, DropperCache {
	private static final int INVENTORY_SIZE = 9;
	private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);

	private CraftingRecipe cachedRecipe;
	private List<ItemStack> cachedIngredients;

	public PandaCrafterBlockEntity(BlockPos pos, BlockState state) {
		super(PandaCrafterBlockEntityType.PANDA_CRAFTER_BLOCK_ENTITY, pos, state);
	}

	@Override
	public int size() {
		return INVENTORY_SIZE;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemStack : this.inventory) {
			if (!itemStack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getStack(int slot) {
		return this.inventory.get(slot);
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
		ItemStack itemStack = this.inventory.get(slot);
		if (itemStack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack removedStack = itemStack.split(amount);
		if (itemStack.isEmpty()) {
			this.inventory.set(slot, ItemStack.EMPTY);
		}
		this.markDirty();
		return removedStack;
	}

	@Override
	public ItemStack removeStack(int slot) {
		ItemStack itemStack = this.inventory.get(slot);
		if (itemStack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		this.inventory.set(slot, ItemStack.EMPTY);
		this.markDirty();
		return itemStack;
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		this.inventory.set(slot, stack);
		this.markDirty();
	}

	@Override
	public void clear() {
		for (int i = 0; i < INVENTORY_SIZE; i++) {
			this.inventory.set(i, ItemStack.EMPTY);
		}
		this.markDirty();
	}

	@Override
	public void markDirty() {
		if (this.world != null) {
			this.world.markDirty(this.pos);
		}
	}

	@Override
	public boolean canPlayerUse(net.minecraft.entity.player.PlayerEntity player) {
		return true;
	}

	@Override
	protected void writeData(WriteView view) {
		Inventories.writeData(view, this.inventory);
	}

	@Override
	protected void readData(ReadView view) {
		Inventories.readData(view, this.inventory);
	}


	@Override
	public CraftingRecipe eac_getRecipe() {
		return cachedRecipe;
	}

	@Override
	public void eac_setRecipe(CraftingRecipe recipe) {
		this.cachedRecipe = recipe;
	}

	@Override
	public List<ItemStack> eac_getIngredients() {
		return cachedIngredients;
	}

	@Override
	public void eac_setIngredients(List<ItemStack> ingredients) {
		this.cachedIngredients = ingredients;
	}

	@Override
	public void eac_clearCache() {
		this.cachedRecipe = null;
		this.cachedIngredients = null;
	}
}
