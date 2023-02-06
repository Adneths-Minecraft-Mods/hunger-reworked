package me.adneths.hunger_reworked.integration.diet;

import java.util.Set;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;

public abstract class ProxyDiet
{
	public LazyOptional<ProxyIDietTracker> get(Player player)
	{
		return LazyOptional.empty();
	}
	
	public Set<ProxyIDietGroup> getGroups()
	{
		return Set.of();
	}
}
