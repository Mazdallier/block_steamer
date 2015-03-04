package com.cterm2.block_steamer.block;

import net.minecraft.init.*;
import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.tileentity.*;
import net.minecraft.world.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.*;

import com.cterm2.block_steamer.*;
import com.cterm2.block_steamer.tile.*;
import com.cterm2.block_steamer.enums.*;

public class BlockCoalBoiler extends BlockContainer
{
	// coal boiler
	public BlockCoalBoiler()
	{
		super(Material.rock);
	
		this.setStepSound(soundTypeStone);
		this.setBlockName("coalBoiler");
		this.setUnlocalizedName("coalBoiler")
	}

	@Override
	public IIcon getIcon(int side, int meta)
	{
		if(side == 1)
		{
			return Blocks.iron_block.getIcon(side, meta);
		}
		return Blocks.furnace.getIcon(side, meta);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return new TileCoalBoiler();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z,
		EntityPlayer player, int side,
		float posX, float posY, float posZ)
	{
		if(world.isRemote) return true;

		TileEntity t = world.getTileEntity(x, y, z);
		if(t == null) return true;
		player.openGui(BlockSteamer.instance,
			EnumGuiIdentifier.CoalBoiler.ordinal(), world, x, y, z);
		return true;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z,
		Block block)
	{
		TileEntity tent = world.getTileEntity(x, y, z);
		if(tent != null)
		{
			// update connection
			((TileCoalBoiler)tent).updateTileConnection();
		}
		super.onNeighborBlockChange(world, x, y, z, block);
	}
}

