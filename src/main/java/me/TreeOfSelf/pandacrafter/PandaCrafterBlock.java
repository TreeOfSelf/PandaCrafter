package me.TreeOfSelf.pandacrafter;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.TreeOfSelf.pandacrafter.mixin.TransientCraftingContainerMixin;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Containers;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PandaCrafterBlock extends BaseEntityBlock implements PolymerBlock {
	public static final MapCodec<PandaCrafterBlock> CODEC = simpleCodec(PandaCrafterBlock::new);
	public static final EnumProperty<Direction> FACING = DispenserBlock.FACING;
	public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;

	public PandaCrafterBlock(BlockBehaviour.Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(FACING, Direction.NORTH)
			.setValue(TRIGGERED, false));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, TRIGGERED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return this.defaultBlockState().setValue(FACING, ctx.getNearestLookingDirection().getOpposite());
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PandaCrafterBlockEntity(pos, state);
	}

	@Override
	public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
		return Blocks.DROPPER.defaultBlockState().setValue(DispenserBlock.FACING, state.getValue(FACING));
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof PandaCrafterBlockEntity pandaCrafterBlockEntity && player instanceof ServerPlayer serverPlayer) {
			SimpleGui gui = new SimpleGui(MenuType.GENERIC_3x3, serverPlayer, false);
			gui.setTitle(Component.literal("Easy Crafter"));

			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					int index = i * 3 + j;
					gui.setSlot(index, new Slot(pandaCrafterBlockEntity, index, 62 + j * 18, 17 + i * 18));
				}
			}

			gui.open();
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		Containers.updateNeighboursAfterDestroy(state, level, pos);
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean notify) {
		super.onPlace(state, level, pos, oldState, notify);
		level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(state));
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block sourceBlock, @Nullable Orientation orientation, boolean notify) {
		super.neighborChanged(state, level, pos, sourceBlock, orientation, notify);
		if (level instanceof ServerLevel serverWorld) {
			boolean isPowered = level.hasNeighborSignal(pos);
			boolean wasTriggered = state.getValue(TRIGGERED);

			if (isPowered && !wasTriggered) {
				level.setBlock(pos, state.setValue(TRIGGERED, true), Block.UPDATE_CLIENTS);
				craftRecipe(serverWorld, state, pos);
			} else if (!isPowered && wasTriggered) {
				level.setBlock(pos, state.setValue(TRIGGERED, false), Block.UPDATE_CLIENTS);
			}
		}
	}

	private void craftRecipe(ServerLevel world, BlockState state, BlockPos pos) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (!(blockEntity instanceof PandaCrafterBlockEntity pandaCrafter)) {
			return;
		}

		List<ItemStack> ingredients = new ArrayList<>(9);
		TransientCraftingContainer craftingContainer = new TransientCraftingContainer(new StubScreenHandler(), 3, 3);

		for (int i = 0; i < 9; i++) {
			@SuppressWarnings("ConstantConditions")
			ItemStack stack = InventoryUtil.singleItemOf(pandaCrafter.getItem(i));
			addToMergedItemStackList(ingredients, stack);
			craftingContainer.setItem(i, stack);
		}

		Direction facing = state.getValue(FACING);
		Storage<ItemVariant> ingredientStorage = Config.enable3x3InventorySearching ?
			InventoryUtil.getMerged3x3Storage(world, pos.relative(facing.getOpposite()), facing) :
			ItemStorage.SIDED.find(world, pos.relative(facing.getOpposite()), facing);

		boolean patternMode = ingredientStorage != null;

		if (craftingContainer.isEmpty() || !patternMode || !InventoryUtil.tryTakeItems(ingredientStorage, ingredients, true)) {
			return;
		}

		CraftingRecipe recipe = ((DropperCache) pandaCrafter).eac_getRecipe();

		List<ItemStack> craftingInventoryItems = ((TransientCraftingContainerMixin) (Object) craftingContainer).getItemsInternal();

		CraftingInput craftInput = craftingContainer.asCraftInput();

		if (!InventoryUtil.itemStackListsEqual(((DropperCache) pandaCrafter).eac_getIngredients(), craftingInventoryItems)
			|| recipe != null && !recipe.matches(craftInput, world)) {
			RecipeHolder<CraftingRecipe> holder = world.recipeAccess().getRecipeFor(RecipeType.CRAFTING, craftInput, world).orElse(null);

			recipe = holder == null ? null : holder.value();

			((DropperCache) pandaCrafter).eac_setRecipe(recipe);
			((DropperCache) pandaCrafter).eac_setIngredients(craftingInventoryItems);
		}

		if (recipe != null) {
			List<ItemStack> craftingResults = new ArrayList<>();

			addToMergedItemStackList(craftingResults, recipe.assemble(craftInput));

			for (ItemStack remainingStack : recipe.getRemainingItems(craftInput)) {
				addToMergedItemStackList(craftingResults, remainingStack);
			}

			Container inventoryInFront = HopperBlockEntity.getContainerAt(world, pos.relative(facing));
			Storage<ItemVariant> storage = ItemStorage.SIDED.find(world, pos.relative(facing), facing.getOpposite());
			boolean hasCrafted = false;

			if (inventoryInFront != null) {
				if (InventoryUtil.tryPutItems(inventoryInFront, craftingResults, facing.getOpposite(), true)) {
					InventoryUtil.tryPutItems(inventoryInFront, craftingResults, facing.getOpposite(), false);
					hasCrafted = true;
				}
			} else if (storage != null) {
				if (InventoryUtil.tryPutItems(storage, craftingResults)) {
					hasCrafted = true;
				}
			} else {
				Vec3 spawnPos = pos.getCenter().add(0.7 * facing.getStepX(), 0.7 * facing.getStepY(), 0.7 * facing.getStepZ());
				for (ItemStack craftingResult : craftingResults) {
					DefaultDispenseItemBehavior.spawnItem(world, craftingResult, 6, facing, spawnPos);
				}

				world.levelEvent(1000, pos, 0);
				world.levelEvent(2000, pos, facing.get3DDataValue());

				hasCrafted = true;
			}

			if (hasCrafted) {
				InventoryUtil.tryTakeItems(ingredientStorage, ingredients, false);
			}
		}
	}

	private static void addToMergedItemStackList(List<ItemStack> stackList, ItemStack newStack) {
		if (newStack.isEmpty()) {
			return;
		}

		for (ItemStack stack : stackList) {
			if (InventoryUtil.itemsEqual(stack, newStack)) {
				stack.grow(newStack.getCount());
				return;
			}
		}

		stackList.add(newStack.copy());
	}
}
