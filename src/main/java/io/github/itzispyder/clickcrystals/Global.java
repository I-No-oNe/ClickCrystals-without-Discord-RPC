package io.github.itzispyder.clickcrystals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.itzispyder.clickcrystals.client.system.ClickCrystalsSystem;
import net.minecraft.client.MinecraftClient;

public interface Global {

    MinecraftClient mc = MinecraftClient.getInstance();
    ClickCrystalsSystem system = ClickCrystalsSystem.getInstance();
    String prefix = ClickCrystals.prefix;
    String version = ClickCrystals.version;
    String starter = ClickCrystals.starter;
    String modId = ClickCrystals.modId;
    Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

}
