package fr.bastoup.bperipherals.peripherals.femeter;

import fr.bastoup.bperipherals.util.BlockFaces;
import fr.bastoup.bperipherals.util.Config;
import fr.bastoup.bperipherals.util.Util;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class EnergyFEMeterOut implements IEnergyStorage, INBTSerializable<CompoundNBT> {

	private int energyStored;
	private int transferRate;
	private final TileFEMeter tile;
	private boolean updated = false;

	public EnergyFEMeterOut(TileFEMeter tileRFMeter) {
		energyStored = 0;
		transferRate = Config.MAX_FE_METER_TRANSFER_RATE;
		tile = tileRFMeter;
	}

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		return 0;
	}

	@Override
	public int getEnergyStored() {
		return energyStored;
	}

	@Override
	public int getMaxEnergyStored() {
		return Config.FE_METER_INTERNAL_BUFFER_SIZE;
    }

	@Override
	public boolean canExtract() {
		return false;
	}

	@Override
	public boolean canReceive() {
		return false;
	}

	public int getTransferRate() {
		return transferRate;
	}

	public void setTransferRate(int transferRate) {
		int actualTransferRate;
		if (transferRate > Config.MAX_FE_METER_TRANSFER_RATE) {
			actualTransferRate = Config.MAX_FE_METER_TRANSFER_RATE;
		} else actualTransferRate = Math.max(transferRate, 0);

		this.transferRate = actualTransferRate;
		updated = true;
		tile.setChanged();
	}
	
	public int transferFromIn(int maxTransfer, boolean simulate) {
		int actualTransfer = Math.min(maxTransfer, transferRate);

		if (maxTransfer + energyStored >= Config.FE_METER_INTERNAL_BUFFER_SIZE) {
			actualTransfer = Config.FE_METER_INTERNAL_BUFFER_SIZE - energyStored;
		}

		if (!simulate) {
			energyStored += actualTransfer;
			updated = true;
			tile.setChanged();
		}

		return actualTransfer;
	}
	
	public int getExtractableEnergy() {
		return Math.min(energyStored, transferRate);
	}
	
	public void sendEnergy() {
		Direction rightFace = tile.getFacingOfFace(BlockFaces.RIGHT);
		BlockPos pos = Util.getNextPos(tile.getBlockPos(), rightFace);
		World world = tile.getLevel();
		if (world == null)
			return;
		TileEntity targetTile = world.getBlockEntity(pos);
		if (targetTile != null && ((ICapabilityProvider) targetTile).getCapability(CapabilityEnergy.ENERGY, Util.getOppositeFacing(rightFace)).isPresent()) {
			IEnergyStorage cap = ((ICapabilityProvider) targetTile).getCapability(CapabilityEnergy.ENERGY, Util.getOppositeFacing(rightFace)).orElse(null);
			if (cap.canReceive()) {
				int extracted = cap.receiveEnergy(getExtractableEnergy(), false);
				energyStored -= extracted;
				updated = true;
				tile.setChanged();
			}
		}
	}
	
	public int getMaxTransferRate() {
		return Config.MAX_FE_METER_TRANSFER_RATE;
	}
	
	public boolean resetUpdated() {
		if(updated) {
			updated = false;
			return true;
		}
		return false;
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt("transferRate", transferRate);
		nbt.putInt("energyStored", energyStored);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		transferRate = nbt.getInt("transferRate");
		energyStored = nbt.getInt("energyStored");
	}
}
