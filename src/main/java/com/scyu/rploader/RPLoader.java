package com.scyu.rploader;

import com.scyu.rploader.proxy.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.versioning.VersionRange;
import org.lwjgl.opengl.Display;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod(modid = Reference.MOD_ID, name = Reference.NAME, version = Reference.VERSION, acceptedMinecraftVersions = Reference.MCVERSION)
public class RPLoader {

    /**
     * The instance of this class
     */
    @Instance(Reference.MOD_ID)
    public static RPLoader instance;


    /**
     * The proxy for client and server.
     */
    @SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
    public static CommonProxy proxy;
    public static File modFolder;

    /**
     * The first init method, this is where you init the basic early stuff
     *
     * @param event The event object
     */
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        modFolder = new File(event.getModConfigurationDirectory(), "/RPLoader/");
        if (!modFolder.exists()) modFolder.mkdirs();
        proxy.preInit();
    }

    /**
     * The second init method, this is where you init stuff like keybindings
     *
     * @param event The event object
     */
    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }

    /**
     * The third and last init method, this is where you register events and stuff
     *
     * @param event The event object
     */
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
        proxy.registerEvents();
    }

    /**
     * The mod folder to save and load from
     */
    private static Minecraft mc;
    private static boolean prevOperate = false;

    /**
     * Function to check if it plugin's methods should operate or not
     *
     * @return true, if plugin should operate
     */
    public static boolean operate() {
        boolean operat = _operate();
        prevOperate = operat;
        return operat;
    }

    /**
     * The actual operate method
     *
     * @return true if plugin should operate or if EnhancedWynn.DEBUG is true
     */
    private static boolean _operate() {
        if (mc == null) {
            mc = Minecraft.getMinecraft();
        }
        ServerData data = mc.getCurrentServerData();
        return data != null && data.serverIP != null && validServer(data.serverIP.trim()) && mc.player != null && mc.player.experienceLevel >= 1;
    }

    /**
     * Returns a boolean value determining if the server IP is valid for the plugin to operate
     *
     * @param ip The ip to check for
     * @return true if the IP is valid
     */
    private static boolean validServer(String ip) {
        if (ip.endsWith(":25565")) ip.substring(0, ip.lastIndexOf(":25565"));
        return ip.toLowerCase().contains("wynncraft");
    }
}
