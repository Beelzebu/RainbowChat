package com.github.beelzebu.rainbowchat.hook;

import com.github.beelzebu.rainbowchat.RainbowChat;
import com.github.beelzebu.rainbowchat.channel.ChatChannel;
import java.util.Objects;
import java.util.function.Function;
import net.kyori.adventure.audience.Audience;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * @author Beelzebu
 */
public abstract class Hook {

    protected final RainbowChat plugin;

    public Hook(RainbowChat plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("Loading hook: " + getClass().getSimpleName());
    }

    public abstract void register();

    public abstract void unregister();

    public abstract ConfigurationSection getChannelsConfig();

    public final void loadChannel(String key, Function<Player, Audience> audience) {
        ChatChannel channel;
        if (audience == null) {
            channel = plugin.getChatConfig().readChannel(key, Objects.requireNonNull(getChannelsConfig().getConfigurationSection(key)));
        } else {
            channel = plugin.getChatConfig().readChannel(key, Objects.requireNonNull(getChannelsConfig().getConfigurationSection(key)), audience);
        }
        plugin.getStorage().loadChannel(channel);
    }
}
