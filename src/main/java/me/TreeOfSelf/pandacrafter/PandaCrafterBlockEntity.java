package me.TreeOfSelf.pandacrafter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class PandaCrafterBlockEntity extends BlockEntity implements Container, DropperCache {
	private static final int INVENTORY_SIZE = 9;
	private final NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

	private @Nullable CraftingRecipe cachedRecipe;
	private @Nullable List<ItemStack> cachedIngredients;

	public PandaCrafterBlockEntity(BlockPos pos, BlockState state) {
		super(PandaCrafterBlockEntityType.PANDA_CRAFTER_BLOCK_ENTITY, pos, state);
	}

	@Override
	public int getContainerSize() {
		return INVENTORY_SIZE;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemStack : this.items) {
			if (!itemStack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		return this.items.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack itemStack = ContainerHelper.removeItem(this.items, slot, amount);
		if (!itemStack.isEmpty()) {
			this.setChanged();
		}
		return itemStack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(this.items, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		this.items.set(slot, stack);
		this.setChanged();
	}

	@Override
	public void clearContent() {
		this.items.clear();
		this.setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, this.items);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		for (int i = 0; i < INVENTORY_SIZE; i++) {
			this.items.set(i, ItemStack.EMPTY);
		}
		ContainerHelper.loadAllItems(input, this.items);
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
