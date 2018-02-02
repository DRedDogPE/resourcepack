package com.scyu.rploader;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.scyu.rploader.util.ModSettings;
import net.minecraft.client.resources.ResourcePackRepository;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ResourcePackRepositoryWE extends ResourcePackRepository {

	private ResourcePackRepository original;

	public ResourcePackRepositoryWE(ResourcePackRepository original) {
		super(Reference.getField(ResourcePackRepository.class, 4, original),
				Reference.getField(ResourcePackRepository.class, 6, original),
				original.rprDefaultResourcePack,
				original.rprMetadataSerializer,
				Reference.mc().gameSettings);

		Reference.copyClass(ResourcePackRepository.class, original, this);
	}

	@Override
	public void clearResourcePack() {
		System.out.println("clear rp");
		String state = ModSettings.getLow("worldstate");
		System.out.println("rp state: " + (state == null ? "null" : state) + " ret: " + (state != null && state.equals("unload") && getServerResourcePack() != null));
		if (state != null && state.equals("unload") && getServerResourcePack() != null)
			return;

		super.clearResourcePack();
	}

	@Override
	public ListenableFuture<Object> setServerResourcePack(File resourceFile) {

		try {
			ZipFile file = new ZipFile(resourceFile);
			ZipEntry entry = file.getEntry("pack.mcmeta");
			BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(entry), "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("\"pack_format\"")) {
					System.out.println("IsLine: " + line);
					String part = line.substring(0, line.lastIndexOf(',') == -1 ? line.length() : line.lastIndexOf(',')).trim();
					System.out.println("IsPart: " + part);
					if (part.contains(":") && !part.split(":")[1].trim().equals("2")) {
						reader.close();
						file.close();
						System.out.println("File: " + resourceFile.getPath() + " Exists: " + resourceFile.exists());
						if (resourceFile.exists()) {
							if (!resourceFile.delete()) {
								System.out.println("Failed delete");
								resourceFile.deleteOnExit();
							} else
								System.out.println("Success delete");
						}
						System.out.println("FExists: " + resourceFile.exists());
						ModSettings.setLow("rpodt", System.currentTimeMillis());
                        ModSettings.set("rpname", null);
                        ModSettings.setLow("rpname", null);
                        return Futures.immediateFailedFuture(new RuntimeException("Invalid resourcepack"));
					}
					break;
				}
				System.out.println("OtherLine: " + line);
			}
			reader.close();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return super.setServerResourcePack(resourceFile);
	}


}
