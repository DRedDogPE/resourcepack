package com.scyu.rploader.util;

import com.scyu.rploader.RPLoader;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ModSettings {
	
	private Map<String, Object> values;
	private Map<String, Object> valuesLow;
	private long lastSave = -1L;
	private boolean dirty;
	
	private ModSettings() {
		values = new HashMap <>();
		valuesLow = new HashMap <>();

		dirty = false;
		load();
	}
		
	private boolean delta() {
		long now = System.currentTimeMillis();
		long delta = now - lastSave;
		if (delta >= 15 * 1000) {
			lastSave = now;
			return true;
		}
		return false;
	}
	
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (lastSave < 0L) lastSave = System.currentTimeMillis();
		boolean ticked = delta();
		if (!ticked) return;
		if (!dirty) return;
		save();
		dirty = false;
	}

	private void save() {
		NBTTagCompound tag = new NBTTagCompound();
		NBTTagCompound data = new NBTTagCompound();
		Set<String> keys = values.keySet();
		for (String key : keys) {
			Object value = values.get(key);
			if (value instanceof String) {
				data.setString(key, (String)value);
				continue;
			}
			if (value instanceof Boolean) {
				data.setByte(key, ((Boolean) value) ? (byte)1 : (byte)0);
				continue;
			}
			if (value instanceof Integer) {
				data.setInteger(key, (Integer) value);
				continue;
			}
			if (value instanceof Float) {
				data.setFloat(key, (Float) value);
				continue;
			}
		}
		tag.setTag("data", data);
		try {
			CompressedStreamTools.write(tag, new File(RPLoader.modFolder, "data"));
			dirty = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void load() {
		try {
			NBTTagCompound tag = CompressedStreamTools.read(new File(RPLoader.modFolder, "data"));
			if (tag == null) return;
			NBTTagCompound data = tag.getCompoundTag("data");
			Set<String> keys = data.getKeySet();
			for (String key : keys) {
				if (data.hasKey(key, NBT.TAG_STRING)) values.put(key, data.getString(key));
				if (data.hasKey(key, NBT.TAG_BYTE)) values.put(key, data.getByte(key) == ((byte)1));
				if (data.hasKey(key, NBT.TAG_INT)) values.put(key, data.getInteger(key));
				if (data.hasKey(key, NBT.TAG_FLOAT)) values.put(key, data.getFloat(key));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void _set(String key, Object value) {
		values.put(key, value);
		dirty = true;
	}
	
	private void _setLow(String key, Object value) {
		valuesLow.put(key, value);
	}
	
	public static <T> void setLow(String key, T value) {
//		String logLow = String.format("setLow; Key: %s Value: %s", key, value);
//		System.out.println(logLow);
		inst()._setLow(key, value);
	}
	
	public static <T> T getLow(String key) {
		boolean has = inst().hasLow(key);
		T value = (has) ? ((T)inst().valuesLow.get(key)) : null;
//		String logLow = String.format("getLow; Key: %s Value: %s", key, value);
//		System.out.println(logLow);
		return value;
	}
	
	public static boolean hasLow(String key) {
		return inst().valuesLow.containsKey(key);
	}
	
	public static <T> void set(String key, T value) {
		if (value instanceof String || value instanceof Integer || value instanceof Boolean || value instanceof Float) {
			inst()._set(key, value);
		}
	}
	
	public static <T> T get(String key) {
		if (!has(key)) return null;
		Object value = inst().values.get(key);
		if (value instanceof String || value instanceof Integer || value instanceof Boolean || value instanceof Float)
			return (T)value;
		return null;
	}
	
	public static boolean has(String key) {
		return inst().values.containsKey(key);
	}
	
	private static ModSettings instance = null;
	private static ModSettings inst() {
		if (instance != null) return instance;
		instance = new ModSettings();
		MinecraftForge.EVENT_BUS.register(instance);
		return instance;
	}
}
