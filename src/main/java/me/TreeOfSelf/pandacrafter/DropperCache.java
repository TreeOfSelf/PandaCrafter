package me.TreeOfSelf.pandacrafter;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;

import java.util.List;

public interface DropperCache {
	CraftingRecipe eac_getRecipe();

	void eac_setRecipe(CraftingRecipe r);

	List<ItemStack> eac_getIngredients();

	void eac_setIngredients(List<ItemStack> l);

	void eac_clearCache();
}
