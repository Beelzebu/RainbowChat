package com.github.beelzebu.rainbowchat.config;

import com.github.beelzebu.rainbowchat.RainbowChat;
import com.github.beelzebu.rainbowchat.channel.ChatChannel;
import com.github.beelzebu.rainbowchat.channel.DynamicAudienceChatChannel;
import com.github.beelzebu.rainbowchat.channel.SimpleChatChannel;
import com.github.beelzebu.rainbowchat.composer.RainbowComposer;
import com.github.beelzebu.rainbowchat.hook.HookType;
import com.github.beelzebu.rainbowchat.util.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public class Config {

    private final RainbowChat plugin;

    public Config(RainbowChat plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.reloadConfig();
        Objects.requireNonNull(plugin.getConfig().getConfigurationSection("formats")).getKeys(false).forEach(formatName -> readFormat(formatName, plugin.getConfig().getConfigurationSection("formats." + formatName)));
        Objects.requireNonNull(plugin.getConfig().getConfigurationSection("channels")).getKeys(false).forEach(channel -> plugin.getStorage().loadChannel(readChannel(channel, plugin.getConfig().getConfigurationSection("channels." + channel))));
        if (plugin.getStorage().getChannels().isEmpty()) {
            plugin.getStorage().loadChannel(SimpleChatChannel.DEFAULT_CHANNEL);
        }
        if (plugin.getStorage().getChannels().size() == 1) {
            for (ChatChannel channel : plugin.getStorage().getChannels()) {
                if (!channel.isDefault()) {
                    channel.setDefault(true);
                }
                break;
            }
        }
    }

    public void unload() {
        plugin.getStorage().clearChannels();
        RainbowComposer.COMPOSERS.clear();
    }

    public void reload() {
        unload();
        load();
    }

    public boolean isHookEnabled(HookType hook) {
        return plugin.getConfig().getBoolean("hooks." + hook.getName().toLowerCase(Locale.ROOT) + ".enabled", false);
    }

    private @Nullable TextComponent readComponent(@NotNull ConfigurationSection section) {
        ClickEvent.Action action = null;
        try {
            String actionName = section.getString("click-action");
            if (actionName != null) {
                action = ClickEvent.Action.valueOf(actionName);
            }
        } catch (@NotNull IllegalArgumentException | NullPointerException e) {
            e.printStackTrace();
        }
        String text = section.getString("text");
        if (text == null || text.isBlank()) {
            return null;
        }
        return Util.parseComponent(text, section.getStringList("hover"), action, section.getString("click-command"));
    }

    private void readFormat(String formatName, @NotNull ConfigurationSection section) {
        List<Component> children = new ArrayList<>();
        Component prev = null;
        for (String part : Objects.requireNonNull(section.getConfigurationSection("parts")).getKeys(false)) {
            Component component = readComponent(Objects.requireNonNull(section.getConfigurationSection("parts." + part)));
            if (component == null) {
                continue;
            }
            children.add(prev = (prev != null ? component.colorIfAbsent(prev.color()) : component));
        }
        if (section.contains("style")) {
            Style style = Util.parseStyle(Objects.requireNonNull(section.getConfigurationSection("style")));
            RainbowComposer.load(
                    formatName,
                    section.getString("permission"),
                    section.getInt("priority", 1),
                    Component.empty().children(children),
                    style);

        } else {
            TextColor textColor = Util.parseTextColor(section.getString("color", "f").replaceAll("&", ""));
            RainbowComposer.load(
                    formatName,
                    section.getString("permission"),
                    section.getInt("priority", 1),
                    Component.empty().children(children),
                    textColor);
        }
    }

    public @NotNull SimpleChatChannel readChannel(@NotNull String channel, @NotNull ConfigurationSection section) {
        return new SimpleChatChannel(
                channel,
                Objects.requireNonNull(section.getString("command")),
                section.getStringList("aliases").toArray(new String[0]),
                section.getBoolean("default"),
                Util.deserialize(Objects.requireNonNull(section.getString("displayname"))),
                section.getStringList("formats"),
                section.getString("permission"),
                section.getBoolean("hologram")
        );
    }

    public @NotNull DynamicAudienceChatChannel readChannel(@NotNull String channel, @NotNull ConfigurationSection section, Function<Player, Audience> audienceFunction) {
        return new DynamicAudienceChatChannel(
                channel,
                Objects.requireNonNull(section.getString("command")),
                section.getStringList("aliases").toArray(new String[0]),
                Util.deserialize(Objects.requireNonNull(section.getString("displayname"))),
                section.getStringList("formats"),
                audienceFunction,
                section.getBoolean("hologram")
        );
    }
}
