package com.github.beelzebu.rainbowchat.listener;

import com.github.beelzebu.rainbowchat.RainbowChat;
import com.github.beelzebu.rainbowchat.channel.ChatChannel;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class ChatListener implements Listener {

    private final RainbowChat plugin;

    public ChatListener(RainbowChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(@NotNull AsyncChatEvent event) {
        if (event.isCancelled()) { // other plugin cancelled the event before, so skip formatting
            return;
        }
        event.setCancelled(true); // don't fire the chat event, so we can properly use the channel audience
        Player player = event.getPlayer();
        ChatChannel chatChannel = plugin.getStorage().getChannel(player);
        Audience audience = chatChannel.getAudience(player);
        Component component = chatChannel.getComposer(player).render(player, player.displayName(), event.message(), audience);
        audience.sendMessage(component);
    }
}
