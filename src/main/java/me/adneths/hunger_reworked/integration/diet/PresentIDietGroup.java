package me.adneths.hunger_reworked.integration.diet;

import top.theillusivec4.diet.api.IDietGroup;

public class PresentIDietGroup implements ProxyIDietGroup
{
	private IDietGroup group;
	
	public PresentIDietGroup(IDietGroup group)
	{
		this.group = group;
	}
	
	@Override
	public String getName()
	{
		return this.group.getName();
	}

}
