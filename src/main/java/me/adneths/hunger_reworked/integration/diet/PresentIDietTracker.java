package me.adneths.hunger_reworked.integration.diet;

import net.minecraft.world.item.ItemStack;
import top.theillusivec4.diet.api.IDietTracker;

public class PresentIDietTracker implements ProxyIDietTracker
{
	private IDietTracker diet;
	
	public PresentIDietTracker(IDietTracker diet)
	{
		this.diet = diet;
	}
	
	public void consume(ItemStack stack, int healing, float saturationModifier)
	{
		this.diet.consume(stack, healing, saturationModifier);
	}
	
	public float getValue(String group)
	{
		return this.diet.getValue(group);
	}

	public void setValue(String group, float amount)
	{
		this.diet.setValue(group, amount);
	}

	public void sync()
	{
		this.diet.sync();
	}
	
}
