package com.github.beelzebu.rainbowchat.placeholder;

import com.github.beelzebu.rainbowchat.RainbowChat;
import com.github.beelzebu.rainbowchat.util.Util;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class RainbowChatPlaceholders extends PlaceholderExpansion {

    private final RainbowChat plugin;

    public RainbowChatPlaceholders(RainbowChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "rainbowchat";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Beelzebu";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull String onPlaceholderRequest(Player player, @NotNull String params) {
        return switch (params.toLowerCase()) {
            case "channel_name" -> plugin.getStorage().getChannel(player).getName();
            case "channel", "channel_display" -> Util.serialize(plugin.getStorage().getChannel(player).getDisplayName());
            default -> "unknown placeholder";
        };
    }
}
