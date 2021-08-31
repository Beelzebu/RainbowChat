package com.github.beelzebu.rainbowchat.composer;

import com.github.beelzebu.rainbowchat.util.Util;
import io.papermc.paper.chat.ChatRenderer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public class RainbowComposer implements ChatRenderer {

    public static final List<RainbowComposer> COMPOSERS = new ArrayList<>();
    private final String name;
    private final String permission;
    private final int priority;
    private final Component format;
    private final TextColor color;

    public static @NotNull RainbowComposer load(String name, String permission, int priority, Component format, TextColor color) {
        RainbowComposer rainbowComposer = new RainbowComposer(name, permission, priority, format, color);
        COMPOSERS.add(rainbowComposer);
        COMPOSERS.sort(Comparator.comparingInt(RainbowComposer::getPriority));
        return rainbowComposer;
    }

    private RainbowComposer(String name, String permission, int priority, Component format, TextColor color) {
        this.name = name;
        this.permission = permission;
        this.priority = priority;
        this.format = format;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public int getPriority() {
        return priority;
    }

    public Component getFormat() {
        return format;
    }

    @Override
    public @NotNull Component render(@NotNull Player source, @Nullable Component displayName, @NotNull Component message, @NotNull Audience viewer) {
        Component component = Util.replace(this.format, source);
        message = message.color(color);
        if (source.hasPermission("rainbowchat.color")) {
            // FIXME: 26-05-2021 optimize
            // this transforms the modern message to legacy so we can transform legacy parts to modern
            //                     .replaceText(builder -> builder.match("\\{#[a-fA-F0-9]{6}}").replacement(builder1 -> builder1.content(builder1.content().replace("{#", "&#").replace("}", ""))))
            message = Util.deserialize(Util.serialize(message).replaceAll("\\{#([a-fA-F0-9]{6})}", "&#$1"));
            //message = Util.replaceColor(message);
        }
        if (source.hasPermission("rainbowchat.item")) {
            message = Util.replaceItem(message, source);
        }
        return component.append(message);
    }
}
