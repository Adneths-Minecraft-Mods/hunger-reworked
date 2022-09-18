package me.adneths.hunger_reworked.init.datagen;

import me.adneths.hunger_reworked.HungerReworked;
import me.adneths.hunger_reworked.init.Registration;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class GenLanguageProvider extends LanguageProvider
{

	public GenLanguageProvider(DataGenerator gen, String locale)
	{
		super(gen, HungerReworked.MODID, locale);
	}

	@Override
	protected void addTranslations()
	{
		add("effect."+HungerReworked.MODID+"."+Registration.STRONG_STOMACH.getId().getPath(), "Strong Stomach");
		add("effect."+HungerReworked.MODID+"."+Registration.VOMITING.getId().getPath(), "Vomiting");
		add("effect."+HungerReworked.MODID+"."+Registration.OVERSTUFFED.getId().getPath(), "Overstuffed");
		
		add("item.minecraft.potion.effect.strong_stomach_potion", "Potion of Strong Stomach");
		add("item.minecraft.splash_potion.effect.strong_stomach_potion", "Splash Potion of Strong Stomach");
		add("item.minecraft.lingering_potion.effect.strong_stomach_potion", "Lingering Potion of Strong Stomach");
		add("item.minecraft.tipped_arrow.effect.strong_stomach_potion", "Arrow of Strong Stomach");
	}
}
