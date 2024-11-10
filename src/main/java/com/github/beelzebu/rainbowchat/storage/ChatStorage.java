package com.github.beelzebu.rainbowchat.storage;

import com.github.beelzebu.rainbowchat.channel.ChatChannel;
import java.util.Collection;
import net.kyori.adventure.identity.Identified;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public interface ChatStorage {

    @NotNull ChatChannel getChannel(Player player);

    @Nullable ChatChannel getChannelByName(String channel);

    @NotNull ChatChannel getDefaultChannel();

    void addToChannel(Identified identified, ChatChannel chatChannel);

    void resetChannel(Identified identified);

    void loadChannel(ChatChannel chatChannel);

    Collection<ChatChannel> getChannels();

    void clearChannels();
}
