package fr.bastoup.bperipherals.util.tiles;

import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.shared.Capabilities;
import fr.bastoup.bperipherals.util.blocks.BlockPeripheral;
import fr.bastoup.bperipherals.util.peripherals.BPeripheral;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public abstract class TilePeripheral extends TileOrientable {

    protected final Set<IComputerAccess> computers = new HashSet<>(1);
    private BPeripheral peripheral;
    private LazyOptional<BPeripheral> holderPeripheral;

    public TilePeripheral(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    protected void setPeripheral(BPeripheral peripheral) {
        this.peripheral = peripheral;
        this.holderPeripheral = LazyOptional.of(() -> peripheral);
    }

    public void addComputer(IComputerAccess computer) {
        synchronized (computers) {
            computers.add(computer);
            update();
        }
    }

    public void removeComputer(IComputerAccess computer) {
        synchronized (computers) {
            computers.remove(computer);
            update();
        }
    }

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        if (capability.equals(Capabilities.CAPABILITY_PERIPHERAL)) {
            return holderPeripheral.cast();
        }
        return super.getCapability(capability, facing);
    }

    public void update() {
        if (this.getLevel() == null)
            return;

        if (!this.getLevel().getBlockState(worldPosition).hasProperty(BlockPeripheral.SWITCHED_ON))
            return;

        if (computers.isEmpty()) {
            this.getLevel().setBlockAndUpdate(worldPosition, this.getBlockState().setValue(BlockPeripheral.SWITCHED_ON, false));
        } else {
            this.getLevel().setBlockAndUpdate(worldPosition, this.getBlockState().setValue(BlockPeripheral.SWITCHED_ON, true));
        }
    }
}
