package com.scyu.rploader;

import com.scyu.rploader.util.ModSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.lang.reflect.Field;

public class AutoResourcePack {
	
	private Minecraft mc;
	
	public AutoResourcePack(Minecraft mcIn) {
		this.mc = mcIn;
	}
	
	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		GuiScreen gui = event.getGui();
		if (gui instanceof GuiYesNo) {
            String msg = I18n.format("multiplayer.texturePrompt.line1");
			Field field = GuiYesNo.class.getDeclaredFields()[1];
			field.setAccessible(true);
			try {
				String _msg = (String) field.get(gui);
				if (_msg.equals(msg)) {
					GuiYesNo yn = (GuiYesNo) gui;
					event.setCanceled(true);
					GuiYesNoCallback cb = null;
					field = GuiYesNo.class.getDeclaredFields()[0];
					field.setAccessible(true);
					cb = (GuiYesNoCallback)field.get(yn);
					cb.confirmClicked(true, 0);
				}
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			}
		}
	}
	
	private boolean unload = false;
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		ModSettings.setLow("worldstate", "unload");
	}
	
	public void onWorldLoad(WorldEvent.Load event) {
		ModSettings.setLow("worldstate", "load");
	}
	
	@SubscribeEvent
	public void onClientUpdate(TickEvent.ClientTickEvent event) {
		if (!RPLoader.operate()) updateOffline();
		else updateOnline();
	}
	
	private void test() {
        Reference.mc().loadWorld(null);
	}
	
	private void updateOffline() {
		if (mc.world != null) {
			updateLobby();
			return;
		}
		String name = ModSettings.getLow("rpname");
		if (name != null && !name.equals("null")) {
			ResourcePackRepository rep = Reference.getResourcePackRepository();
			if (rep.getServerResourcePack() == null) {
				File rootFolder = new File(System.getProperty("user.dir"), Reference.mc().mcDataDir.getPath());
				File srf = Reference.getField(ResourcePackRepository.class, 6, rep);
				srf = new File(rootFolder, srf.getName());
				File rpFile = new File(srf, name);
				
				if (rpFile.exists()) {
//					rep.setServerResourcePack(rpFile);
				}
			}
		}
	}
	
	private void updateLobby() {
		
	}
	
	private void updateOnline() {
		ItemStack stack = mc.player.inventory.getStackInSlot(8);
		if (stack == null) return;
		if (stack.getItem() != Items.NETHER_STAR) return;
		String name = TextFormatting.getTextWithoutFormattingCodes(stack.getDisplayName()).trim();
		if (!name.contains("Soul")) return;
		ResourcePackRepository rpr = mc.getResourcePackRepository();
		IResourcePack pack = rpr.getServerResourcePack();
		
		if (pack != null) {
			ModSettings.setLow("rpname", pack.getPackName());
			ModSettings.set("rpname", pack.getPackName());
		}
//		System.out.println("SRP: " + ((pack == null) ? ("none") : (pack.getPackName())));
	}
	
}
