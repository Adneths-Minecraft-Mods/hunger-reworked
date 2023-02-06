package me.adneths.hunger_reworked.integration.diet;

import net.minecraft.world.item.ItemStack;

public interface ProxyIDietTracker
{
	public void consume(ItemStack stack, int healing, float saturationModifier);
	public float getValue(String group);
	public void setValue(String group, float amount);
	public void sync();
}
