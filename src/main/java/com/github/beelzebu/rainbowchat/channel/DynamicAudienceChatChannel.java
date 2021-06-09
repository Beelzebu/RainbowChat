package com.github.beelzebu.rainbowchat.channel;

import com.github.beelzebu.rainbowchat.composer.RainbowComposer;
import java.util.List;
import java.util.function.Function;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public final class DynamicAudienceChatChannel implements ChatChannel {

    private final @NotNull String name;
    private final @NotNull String commandName;
    private final @NotNull String[] aliases;
    private boolean def;
    private final @NotNull Component displayName;
    private final @NotNull List<String> formats;
    private @NotNull Function<Player, Audience> audienceFunction;
    private @Nullable Command command;

    public DynamicAudienceChatChannel(@NotNull String name, @NotNull String commandName, @NotNull String[] aliases, @NotNull Component displayName, @NotNull List<String> formats, @NotNull Function<Player, Audience> audienceFunction) {
        this.name = name;
        this.commandName = commandName;
        this.aliases = aliases;
        this.displayName = displayName;
        this.formats = formats;
        this.audienceFunction = audienceFunction;
    }


    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull String getCommandName() {
        return commandName;
    }

    @Override
    public @NotNull String[] getCommandAliases() {
        return aliases;
    }

    @Override
    public boolean isDefault() {
        return def;
    }

    @Override
    public void setDefault(boolean def) {
        this.def = def;
    }

    @Override
    public @NotNull List<String> getFormats() {
        return formats;
    }

    @Override
    public @Nullable String getPermission() {
        return null;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return displayName;
    }

    @Override
    public @NotNull RainbowComposer getComposer(@NotNull Player player) {
        for (RainbowComposer composer : RainbowComposer.COMPOSERS) {
            if (this.formats.contains(composer.getName()) && player.hasPermission(composer.getPermission())) {
                return composer;
            }
        }
        return RainbowComposer.COMPOSERS.get(RainbowComposer.COMPOSERS.size() - 1);
    }

    @Override
    public @NotNull Audience getAudience(Player player) {
        return Audience.audience(Bukkit.getConsoleSender(), audienceFunction.apply(player));
    }

    public void setAudienceFunction(@NotNull Function<Player, Audience> audienceFunction) {
        this.audienceFunction = audienceFunction;
    }

    @Override
    public @Nullable Command getCommand() {
        return command;
    }

    public @NotNull <T extends Command> T setCommand(@NotNull T command) {
        this.command = command;
        return command;
    }
}
