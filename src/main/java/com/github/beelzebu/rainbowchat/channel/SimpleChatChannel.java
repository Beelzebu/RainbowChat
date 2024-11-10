package com.github.beelzebu.rainbowchat.channel;

import com.github.beelzebu.rainbowchat.composer.RainbowComposer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public final class SimpleChatChannel extends BaseChatChannel implements ChatChannel {

    public static final SimpleChatChannel DEFAULT_CHANNEL = new SimpleChatChannel("default", "g", new String[0], true, Component.text("default"), RainbowComposer.COMPOSERS.stream().map(RainbowComposer::getName).collect(Collectors.toList()), null, true);

    private final @Nullable String permission;

    public SimpleChatChannel(@NotNull String name, @NotNull String commandName, @NotNull String[] aliases, boolean def, @NotNull Component displayName, @NotNull List<String> formats, @Nullable String permission, boolean chatHologram) {
        super(name, commandName, aliases, displayName, formats, chatHologram);
        this.setDefault(def);
        this.permission = permission;
    }

    @Override
    public @Nullable String getPermission() {
        return permission;
    }

    @Override
    public @NotNull Audience getAudience(Player player) {
        if (isDefault() || getPermission() == null) {
            return Bukkit.getServer();
        } else {
            Set<Audience> members = new HashSet<>();
            members.add(Bukkit.getConsoleSender());
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission(getPermission())) {
                    members.add(onlinePlayer);
                }
            }
            return Audience.audience(members);
        }
    }
}
