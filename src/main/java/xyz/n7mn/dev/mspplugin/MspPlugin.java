package xyz.n7mn.dev.mspplugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class MspPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new MinecartEvent(this), this);
        getLogger().info("Started msp-plugin");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Shutdown msp-plugin");
        this.setEnabled(false);
    }

}
