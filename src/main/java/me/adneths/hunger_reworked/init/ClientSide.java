package me.adneths.hunger_reworked.init;

import com.mojang.blaze3d.systems.RenderSystem;

import me.adneths.hunger_reworked.HungerReworked;
import me.adneths.hunger_reworked.capability.PlayerStomachProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSide
{
	public static Minecraft minecraft;
	
	protected static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(HungerReworked.MODID, "textures/gui/stomach.png");
	public static IGuiOverlay STOMACH_OVERLAY = (gui, poseStack, partialTicks, width, height) -> {
		Player player = ClientSide.minecraft.player;
		if (player.getAbilities().instabuild || player.isSpectator())
			return;
		player.getCapability(PlayerStomachProvider.PLAYER_STOMACH).ifPresent((stomach) -> {
			int foodAmount = stomach.totalFood;
			
			MobEffectInstance ss = player.getEffect(Registration.STRONG_STOMACH.get());
			int ssa = ss == null ? 0 : ss.getAmplifier() + 1;

			int left = width / 2 + 91;
			int top = height - gui.rightHeight;
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, TEXTURE_LOCATION);
			int maxAmount = Math.max(foodAmount, 20 + ssa * 8);
			
			int rows = Math.max(1, (maxAmount + 19) / 20);
			int rowHeight = Math.max(3, 12 - rows*2);
			for (int j = rows-1; j > -1; j--)
				for (int i = 0; i < Math.min(10, j == 0 ? 10 : ((maxAmount + 1) / 2) - j * 10); i++)
					GuiComponent.blit(poseStack, left - 9 - 8 * i, top - rowHeight * j, 8, 8, foodAmount - i * 2 - j * 20 < 1 ? 0 : foodAmount - i * 2 - j * 20 == 1 ? 16 : 32,
							j > 0 && i + j * 10 - 10 < ssa * 4 ? 16 : 0, 16, 16, 48, 32);

			gui.rightHeight += rowHeight * (rows-1) + 10;
		});
	};
	
	@SubscribeEvent
	public static void init(final FMLClientSetupEvent event)
	{
		minecraft = Minecraft.getInstance();
	}
	
	@SubscribeEvent
	public static void onRegisterOverlay(RegisterGuiOverlaysEvent event)
	{
		event.registerAbove(VanillaGuiOverlay.FOOD_LEVEL.id(), "stomach_hud", STOMACH_OVERLAY);
	}
}