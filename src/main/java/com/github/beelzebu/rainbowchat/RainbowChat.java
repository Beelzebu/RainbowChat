package com.github.beelzebu.rainbowchat;

import com.github.beelzebu.rainbowchat.channel.ChatChannel;
import com.github.beelzebu.rainbowchat.config.Config;
import com.github.beelzebu.rainbowchat.hook.BaseHook;
import com.github.beelzebu.rainbowchat.hook.HookType;
import com.github.beelzebu.rainbowchat.hook.TownyHook;
import com.github.beelzebu.rainbowchat.listener.ChatListener;
import com.github.beelzebu.rainbowchat.listener.LoginListener;
import com.github.beelzebu.rainbowchat.placeholder.RainbowChatPlaceholders;
import com.github.beelzebu.rainbowchat.storage.ChatStorage;
import com.github.beelzebu.rainbowchat.storage.MemoryChatStorage;
import com.github.beelzebu.rainbowchat.util.HologramUtil;
import com.github.beelzebu.rainbowchat.util.Util;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.nifheim.bukkit.commandlib.CommandAPI;
import net.nifheim.bukkit.commandlib.RegistrableCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class RainbowChat extends JavaPlugin {

    public static boolean PLACEHOLDER_API = false;
    private final Map<HookType, BaseHook> hooks = new HashMap<>();
    private Config chatConfig;
    private MemoryChatStorage storage;
    private FileConfiguration messages;
    private HologramUtil hologramUtil;

    @Override
    public void onEnable() {
        hologramUtil = new HologramUtil(this);
        saveDefaultConfig();
        this.storage = new MemoryChatStorage();
        Bukkit.getPluginManager().registerEvents(new LoginListener(storage), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PLACEHOLDER_API = true;
            new RainbowChatPlaceholders(this).register();
        }
        chatConfig = new Config(this);
        chatConfig.load();
        saveResource("messages.yml", false);
        messages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        reloadHooks();
        loadChannels();
        new RegistrableCommand(this, "rainbowchat", "rainbowchat.admin", false) {

            @Override
            public void onCommand(CommandSender sender, String label, String[] args) {
                if (args.length == 0) {
                    sender.sendMessage(Component.text("RainbowChat v" + RainbowChat.this.getDescription().getVersion()).color(TextColor.color(0xD51750)));
                } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                    CommandAPI.unregister(RainbowChat.this);
                    chatConfig.reload();
                    reloadHooks();
                    loadChannels();
                    sender.sendMessage(Component.text("Plugin reloaded").color(TextColor.color(0x88FF00)));
                }
                try {
                    Class.forName("org.bukkit.craftbukkit.v1_19_R1.CraftServer").getMethod("syncCommands").invoke(Bukkit.getServer());
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Override
    public void onDisable() {
        if (hologramUtil != null) {
            hologramUtil.clearHolograms();
        }
        chatConfig.unload();
        CommandAPI.unregister(this);
    }

    private void loadChannels() {
        for (ChatChannel channel : storage.getChannels()) {
            channel.setCommand(new RegistrableCommand(this, channel.getCommandName(), channel.getPermission(), false) {
                @Override
                public void onCommand(CommandSender sender, String label, String[] strings) {
                    if (sender instanceof Player player) {
                        if (strings.length == 0) {
                            player.sendMessage(Util.deserialize(messages.getString("channel.switch", "").replace("%channel%", Util.serialize(channel.getDisplayName()))));
                            storage.addToChannel(player, channel);
                        } else {
                            StringBuilder stringBuilder = new StringBuilder();
                            for (String string : strings) {
                                stringBuilder.append(string).append(' ');
                            }
                            channel.sendMessage(player, stringBuilder.toString());
                        }
                    }
                }
            });
        }
    }

    @NotNull
    public ChatStorage getStorage() {
        return storage;
    }

    public Config getChatConfig() {
        return chatConfig;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public void reloadHooks() {
        for (HookType hook : HookType.values()) {
            if (chatConfig.isHookEnabled(hook)) {
                if (hook.isPlugin() && Bukkit.getPluginManager().getPlugin(hook.getName()) == null) {
                    continue;
                }
                loadHook(hook);
            } else {
                unloadHook(hook);
            }
        }
    }

    public HologramUtil getHologramUtil() {
        return hologramUtil;
    }

    private void loadHook(HookType hookType) {
        unloadHook(hookType);
        BaseHook hook = null;
        switch (hookType) {
            case TOWNY:
                hook = new TownyHook(this);
                break;
            default:
                break;
        }
        if (hook != null) {
            getLogger().info("Registering hook: " + hookType);
            hook.register();
            hooks.put(hookType, hook);
        }
    }

    private void unloadHook(HookType hook) {
        BaseHook pluginHook = hooks.get(hook);
        if (pluginHook != null) {
            pluginHook.unregister();
            hooks.remove(hook);
        }
    }
}
