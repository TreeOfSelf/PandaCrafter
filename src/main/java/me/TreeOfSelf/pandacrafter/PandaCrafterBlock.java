package me.TreeOfSelf.pandacrafter;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.TreeOfSelf.pandacrafter.mixin.CraftingInventoryMixin;
import net.minecraft.screen.slot.Slot;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.*;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;

public class PandaCrafterBlock extends Block implements PolymerBlock, BlockEntityProvider {
	public static final EnumProperty<Direction> FACING = FacingBlock.FACING;
	public static final BooleanProperty TRIGGERED = Properties.TRIGGERED;

	public PandaCrafterBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
				.with(FACING, Direction.NORTH)
				.with(TRIGGERED, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, TRIGGERED);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite());
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new PandaCrafterBlockEntity(pos, state);
	}

	@Override
	public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
		return Blocks.DROPPER.getDefaultState().with(DispenserBlock.FACING, state.get(FACING));
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world.isClient()) {
			return ActionResult.SUCCESS;
		}
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof PandaCrafterBlockEntity pandaCrafterBlockEntity && player instanceof ServerPlayerEntity serverPlayer) {
			SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_3X3, serverPlayer, false);
			gui.setTitle(Text.literal("PandaCrafter"));

			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					int index = i * 3 + j;
					gui.setSlotRedirect(index, new Slot(pandaCrafterBlockEntity, index, 62 + j * 18, 17 + i * 18));
				}
			}

			gui.open();
		}
		return ActionResult.SUCCESS;
	}

	@Override
	public void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof PandaCrafterBlockEntity pandaCrafterBlockEntity) {
			for (int i = 0; i < pandaCrafterBlockEntity.size(); ++i) {
				ItemStack itemStack = pandaCrafterBlockEntity.getStack(i);
				if (!itemStack.isEmpty()) {
					Block.dropStack(world, pos, itemStack);
				}
			}
		}
		super.onStateReplaced(state, world, pos, moved);
	}

	@Override
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		super.onBlockAdded(state, world, pos, oldState, notify);
		world.emitGameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Emitter.of(state));
	}

	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
		super.neighborUpdate(state, world, pos, sourceBlock, wireOrientation, notify);
		if (world instanceof ServerWorld serverWorld) {
			boolean isPowered = world.isReceivingRedstonePower(pos);
			boolean wasTriggered = state.get(TRIGGERED);

			if (isPowered && !wasTriggered) {
				world.setBlockState(pos, state.with(TRIGGERED, true), Block.NOTIFY_LISTENERS);
				craftRecipe(serverWorld, state, pos);
			} else if (!isPowered && wasTriggered) {
				world.setBlockState(pos, state.with(TRIGGERED, false), Block.NOTIFY_LISTENERS);
			}
		}
	}

	private void craftRecipe(ServerWorld world, BlockState state, BlockPos pos) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (!(blockEntity instanceof PandaCrafterBlockEntity pandaCrafter)) {
			return;
		}

		List<ItemStack> ingredients = new ArrayList<>(9);
		CraftingInventory craftingInventory = new CraftingInventory(new StubScreenHandler(), 3, 3);

		for (int i = 0; i < 9; i++) {
			@SuppressWarnings("ConstantConditions")
			ItemStack stack = InventoryUtil.singleItemOf(pandaCrafter.getStack(i));
			addToMergedItemStackList(ingredients, stack);
			craftingInventory.setStack(i, stack);
		}

		Direction facing = state.get(FACING);
		Storage<ItemVariant> ingredientStorage = Config.enable3x3InventorySearching ?
				InventoryUtil.getMerged3x3Storage(world, pos.offset(facing.getOpposite()), facing) :
				ItemStorage.SIDED.find(world, pos.offset(facing.getOpposite()), facing);

		boolean patternMode = ingredientStorage != null;

		if (craftingInventory.isEmpty() || !patternMode || !InventoryUtil.tryTakeItems(ingredientStorage, ingredients, true)) {
			return;
		}

		CraftingRecipe recipe = ((DropperCache) pandaCrafter).eac_getRecipe();

		List<ItemStack> craftingInventoryItems = ((CraftingInventoryMixin) craftingInventory).getStacks();

		if (!InventoryUtil.itemStackListsEqual(((DropperCache) pandaCrafter).eac_getIngredients(), craftingInventoryItems)
				|| recipe != null && !recipe.matches(craftingInventory.createRecipeInput(), world)) {
			RecipeEntry<CraftingRecipe> entry = world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, craftingInventory.createRecipeInput(), world).orElse(null);

			recipe = entry == null ? null : entry.value();

			((DropperCache) pandaCrafter).eac_setRecipe(recipe);
			((DropperCache) pandaCrafter).eac_setIngredients(craftingInventoryItems);
		}

		if (recipe != null) {
			List<ItemStack> craftingResults = new ArrayList<>();

			addToMergedItemStackList(craftingResults, recipe.craft(craftingInventory.createRecipeInput(), world.getRegistryManager()));

			for (ItemStack remainingStack : recipe.getRecipeRemainders(craftingInventory.createRecipeInput())) {
				addToMergedItemStackList(craftingResults, remainingStack);
			}

			Inventory inventoryInFront = HopperBlockEntity.getInventoryAt(world, pos.offset(facing));
			Storage<ItemVariant> storage = ItemStorage.SIDED.find(world, pos.offset(facing), facing.getOpposite());
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
				for (ItemStack craftingResult : craftingResults) {
					ItemDispenserBehavior.spawnItem(world, craftingResult, 6, facing, pos.toCenterPos().add(0.7 * facing.getOffsetX(), 0.7 * facing.getOffsetY(), 0.7 * facing.getOffsetZ()));
				}

				world.syncWorldEvent(1000, pos, 0);
				world.syncWorldEvent(2000, pos, facing.getIndex());

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
				stack.setCount(stack.getCount() + newStack.getCount());
				return;
			}
		}

		stackList.add(newStack.copy());
	}
}