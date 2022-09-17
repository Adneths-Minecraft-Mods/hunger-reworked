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
	//List<Food> content;
	
	public StomachPacket(PlayerStomach stomach)
	{
		this.totalFood = stomach.totalFood;
		//content = stomach.content;
	}

	public StomachPacket(FriendlyByteBuf buf)
	{
		totalFood = buf.readInt();
		/*
		content = new ArrayList<Food>();
		int n = buf.readInt();
		for(int i = 0; i < n; i++)
		{
			content.add(new Food(buf.readInt(), buf.readFloat(), buf.readDouble(), Food.EMPTY_EFFECTS));
		}
		*/
	}

	public void toBytes(FriendlyByteBuf buf)
	{
		buf.writeInt(totalFood);
		/*
		buf.writeInt(content.size());
		for(Food food : content)
		{
			buf.writeInt(food.getFood());
			buf.writeFloat(food.getDigest());
			buf.writeDouble(food.getProgress());
		}
		*/
	}

	public boolean handle(Supplier<NetworkEvent.Context> supplier)
	{
		NetworkEvent.Context ctx = supplier.get();
		ctx.enqueueWork(() -> {
			PlayerStomach data = ClientSide.minecraft.player.getCapability(PlayerStomachProvider.PLAYER_STOMACH).orElse(null);
			if(data != null)
				data.totalFood = this.totalFood;
				//data.setContents(this.content);
		});
		return true;
	}
}
