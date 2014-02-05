package org.royaldev.lmgtfy;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class LMGTFYPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        if (!new File(this.getDataFolder(), "config.yml").exists()) this.saveDefaultConfig();
        this.reloadConfig();

        final PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new ChatListener(), this);

        this.getCommand("lmgtfy").setExecutor(new LMGTFYCommand(this));
    }

}
