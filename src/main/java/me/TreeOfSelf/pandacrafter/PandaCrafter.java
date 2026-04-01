package me.TreeOfSelf.pandacrafter;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.impl.event.lifecycle.LoadedChunksCache;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class PandaCrafter implements ModInitializer {
	private static final String MOD_ID = "panda-crafter";

	@Override
	public void onInitialize() {
		Config.read();

		registerPandaCrafter();

		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> {
			if (success) {
				server.getAllLevels().forEach(
					w -> ((LoadedChunksCache) w).fabric_getLoadedChunks().forEach(
						c -> c.getBlockEntities().values().stream()
							.filter(DropperCache.class::isInstance)
							.map(DropperCache.class::cast)
							.forEach(DropperCache::eac_clearCache)));
			}
		});
	}

	private void registerPandaCrafter() {
		Identifier blockId = Identifier.fromNamespaceAndPath(MOD_ID, "panda-crafter");
		Identifier itemId = Identifier.fromNamespaceAndPath(MOD_ID, "panda-crafter");
		Identifier blockEntityId = Identifier.fromNamespaceAndPath(MOD_ID, "panda-crafter-block-entity");

		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, blockId);
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, itemId);

		BlockBehaviour.Properties blockSettings = BlockBehaviour.Properties.of()
			.setId(blockKey)
			.strength(3.5f, 3.5f);

		Block pandaCrafterBlock = Registry.register(
			BuiltInRegistries.BLOCK,
			blockId,
			new PandaCrafterBlock(blockSettings)
		);

		Item.Properties itemSettings = new Item.Properties()
			.useItemDescriptionPrefix()
			.setId(itemKey);

		Registry.register(
			BuiltInRegistries.ITEM,
			itemId,
			new PandaCrafterItem(pandaCrafterBlock, itemSettings)
		);

		PandaCrafterBlockEntityType.PANDA_CRAFTER_BLOCK_ENTITY = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			blockEntityId,
			FabricBlockEntityTypeBuilder.create(PandaCrafterBlockEntity::new, pandaCrafterBlock).build()
		);

		PolymerBlockUtils.registerBlockEntity(PandaCrafterBlockEntityType.PANDA_CRAFTER_BLOCK_ENTITY);
	}
}
