package com.cterm2.block_steamer.tile;

// TileEntity for Tank block

import net.minecraft.tileentity.*;
import net.minecraftforge.fluids.*;
import net.minecraft.entity.player.*;
import net.minecraftforge.fluids.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraftforge.common.util.*;
import net.minecraft.network.*;
import net.minecraft.network.play.server.*;
import net.minecraft.nbt.*;

public class TileTank extends TileEntity implements IFluidHandler
{
	private static final String KeyTank = "Tank";
	private static final int Capacity
		= FluidContainerRegistry.BUCKET_VOLUME * 16;

	protected FluidTank tank = new FluidTank(Capacity);

	// Data ReadWrite
	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);

		if(tag.hasKey(KeyTank))
		{
			this.tank.readFromNBT(tag.getCompoundTag(KeyTank));
		}
		else
		{
			this.tank = new FluidTank(Capacity);
		}
	}
	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);

		NBTTagCompound tag_tank = new NBTTagCompound();
		this.tank.writeToNBT(tag_tank);
		tag.setTag(KeyTank, tag_tank);
	}

	// Data Sync
	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound tag = new NBTTagCompound();
		this.writeToNBT(tag);
		return new S35PacketUpdateTileEntity(this.xCoord,
			this.yCoord, this.zCoord, 1, tag);
	}
	@Override
	public void onDataPacket(NetworkManager network,
		S35PacketUpdateTileEntity packet)
	{
		this.readFromNBT(packet.func_148857_g());
	}

	// export
	public IIcon getFluidStaticIcon()
	{
		if(this.tank.getFluid() == null ||
			this.tank.getFluid().getFluid() == null) return null;
		return this.tank.getFluid().getFluid().getStillIcon();
	}
	public IIcon getFluidFlowingIcon()
	{
		if(this.tank.getFluid() == null ||
			this.tank.getFluid().getFluid() == null) return null;
		return this.tank.getFluid().getFluid().getFlowingIcon();
	}
	public double getAmountPercent()
	{
		return (double)this.tank.getFluidAmount() / (double)Capacity;
	}

	// Item Activation
	private void outputTileInformation(EntityPlayer player)
	{
		String output_string = "";
		FluidStack stack = this.tank.getFluid();

		if(stack == null || stack.getFluid() == null)
		{
			output_string = "no fluids";
		}
		else
		{
			output_string = "contains " + stack.getFluid().getName()
				+ " (" + this.tank.getFluidAmount() + " of "
				+ this.tank.getCapacity() + " mB)";
		}

		player.addChatMessage(new ChatComponentText(output_string));
	}
	private void fillFromItem(EntityPlayer player)
	{
		InventoryPlayer inv = player.inventory;
		ItemStack container = inv.getCurrentItem();
		FluidStack fluid = FluidContainerRegistry.
			getFluidForFilledItem(container);

		int fillable = this.fill(ForgeDirection.UNKNOWN, fluid, false);
		if(fillable >= fluid.amount)
		{
			// fill into tank
			this.fill(ForgeDirection.UNKNOWN, fluid, true);

			if(!player.capabilities.isCreativeMode)
			{
				// consume item
				container.stackSize--;
				if(container.stackSize <= 0)
				{
					inv.setInventorySlotContents(inv.currentItem, null);
				}
				
				// return item
				ItemStack emptyItem = FluidContainerRegistry.
					drainFluidContainer(container);
				if(emptyItem != null)
				{
					if(!inv.addItemStackToInventory(emptyItem))
					{
						player.entityDropItem(emptyItem, 1);
					}
				}
				
				// update
				inv.markDirty();
			}

			// update
			this.markDirty();
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord,
				this.zCoord);
			return;
		}

		this.outputTileInformation(player);
		return;
	}
	private void drainToItem(EntityPlayer player)
	{
		InventoryPlayer inv = player.inventory;
		ItemStack container = inv.getCurrentItem();

		if(this.tank.getFluid() == null ||
			this.tank.getFluid().getFluid() == null)
		{
			this.outputTileInformation(player);
			return;
		}

		Fluid fluid = this.tank.getFluid().getFluid();
		FluidStack drainStack = new FluidStack(fluid,
			FluidContainerRegistry.BUCKET_VOLUME);
		FluidStack drainable = this.drain(ForgeDirection.UNKNOWN,
			drainStack, false);
		if(drainable != null && drainable.amount >= drainStack.amount)
		{
			// drain
			this.drain(ForgeDirection.UNKNOWN, drainStack, true);

			if(!player.capabilities.isCreativeMode)
			{
				// consume item
				container.stackSize--;
				if(container.stackSize <= 0)
				{
					inv.setInventorySlotContents(inv.currentItem, null);
				}

				// return item
				ItemStack filledItem = FluidContainerRegistry.
					fillFluidContainer(drainStack, container);
				if(filledItem != null)
				{
					if(!inv.addItemStackToInventory(filledItem))
					{
						player.entityDropItem(filledItem, 1);
					}
				}

				// update
				inv.markDirty();
			}

			// update
			this.markDirty();
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord,
				this.zCoord);
			return;
		}
		
		this.outputTileInformation(player);
		return;
	}
	public void handleActivate(EntityPlayer player)
	{
		if(player == null) return;

		ItemStack currentItem = player.inventory.getCurrentItem();
		if(currentItem == null)
		{
			// output information
			this.outputTileInformation(player);
			return;
		}
		
		// is filled fluid container?
		if(FluidContainerRegistry.isFilledContainer(currentItem))
		{
			this.fillFromItem(player);
			return;
		}
		else if(FluidContainerRegistry.isEmptyContainer(currentItem))
		{
			this.drainToItem(player);
			return;
		}

		this.outputTileInformation(player);
		return;
	}

	// IFluidHandler
	@Override
	public int fill(ForgeDirection from, FluidStack stack, boolean doFill)
	{
		return this.tank.fill(stack, doFill);
	}
	@Override
	public FluidStack drain(ForgeDirection from, FluidStack stack,
		boolean doDrain)
	{
		if(stack == null || !stack.isFluidEqual(this.tank.getFluid()))
		{
			return null;
		}
		return this.tank.drain(stack.amount, doDrain);
	}
	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain,
		boolean doDrain)
	{
		return this.tank.drain(maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return true;
	}
	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return true;
	}
	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return new FluidTankInfo[] { this.tank.getInfo() };
	}
}

