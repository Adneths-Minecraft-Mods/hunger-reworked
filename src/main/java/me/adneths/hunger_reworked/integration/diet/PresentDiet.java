package me.adneths.hunger_reworked.integration.diet;

import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.diet.api.DietCapability;
import top.theillusivec4.diet.common.group.DietGroups;

public class PresentDiet extends ProxyDiet
{	
	@Override
	public LazyOptional<ProxyIDietTracker> get(Player player)
	{
		return DietCapability.get(player).lazyMap((diet) -> new PresentIDietTracker(diet));
	}
	
	@Override
	public Set<ProxyIDietGroup> getGroups()
	{
		return DietGroups.get().stream().map((group) -> new PresentIDietGroup(group)).collect(Collectors.toSet());
	}
}
