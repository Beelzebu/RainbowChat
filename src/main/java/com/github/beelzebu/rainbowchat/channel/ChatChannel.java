package com.github.beelzebu.rainbowchat.channel;

import com.github.beelzebu.rainbowchat.RainbowChat;
import com.github.beelzebu.rainbowchat.composer.RainbowComposer;
import com.github.beelzebu.rainbowchat.storage.ChatStorage;
import com.github.beelzebu.rainbowchat.util.Util;
import java.util.List;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public interface ChatChannel {

    @NotNull String getName();

    @NotNull String getCommandName();

    @NotNull String[] getCommandAliases();

    @Nullable Command getCommand();

    @NotNull <T extends Command> T setCommand(@NotNull T command);

    boolean isDefault();

    void setDefault(boolean def);

    @NotNull List<String> getFormats();

    @Nullable String getPermission();

    @NotNull Component getDisplayName();

    @NotNull RainbowComposer getComposer(Player player);

    /**
     * Get the relative audience for this channel with the provided player.
     *
     * @param player Player to get the relative audience.
     * @return The audience in this channel for the requested player.
     */
    @NotNull Audience getAudience(Player player);

    default void sendMessage(Player player, String message) {
        ChatStorage storage = RainbowChat.getPlugin(RainbowChat.class).getStorage();
        ChatChannel oldChannel = storage.getChannel(player);
        if (oldChannel != this) {
            storage.setChannel(player.getUniqueId(), this);
        }
        Audience audience = getAudience(player);
        Component component = getComposer(player).render(player, player.displayName(), Util.deserialize(message), audience);
        audience.sendMessage(component);
        if (oldChannel != this) {
            storage.setChannel(player.getUniqueId(), oldChannel);
        }
    }
}
