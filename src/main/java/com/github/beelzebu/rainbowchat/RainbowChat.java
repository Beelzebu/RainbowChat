package com.github.beelzebu.rainbowchat;

import com.github.beelzebu.rainbowchat.channel.ChatChannel;
import com.github.beelzebu.rainbowchat.config.Config;
import com.github.beelzebu.rainbowchat.hook.PluginHook;
import com.github.beelzebu.rainbowchat.hook.TownyHook;
import com.github.beelzebu.rainbowchat.listener.ChatListener;
import com.github.beelzebu.rainbowchat.placeholder.RainbowChatPlaceholders;
import com.github.beelzebu.rainbowchat.storage.ChatStorage;
import com.github.beelzebu.rainbowchat.storage.MemoryChatStorage;
import com.github.beelzebu.rainbowchat.util.Util;
import java.io.File;
import net.nifheim.bukkit.commandlib.CommandAPI;
import net.nifheim.bukkit.commandlib.RegistrableCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RainbowChat extends JavaPlugin {

    public static boolean PLACEHOLDER_API = false;
    private Config chatConfig;
    private MemoryChatStorage storage;
    private PluginHook townyHook;
    private FileConfiguration messages;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PLACEHOLDER_API = true;
            new RainbowChatPlaceholders(this).register();
        }
        this.storage = new MemoryChatStorage();
        chatConfig = new Config(this);
        chatConfig.load();
        saveResource("messages.yml", false);
        messages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        if (chatConfig.isHookEnabled(Config.TOWNY_HOOK) && Bukkit.getPluginManager().getPlugin("Towny") != null) {
            townyHook = new TownyHook(this);
            townyHook.register();
            townyHook.getChannels().getKeys(false).forEach(key -> townyHook.loadChannel(townyHook.getChannels().getConfigurationSection(key), chatConfig));
        }
        for (ChatChannel channel : storage.getChannels()) {
            channel.setCommand(new RegistrableCommand(this, channel.getCommandName(), channel.getPermission(), false) {
                @Override
                public void onCommand(CommandSender sender, String[] strings) {
                    if (sender instanceof Player) {
                        if (strings.length == 0) {
                            sender.sendMessage(Util.deserialize(messages.getString("channel.switch").replace("%channel%", Util.serialize(channel.getDisplayName()))));
                            storage.setChannel(((Player) sender).getUniqueId(), channel);
                        } else {
                            StringBuilder stringBuilder = new StringBuilder();
                            for (String string : strings) {
                                stringBuilder.append(string).append(' ');
                            }
                            channel.sendMessage((Player) sender, stringBuilder.toString());
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onDisable() {
        if (townyHook != null) {
            townyHook.unregister();
        }
        chatConfig.unload();
        CommandAPI.unregister(this);
    }

    @NotNull
    public ChatStorage getStorage() {
        return storage;
    }

    public Config getChatConfig() {
        return chatConfig;
    }

    @Nullable
    public PluginHook getTownyHook() {
        return townyHook;
    }

    public FileConfiguration getMessages() {
        return messages;
    }
}
