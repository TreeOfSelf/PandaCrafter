package me.TreeOfSelf.pandacrafter.mixin;

import net.minecraft.block.*;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import me.TreeOfSelf.pandacrafter.CraftingDropper;

@Mixin(value = DropperBlock.class, priority = 1500)
public class DropperBlockMixin extends DispenserBlock {

    @Inject(method = "dispense", at = @At("HEAD"), cancellable = true)
    protected void eac_dispense(ServerWorld world, BlockState state, BlockPos pos, CallbackInfo ci) {
        CraftingDropper.dispense(world, state, pos, ci);
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        if (world instanceof ServerWorld serverWorld
                && CraftingDropper.hasTableNextToBlock(serverWorld, pos)
                && world.getBlockEntity(pos) instanceof DropperBlockEntity dropper) {
            int stackCount = 0;
            for (int i = 0; i < dropper.size(); i++) {
                if (!dropper.getStack(i).isEmpty()) {
                    stackCount++;
                }
            }
            return stackCount;
        }
        return super.getComparatorOutput(state, world, pos, direction);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, wireOrientation, notify);
        if (sourceBlock == Blocks.CRAFTING_TABLE || world.getBlockState(pos).getBlock() == Blocks.CRAFTING_TABLE) {
            world.updateComparators(pos, state.getBlock());
        }
    }


    @SuppressWarnings("ConstantConditions")
    public DropperBlockMixin()
    {
        super(null);
    }

}