package com.cterm2.block_steamer;

// Block Steamer mod

import cpw.mods.fml.common.*;
import cpw.mods.fml.common.Mod.*;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.relauncher.*;
import cpw.mods.fml.common.registry.*;
import cpw.mods.fml.common.network.*;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.init.Items;
import net.minecraft.creativetab.CreativeTabs;
import com.cterm2.block_steamer.block.*;
import com.cterm2.block_steamer.tile.*;
import com.cterm2.block_steamer.*;
import com.cterm2.block_steamer.client.*;
import com.cterm2.block_steamer.server.*;

@Mod(modid=BlockSteamer.ModIdentifier, name=BlockSteamer.ModName,
	version=BlockSteamer.ModVersion)
public class BlockSteamer
{
	public static final String ModIdentifier = "block_steamer";
	public static final String ModName = "Block Steamer";
	public static final String ModVersion = "1.0 rev2";
	public static final CreativeTabs tab =
		new CreativeTabs("tabBlockSteamer")
		{
			@Override
			@SideOnly(Side.CLIENT)
			public Item getTabIconItem()
			{
				return Items.wheat;
			}
		};

	@Instance(value=BlockSteamer.ModIdentifier)
	public static BlockSteamer instance;

	@SidedProxy(clientSide="com.cterm2.block_steamer.client.ClientProxy",
		serverSide="com.cterm2.block_steamer.server.ServerProxy")
	public static IProxy proxy;

	public Block blockCoalBoiler, blockTank;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		// Preinit

		this.blockCoalBoiler = new BlockCoalBoiler()
			.setCreativeTab(this.tab);
		this.blockTank = new BlockTank().setCreativeTab(this.tab);

		GameRegistry.registerBlock(this.blockCoalBoiler, "blockCoalBoiler");
		GameRegistry.registerBlock(this.blockTank, "blockTank");
	}
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		// init
		GameRegistry.registerTileEntity(TileCoalBoiler.class,
			"tileCoalBoiler");
		GameRegistry.registerTileEntity(TileTank.class, "tileTank");

		NetworkRegistry.INSTANCE
			.registerGuiHandler(this, new GuiHandler());

		proxy.registerRenderer();
	}
}

