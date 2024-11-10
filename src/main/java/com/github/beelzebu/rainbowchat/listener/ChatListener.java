package com.github.beelzebu.rainbowchat.listener;

import com.github.beelzebu.rainbowchat.RainbowChat;
import com.github.beelzebu.rainbowchat.channel.ChatChannel;
import com.github.beelzebu.rainbowchat.composer.RainbowComposer;
import com.github.beelzebu.rainbowchat.util.Util;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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

    @EventHandler(priority = EventPriority.MONITOR)//, ignoreCancelled = true)
    public void onPlayerChat(@NotNull AsyncChatEvent event) {
        Player player = event.getPlayer();
        ChatChannel chatChannel = plugin.getStorage().getChannel(player);
        Audience audience = chatChannel.getAudience(player);
        event.viewers().clear();
        event.viewers().add(audience);
        RainbowComposer renderer = chatChannel.getComposer(player);
        event.renderer(renderer);
        if (chatChannel.isChatHologram()) {
            Bukkit.getScheduler().runTask(plugin, () -> plugin.getHologramUtil().createHologram(player.getEyeLocation().add(0, .9, 0), player.name().append(Component.text(": ")).append(Util.formatChatMessage(event.message(), player, renderer.style(), renderer.color()))));
        }
    }
}
