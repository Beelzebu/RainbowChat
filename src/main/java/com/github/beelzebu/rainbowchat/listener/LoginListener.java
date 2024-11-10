package com.github.beelzebu.rainbowchat.listener;

import com.github.beelzebu.rainbowchat.storage.ChatStorage;
import com.github.beelzebu.rainbowchat.storage.MemoryChatStorage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author Beelzebu
 */
public class LoginListener implements Listener {

    private final ChatStorage storage;

    public LoginListener(ChatStorage storage) {
        this.storage = storage;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
        if (storage instanceof MemoryChatStorage) {
            storage.addToChannel(playerJoinEvent.getPlayer(), storage.getDefaultChannel());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent playerQuitEvent) {
        if (storage instanceof MemoryChatStorage) {
            storage.resetChannel(playerQuitEvent.getPlayer());
        }
    }
}
