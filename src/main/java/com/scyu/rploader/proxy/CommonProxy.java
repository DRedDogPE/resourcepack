package com.scyu.rploader.proxy;

public interface CommonProxy {

    void preInit();

    void init();

    void postInit();

    void registerEvents();

}
