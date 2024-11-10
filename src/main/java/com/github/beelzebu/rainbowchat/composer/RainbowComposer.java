package com.github.beelzebu.rainbowchat.composer;

import com.github.beelzebu.rainbowchat.util.Util;
import io.papermc.paper.chat.ChatRenderer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
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
    private final Style style;

    public static @NotNull RainbowComposer load(String name, String permission, int priority, Component format, Style style) {
        RainbowComposer rainbowComposer = new RainbowComposer(name, permission, priority, format, style);
        COMPOSERS.add(rainbowComposer);
        COMPOSERS.sort(Comparator.comparingInt(RainbowComposer::getPriority));
        return rainbowComposer;
    }

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
        this.style = null;
    }

    private RainbowComposer(String name, String permission, int priority, Component format, Style style) {
        this.name = name;
        this.permission = permission;
        this.priority = priority;
        this.format = format;
        this.color = null;
        this.style = style;
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
        return Util.replacePlaceholdersRecursively(this.format, source).append(Util.formatChatMessage(message, source, style, color));
    }

    public TextColor color() {
        return color;
    }

    public Style style() {
        return style;
    }
}
