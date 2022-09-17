package me.adneths.hunger_reworked.capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class PlayerStomachProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static Capability<PlayerStomach> PLAYER_STOMACH = CapabilityManager.get(new CapabilityToken<>(){});

    private PlayerStomach stomach = null;
    private final LazyOptional<PlayerStomach> opt = LazyOptional.of(this::createPlayerStomach);

    @Nonnull
    private PlayerStomach createPlayerStomach() {
        if (stomach == null) {
        	stomach = new PlayerStomach();
        }
        return stomach;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap == PLAYER_STOMACH) {
            return opt.cast();
        }
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return getCapability(cap);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerStomach().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
    	createPlayerStomach().loadNBTData(nbt);
    }
}
