package com.scyu.rploader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ResourcePackRepository;
import scala.xml.Null;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Reference {

	public static final String MOD_ID = "rploader";
	public static final String NAME = "RP Loader";
	public static final String VERSION = "1.1";
	public static final String MCVERSION = "[1.11,)";



	public static final String CLIENT_PROXY_CLASS = "com.scyu.rploader.proxy.ClientProxy";
	public static final String SERVER_PROXY_CLASS = "com.scyu.rploader.proxy.ServerProxy";

    public static void copyClass(Class<?> c, Object src, Object dest) {
        Field[] fields = c.getDeclaredFields();
        Field field;
        for (int i = 0; i < fields.length; i++) {
            field = fields[i];
            if ((field.getModifiers() & Modifier.FINAL) == Modifier.FINAL && (field.getModifiers() & Modifier.STATIC) == Modifier.STATIC) continue;
            field.setAccessible(false);
            field.setAccessible(true);
            try {
                field.set(dest, field.get(src));
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            }
        }
    }

    public static void copyClassDeep(Class<?> c, Object src, Object dest) {
        Class clazz = c;

        do {
            copyClass(clazz, src, dest);
        } while (!(clazz = clazz.getSuperclass()).equals(Object.class));
    }


	public static Minecraft mc() {
		return Minecraft.getMinecraft();
	}

	public static ResourcePackRepositoryWE getResourcePackRepository() {
		ResourcePackRepository rpr = mc().getResourcePackRepository();
		//		System.out.println("rpr: " + rpr);
		if (rpr instanceof ResourcePackRepositoryWE)
			return (ResourcePackRepositoryWE)rpr;
		System.out.println("new rpr");
		ResourcePackRepositoryWE nrpr = new ResourcePackRepositoryWE(rpr);
		Reference.setField(Minecraft.class, 76, mc(), nrpr);
		return nrpr;
	}



	public static <T> T getField(Class<?> clazz, int id, Object owner, Object...nested) {
		Field field = clazz.getDeclaredFields()[id];
		boolean a = field.isAccessible();
		field.setAccessible(true);
		Object obj;
		try {
			obj = field.get(owner);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return null;
		}
		field.setAccessible(a);
		if (nested != null && nested.length > 2) {
			int len = nested.length / 3;
			for (int i = 0; i < len; i++) {
				Class<?> nclazz;
				int nid;
				Object nowner;
				try {
					nclazz = (Class<?>)nested[i * 3];
					nid = (int)nested[i*3+1];
					nowner = nested[i*3+2];
				} catch (ClassCastException e) {
					return null;
				}
				Field nfield = nclazz.getDeclaredFields()[nid];
				boolean na = nfield.isAccessible();
				nfield.setAccessible(true);
				try {
					obj = nfield.get(nowner);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					return null;
				}
				nfield.setAccessible(na);
			}
		}
		T t = null;
		try {
			t = (T)obj;
		} catch (ClassCastException ignored) {
		}
		return t;
	}

	public static <T> void setField(Class<?> clazz, int id, Object owner, T value) {
		Field field = clazz.getDeclaredFields()[id];
		boolean a = field.isAccessible();
		field.setAccessible(true);
		try {
			field.set(owner, value);
		} catch (IllegalArgumentException | IllegalAccessException ignored) {
		}
		field.setAccessible(a);
	}

}
