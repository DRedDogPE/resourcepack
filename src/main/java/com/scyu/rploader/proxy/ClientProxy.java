package com.scyu.rploader.proxy;

import com.scyu.rploader.AutoResourcePack;
import com.scyu.rploader.NetworkFilter;
import com.scyu.rploader.Reference;
import com.scyu.rploader.util.ModSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

import java.io.File;

@Mod.EventBusSubscriber
public class ClientProxy implements CommonProxy {

	@Override
	public void registerEvents() {
		Minecraft mc = Minecraft.getMinecraft();
        MinecraftForge.EVENT_BUS.register(new NetworkFilter(mc));
		MinecraftForge.EVENT_BUS.register(new AutoResourcePack(mc));
		MinecraftForge.EVENT_BUS.register(this);
	}


	@Override
	public void init() {

	}


	@Override
	public void preInit() {
		ModSettings.has("loaded");
	}

	@Override
	public void postInit() {
		System.out.println("Post init");
		String name = ModSettings.get("rpname");
		System.out.println("Rpname: " + name);
		if (name == null) {
			System.out.println("LowRpname: null");
			ModSettings.setLow("rpname", "null");
			return;
		}
		// Get RPR
		System.out.println("Getting RPR");
		ResourcePackRepository rep = Reference.getResourcePackRepository();
		// Get root directory
		System.out.println("Getting root dir");
		File rootFolder = new File(System.getProperty("user.dir"));
		System.out.println("Root dir: " + rootFolder.getPath());
		// Get Server Resourcepack Folder
		System.out.println("Getting SRF");
		File srf = Reference.getField(ResourcePackRepository.class, 6, rep);
		System.out.println("SRF: " + srf.getPath() + " Name: " + srf.getName());
		// Reset Server Resourcepack Folder
		System.out.println("Resetting SRF");
		srf = new File(rootFolder, srf.getName());
		System.out.println("ResetSRF: " + srf.getPath());
		// Create new final file for Server Resourcepack File
		System.out.println("Getting rpFile");
		File rpFile = new File(srf, name);
		
		if (!rpFile.exists()) {
			System.out.println("File doesn't exist: " + rpFile.getPath());
			ModSettings.setLow("rpname", "null");
			return;
		}
		System.out.println("File does exist: " + rpFile.getPath());
		System.out.println("Setting SRP: " + rpFile.getPath());
		
		rep.setServerResourcePack(rpFile);
		
		long rpodt = ModSettings.hasLow("rpodt") ? ModSettings.getLow("rpodt") : 0L;
		
		if (System.currentTimeMillis() - rpodt > 2000L) {
			System.out.println("LowRpname: " + name);
			ModSettings.setLow("rpname", name);
		}
		
	}

}
