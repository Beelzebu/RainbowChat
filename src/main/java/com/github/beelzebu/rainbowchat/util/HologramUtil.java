package com.github.beelzebu.rainbowchat.util;

import com.github.beelzebu.rainbowchat.RainbowChat;
import java.util.HashSet;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

public final class HologramUtil {

    private final RainbowChat plugin;
    private final Set<ArmorStand> holograms = new HashSet<>();

    public HologramUtil(RainbowChat plugin) {
        this.plugin = plugin;
        Bukkit.getScheduler().runTaskTimer(plugin, () -> holograms.forEach(hologram -> hologram.teleportAsync(hologram.getLocation().add(0, 0.015, 0))), 0, 1);
    }

    public ArmorStand createHologram(Location location, Component text) {
        ArmorStand hologram = location.getWorld().spawn(location, ArmorStand.class, entity -> {
            entity.getLocation().getChunk().addPluginChunkTicket(plugin);
            entity.customName(text);
            entity.setCustomNameVisible(true);
            entity.setGravity(false);
            entity.setInvulnerable(true);
            entity.setMarker(true);
            entity.setSilent(true);
            entity.setVisible(false);
            entity.setCollidable(false);
            entity.setRemoveWhenFarAway(true);
            entity.setCanPickupItems(false);
            entity.setGlowing(false);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                entity.remove();
                entity.getLocation().getChunk().removePluginChunkTicket(plugin);
            }, 80L);
        });
        holograms.add(hologram);
        return hologram;
    }

    public void clearHolograms() {
        holograms.forEach(ArmorStand::remove);
        holograms.clear();
    }
}
