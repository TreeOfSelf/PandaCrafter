package me.TreeOfSelf.pandacrafter.mixin;

import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import me.TreeOfSelf.pandacrafter.DropperCache;

import java.util.List;

@Mixin(value = DropperBlockEntity.class, priority = 1500)
public class DropperBlockEntityMixin extends DispenserBlockEntity implements DropperCache
{
    @Unique
    private CraftingRecipe cachedRecipe;

    @Unique
    private List<ItemStack> cachedIngredients;


    @Override
    public CraftingRecipe eac_getRecipe()
    {
        return this.cachedRecipe;
    }

    @Override
    public void eac_setRecipe(CraftingRecipe r)
    {
        this.cachedRecipe = r;
    }

    @Override
    public List<ItemStack> eac_getIngredients()
    {
        return this.cachedIngredients;
    }

    @Override
    public void eac_setIngredients(List<ItemStack> r)
    {
        this.cachedIngredients = r;
    }

    @Override
    public void eac_clearCache()
    {
        this.cachedRecipe = null;
        this.cachedIngredients = null;
    }

    @SuppressWarnings("ConstantConditions")
    public DropperBlockEntityMixin()
    {
        super(null, null);
    }
}
