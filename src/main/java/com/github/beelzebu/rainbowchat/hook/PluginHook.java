package com.github.beelzebu.rainbowchat.hook;

import com.github.beelzebu.rainbowchat.RainbowChat;
import com.github.beelzebu.rainbowchat.config.Config;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Beelzebu
 */
public abstract class PluginHook {

    protected final RainbowChat plugin;

    public PluginHook(RainbowChat plugin) {
        this.plugin = plugin;
    }

    public abstract void register();

    public abstract void unregister();

    public abstract ConfigurationSection getChannels();

    public abstract void loadChannel(ConfigurationSection section, Config config);
}
