package com.scyu.rploader;

import com.mojang.authlib.GameProfile;
import com.scyu.rploader.util.ModSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.client.CPacketResourcePackStatus;
import net.minecraft.network.play.server.SPacketResourcePackSend;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import org.apache.commons.codec.digest.DigestUtils;

import java.lang.reflect.Field;

public class NetworkFilter {
	
	private Minecraft mc;
	
	public NetworkFilter(Minecraft mc) {
		this.mc = mc;
	}
	
	@SubscribeEvent
	public void onClientConnectedToServer(ClientConnectedToServerEvent event) {
		try {
			System.out.println("INJECTING NETWORK FILTER");
			NetworkManager nm = event.getManager();
			Field field = NetworkManager.class.getDeclaredFields()[12];
			field.setAccessible(false);
			field.setAccessible(true);
			NetHandlerPlayClient nhpc = (NetHandlerPlayClient)field.get(nm);
			Field[] fields = NetHandlerPlayClient.class.getDeclaredFields();
			field = fields[3];
			field.setAccessible(false);
			field.setAccessible(true);
			GuiScreen gui = (GuiScreen)field.get(nhpc);
			field = fields[2];
			field.setAccessible(false);
			field.setAccessible(true);
			GameProfile prof = (GameProfile)field.get(nhpc);
			NetHandlerPlayClient nnhpc = new NetworkPlayFilter(Minecraft.getMinecraft(), gui, nm, prof, nhpc);
			field = NetworkManager.class.getDeclaredFields()[12];
			field.setAccessible(false);
			field.setAccessible(true);
			field.set(nm, nnhpc);
			System.out.println("INJECT COMPLETE");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
	}
	
	public static class NetworkPlayFilter extends NetHandlerPlayClient {
		
		private final NetHandlerPlayClient original;
		
		public NetworkPlayFilter(Minecraft mcIn, GuiScreen p_i46300_2_, NetworkManager networkManagerIn,
				GameProfile profileIn, NetHandlerPlayClient original) {
			super(mcIn, p_i46300_2_, networkManagerIn, profileIn);
			this.original = original;
			
			Reference.copyClassDeep(NetworkPlayFilter.class, original, this);
		}
		
		@Override
		public void handleResourcePack(SPacketResourcePackSend packetIn){
			System.out.println("RP TEST");
			if (Reference.getResourcePackRepository().getServerResourcePack() != null) {
				System.out.println("RP ACTIVE");
				String hashLow = ModSettings.getLow("rpname");
				hashLow = hashLow.trim();
				String hashPacket = packetIn.getHash().trim();
				if (hashPacket == null || hashPacket.equals("null")) {
					hashPacket = DigestUtils.sha1Hex(packetIn.getURL()).trim();
				}
				if (hashPacket.equalsIgnoreCase(hashLow)) {
					System.out.println("RP VALID");
					NetworkManager nm = super.getNetworkManager();
					nm.sendPacket(new CPacketResourcePackStatus(CPacketResourcePackStatus.Action.ACCEPTED));
					nm.sendPacket(new CPacketResourcePackStatus(CPacketResourcePackStatus.Action.SUCCESSFULLY_LOADED));
					return;
				}
				System.out.println("RP INVALID");
			}
			System.out.println("RP DEFAULT");
			ModSettings.setLow("rpstate", "loading");
			super.handleResourcePack(packetIn);
			ModSettings.setLow("rpstate", "good");
			ModSettings.setLow("rpname", packetIn.getHash().trim());
			ModSettings.set("rpname", packetIn.getHash().trim());
		}
	}
	
}
