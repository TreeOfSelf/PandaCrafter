package me.TreeOfSelf.pandacrafter;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.impl.event.lifecycle.LoadedChunksCache;

public class PandaCrafter implements ModInitializer
{
	private static final String MOD_ID = "panda-crafter";

	@Override
	public void onInitialize()
	{
		Config.read();

		registerPandaCrafter();

		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) ->
		{
			if (success)
			{
				//noinspection UnstableApiUsage
				server.getWorlds().forEach(
					w -> ((LoadedChunksCache)w).fabric_getLoadedChunks().forEach(
						c -> c.getBlockEntities().values().stream()
							.filter(DropperCache.class::isInstance)
							.map(DropperCache.class::cast)
							.forEach(DropperCache::eac_clearCache)));
			}
		});
	}

	private void registerPandaCrafter() {
		Identifier blockId = Identifier.of(MOD_ID, "panda-crafter");
		Identifier blockEntityId = Identifier.of(MOD_ID, "panda-crafter-block-entity");

		RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, blockId);

		Block.Settings blockSettings = Block.Settings.create()
			.registryKey(blockKey)
			.strength(3.5f, 3.5f);

		Block pandaCrafterBlock = Registry.register(
			Registries.BLOCK,
			blockId,
			new PandaCrafterBlock(blockSettings)
		);


		PandaCrafterBlockEntityType.PANDA_CRAFTER_BLOCK_ENTITY = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			blockEntityId,
			FabricBlockEntityTypeBuilder.create(PandaCrafterBlockEntity::new, pandaCrafterBlock).build()
		);

		PolymerBlockUtils.registerBlockEntity(PandaCrafterBlockEntityType.PANDA_CRAFTER_BLOCK_ENTITY);
	}
}
