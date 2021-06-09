package com.github.beelzebu.rainbowchat.config;

import com.github.beelzebu.rainbowchat.RainbowChat;
import com.github.beelzebu.rainbowchat.channel.ChatChannel;
import com.github.beelzebu.rainbowchat.channel.DynamicAudienceChatChannel;
import com.github.beelzebu.rainbowchat.channel.SimpleChatChannel;
import com.github.beelzebu.rainbowchat.composer.RainbowComposer;
import com.github.beelzebu.rainbowchat.util.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyFormat;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public class Config {

    public static final String TOWNY_HOOK = "towny";
    private final RainbowChat plugin;

    public Config(RainbowChat plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveResource("config.yml", false);
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

    public boolean isHookEnabled(String hook) {
        return plugin.getConfig().getBoolean("hooks." + hook + ".enabled", false);
    }

    private @NotNull ConfigComponent readPart(@NotNull ConfigurationSection section) {
        ClickEvent.Action action = null;
        try {
            String actionName = section.getString("click-action");
            if (actionName != null) {
                action = ClickEvent.Action.valueOf(actionName);
            }
        } catch (@NotNull IllegalArgumentException | NullPointerException e) {
            e.printStackTrace();
        }
        return new ConfigComponent(section.getString("text"), section.getStringList("hover"), action, section.getString("click-command"));
    }

    private void readFormat(String formatName, @NotNull ConfigurationSection section) {
        List<Component> children = new ArrayList<>();
        Component prev = null;
        for (String part : Objects.requireNonNull(section.getConfigurationSection("parts")).getKeys(false)) {
            Component child = readPart(Objects.requireNonNull(section.getConfigurationSection("parts." + part))).getComponent();
            if (prev != null) {
                Component finalPrev = prev;
                child = child.style(builder -> builder.merge(finalPrev.style(), Style.Merge.Strategy.IF_ABSENT_ON_TARGET, Style.Merge.COLOR), Style.Merge.Strategy.IF_ABSENT_ON_TARGET);
            }
            children.add(prev = child);
        }
        String colorText = section.getString("color", "f").replaceAll("&", "");
        TextColor color = null;
        if (colorText.startsWith("#")) {
            color = TextColor.fromHexString(colorText);
        }
        if (color == null) {
            LegacyFormat legacyFormat = LegacyComponentSerializer.parseChar(colorText.charAt(0));
            if (legacyFormat != null) {
                color = legacyFormat.color();
            }
        }
        if (color == null) {
            color = NamedTextColor.WHITE;
        }
        RainbowComposer.cache(RainbowComposer.load(
                formatName,
                section.getString("permission"),
                section.getInt("priority", 1),
                Component.empty().children(children),
                color));
    }

    public @NotNull ChatChannel readChannel(@NotNull String channel, @NotNull ConfigurationSection section) {
        return new SimpleChatChannel(
                channel,
                Objects.requireNonNull(section.getString("command")),
                section.getStringList("aliases").toArray(new String[0]),
                section.getBoolean("default"),
                Util.deserialize(Objects.requireNonNull(section.getString("displayname"))),
                section.getStringList("formats"),
                section.getString("permission")
        );
    }

    public @NotNull ChatChannel readChannel(@NotNull String channel, @NotNull ConfigurationSection section, Function<Player, Audience> audienceFunction) {
        return new DynamicAudienceChatChannel(
                channel,
                Objects.requireNonNull(section.getString("command")),
                section.getStringList("aliases").toArray(new String[0]),
                Util.deserialize(Objects.requireNonNull(section.getString("displayname"))),
                section.getStringList("formats"),
                audienceFunction
        );
    }

    private static class ConfigComponent {

        private final @NotNull TextComponent text;
        private final @Nullable Component hoverText;
        private final @Nullable ClickEvent.Action clickAction;
        private final @Nullable String clickCommand;

        public ConfigComponent(@Nullable String text, @Nullable List<String> hoverText, @Nullable ClickEvent.Action clickAction, @Nullable String clickCommand) {
            this.text = text != null ? Util.deserialize(text) : Component.empty();
            this.hoverText = hoverText == null ? null : Util.deserialize(String.join("\n", hoverText));
            if (clickAction != null && clickCommand != null) {
                this.clickAction = clickAction;
                this.clickCommand = clickCommand;
            } else {
                this.clickAction = null;
                this.clickCommand = null;
            }
        }

        public @NotNull Component getComponent() {
            Component component = text.hoverEvent(hoverText);
            if (clickAction != null && clickCommand != null) {
                component = component.clickEvent(ClickEvent.clickEvent(clickAction, clickCommand));
            }
            return component;
        }
    }
}
