package com.github.beelzebu.rainbowchat.channel;

import com.github.beelzebu.rainbowchat.composer.RainbowComposer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public final class SimpleChatChannel implements ChatChannel {

    public static final SimpleChatChannel DEFAULT_CHANNEL = new SimpleChatChannel("default", "g", new String[0], true, Component.text("default"), RainbowComposer.COMPOSERS.stream().map(RainbowComposer::getName).collect(Collectors.toList()), null);

    private final @NotNull String name;
    private final @NotNull String commandName;
    private final @NotNull String[] aliases;
    private boolean def;
    private final @NotNull Component displayName;
    private final @NotNull List<String> formats;
    private final @Nullable String permission;
    private @Nullable Command command;

    public SimpleChatChannel(@NotNull String name, @NotNull String commandName, @NotNull String[] aliases, boolean def, @NotNull Component displayName, @NotNull List<String> formats, @Nullable String permission) {
        this.name = name;
        this.commandName = commandName;
        this.aliases = aliases;
        this.def = def;
        this.displayName = displayName;
        if (formats.isEmpty()) {
            throw new IllegalArgumentException("Empty format list");
        }
        Iterator<String> it = formats.iterator();
        while (it.hasNext()) {
            String formatName = it.next();
            boolean exists = false;
            for (RainbowComposer format : RainbowComposer.COMPOSERS) {
                if (Objects.equals(format.getName(), formatName)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                it.remove();
            }
        }
        this.formats = formats;
        this.permission = permission;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull String getCommandName() {
        return commandName;
    }

    @Override
    public @NotNull String[] getCommandAliases() {
        return aliases;
    }

    @Override
    public boolean isDefault() {
        return def;
    }

    @Override
    public void setDefault(boolean def) {
        this.def = def;
    }

    @Override
    public @NotNull List<String> getFormats() {
        return formats;
    }

    @Override
    public @Nullable String getPermission() {
        return permission;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return displayName;
    }

    @Override
    public @NotNull RainbowComposer getComposer(@NotNull Player player) {
        for (RainbowComposer composer : RainbowComposer.COMPOSERS) {
            if (this.formats.contains(composer.getName()) && player.hasPermission(composer.getPermission())) {
                return composer;
            }
        }
        return RainbowComposer.COMPOSERS.get(RainbowComposer.COMPOSERS.size() - 1);
    }

    @Override
    public @NotNull Audience getAudience(Player audience) {
        if (def) {
            return Bukkit.getServer();
        } else {
            Set<Audience> members = new HashSet<>();
            members.add(Bukkit.getConsoleSender());
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (getPermission() == null || player.hasPermission(getPermission())) {
                    members.add(player);
                }
            }
            return Audience.audience(members);
        }
    }

    @Nullable
    @Override
    public Command getCommand() {
        return command;
    }

    public @NotNull Command setCommand(@NotNull Command command) {
        this.command = command;
        return command;
    }
}
