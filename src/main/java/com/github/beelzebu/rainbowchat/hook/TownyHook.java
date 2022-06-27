package com.github.beelzebu.rainbowchat.hook;

import com.github.beelzebu.rainbowchat.RainbowChat;
import com.github.beelzebu.rainbowchat.channel.ChatChannel;
import com.github.beelzebu.rainbowchat.channel.DynamicAudienceChatChannel;
import com.github.beelzebu.rainbowchat.channel.SimpleChatChannel;
import com.github.beelzebu.rainbowchat.composer.RainbowComposer;
import com.github.beelzebu.rainbowchat.util.Util;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.ResidentList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.nifheim.bukkit.commandlib.CommandAPI;
import net.nifheim.bukkit.commandlib.RegistrableCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public final class TownyHook extends Hook {

    public TownyHook(RainbowChat plugin) {
        super(plugin);
    }

    @Override
    public void register() {
        if (plugin.getStorage().getChannels().stream().noneMatch(chatChannel -> chatChannel.getName().equals("general"))) {
            plugin.getStorage().loadChannel(SimpleChatChannel.DEFAULT_CHANNEL);
        }
        ChatChannel townChannel = plugin.getStorage().getChannels().stream().filter(chatChannel ->
                chatChannel.getName().equals("town")).findFirst().orElseGet(() ->
                new DynamicAudienceChatChannel(
                        "town",
                        "tc",
                        new String[]{"townchat"},
                        Component.text("town"),
                        RainbowComposer.COMPOSERS.stream().map(RainbowComposer::getName).collect(Collectors.toList()),
                        getTownAudience())
        );
        if (townChannel instanceof DynamicAudienceChatChannel) {
            ((DynamicAudienceChatChannel) townChannel).setAudienceFunction(getTownAudience());
        } else {
            plugin.getStorage().loadChannel(new DynamicAudienceChatChannel(townChannel.getName(), townChannel.getCommandName(), townChannel.getCommandAliases(), townChannel.getDisplayName(), townChannel.getFormats(), getTownAudience()));
        }
        ChatChannel nationChannel = plugin.getStorage().getChannels().stream().filter(chatChannel ->
                chatChannel.getName().equals("nation")).findFirst().orElseGet(() ->
                new DynamicAudienceChatChannel(
                        "nation",
                        "nc",
                        new String[]{"nationchat"},
                        Component.text("nation"),
                        RainbowComposer.COMPOSERS.stream().map(RainbowComposer::getName).collect(Collectors.toList()),
                        getNationAudience())
        );
        if (nationChannel instanceof DynamicAudienceChatChannel) {
            ((DynamicAudienceChatChannel) nationChannel).setAudienceFunction(getNationAudience());
        } else {
            plugin.getStorage().loadChannel(new DynamicAudienceChatChannel(nationChannel.getName(), nationChannel.getCommandName(), nationChannel.getCommandAliases(), nationChannel.getDisplayName(), nationChannel.getFormats(), getNationAudience()));
        }
        if (townChannel.getCommand() != null) {
            CommandAPI.unregisterCommand(plugin, townChannel.getCommand());
        }
        townChannel.setCommand(buildCommand(townChannel));
        if (nationChannel.getCommand() != null) {
            CommandAPI.unregisterCommand(plugin, nationChannel.getCommand());
        }
        nationChannel.setCommand(buildCommand(nationChannel));
        loadChannel("town", getTownAudience());
        loadChannel("nation", getNationAudience());
    }

    @Override
    public void unregister() {
    }

    @Override
    public ConfigurationSection getChannelsConfig() {
        return plugin.getConfig().getConfigurationSection("hooks.towny.channels");
    }

    private @NotNull Audience getAudience(@Nullable ResidentList residentList) {
        if (residentList == null) {
            return Audience.empty();
        }
        if (residentList.getResidents().isEmpty()) {
            return Audience.empty();
        }
        Set<Player> playerSet = new HashSet<>();
        for (Resident resident : residentList.getResidents()) {
            Player player = resident.getPlayer();
            if (player != null) {
                playerSet.add(player);
            }
        }
        return Audience.audience(playerSet);
    }

    private @NotNull Function<Player, Audience> getTownAudience() {
        return player -> {
            try {
                Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
                if (resident != null && resident.hasTown()) {
                    return getAudience(resident.getTown());
                } else {
                    player.sendMessage(Util.deserialize(plugin.getMessages().getString("towny.no-town")));
                    return Audience.empty();
                }
            } catch (NotRegisteredException e) {
                e.printStackTrace();
            }
            player.sendMessage(Util.deserialize(plugin.getMessages().getString("channel.no-audience")));
            return Audience.empty();
        };
    }

    private @NotNull Function<Player, Audience> getNationAudience() {
        return player -> {
            try {
                Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
                if (resident != null && resident.hasNation()) {
                    return getAudience(resident.getTown().getNation());
                } else {
                    player.sendMessage(Util.deserialize(plugin.getMessages().getString("towny.no-nation")));
                    return Audience.empty();
                }
            } catch (NotRegisteredException e) {
                e.printStackTrace();
            }
            player.sendMessage(Util.deserialize(plugin.getMessages().getString("channel.no-audience")));
            return Audience.empty();
        };
    }

    private RegistrableCommand buildCommand(ChatChannel channel) {
        return new RegistrableCommand(plugin, channel.getCommandName(), channel.getPermission(), false) {
            @Override
            public void onCommand(CommandSender sender, String label, String[] args) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
                    if (channel.getName().equals("town") && (resident == null || !resident.hasTown())) {
                        sender.sendMessage(Util.deserialize(plugin.getMessages().getString("towny.no-town")));
                        return;
                    }
                    if (channel.getName().equals("nation") && (resident == null || !resident.hasNation())) {
                        sender.sendMessage(Util.deserialize(plugin.getMessages().getString("towny.no-nation")));
                        return;
                    }
                    if (args.length == 0) {
                        player.sendMessage(Util.deserialize(plugin.getMessages().getString("channel.switch").replace("%channel%", Util.serialize(channel.getDisplayName()))));
                        plugin.getStorage().setChannel(player.getUniqueId(), channel);
                    } else {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (String string : args) {
                            stringBuilder.append(string).append(' ');
                        }
                        channel.sendMessage(player, stringBuilder.toString());
                    }
                }
            }
        };
    }
}
