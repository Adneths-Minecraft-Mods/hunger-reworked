package me.adneths.hunger_reworked.network;

import java.util.function.Supplier;

import me.adneths.hunger_reworked.capability.PlayerStomach;
import me.adneths.hunger_reworked.capability.PlayerStomachProvider;
import me.adneths.hunger_reworked.init.ClientSide;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class StomachPacket
{
	int totalFood;
	
	public StomachPacket(PlayerStomach stomach)
	{
		this.totalFood = stomach.totalFood;
	}

	public StomachPacket(FriendlyByteBuf buf)
	{
		totalFood = buf.readInt();
	}

	public void toBytes(FriendlyByteBuf buf)
	{
		buf.writeInt(totalFood);
	}

	public boolean handle(Supplier<NetworkEvent.Context> supplier)
	{
		NetworkEvent.Context ctx = supplier.get();
		ctx.enqueueWork(() -> {
			PlayerStomach data = ClientSide.minecraft.player.getCapability(PlayerStomachProvider.PLAYER_STOMACH).orElse(null);
			if(data != null)
				data.totalFood = this.totalFood;
		});
		return true;
	}
}
