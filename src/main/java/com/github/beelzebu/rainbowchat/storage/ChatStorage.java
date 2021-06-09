package com.github.beelzebu.rainbowchat.storage;

import com.github.beelzebu.rainbowchat.channel.ChatChannel;
import java.util.Collection;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public interface ChatStorage {

    @NotNull ChatChannel getChannel(Player player);

    void setChannel(UUID uniqueId, ChatChannel chatChannel);

    void loadChannel(ChatChannel chatChannel);

    Collection<ChatChannel> getChannels();

    void clearChannels();
}
