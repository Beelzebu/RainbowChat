package com.github.beelzebu.rainbowchat.channel;

import com.github.beelzebu.rainbowchat.composer.RainbowComposer;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public abstract class AbstractChatChannel implements ChatChannel {

    private final @NotNull String name;
    private final @NotNull String commandName;
    private final @NotNull String[] aliases;
    private boolean def;
    private final @NotNull Component displayName;
    private final @NotNull List<String> formats;
    private @Nullable Command command;

    protected AbstractChatChannel(@NotNull String name, @NotNull String commandName, @NotNull String[] aliases, @NotNull Component displayName, @NotNull List<String> formats) {
        this.name = name;
        this.commandName = commandName;
        this.aliases = aliases;
        this.displayName = displayName;
        this.formats = formats;
        if (formats.isEmpty()) {
            throw new IllegalArgumentException("Empty format list");
        }
        Iterator<String> it = formats.iterator();
        while (it.hasNext()) {
            String formatName = it.next();
            boolean exists = false;
            for (RainbowComposer format : RainbowComposer.COMPOSERS) {
                if (Objects.equals(format.getName(), formatName)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                it.remove();
            }
        }
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return commandName;
    }

    @NotNull
    @Override
    public Component getDisplayName() {
        return displayName;
    }

    @NotNull
    @Override
    public List<String> getFormats() {
        return formats;
    }

    @Nullable
    @Override
    public Command getCommand() {
        return command;
    }

    @Override
    public @NotNull String[] getCommandAliases() {
        return aliases;
    }

    @Override
    public <T extends Command> @NotNull T setCommand(@NotNull T command) {
        this.command = command;
        return command;
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
    public @NotNull RainbowComposer getComposer(@NotNull Player player) {
        for (RainbowComposer composer : RainbowComposer.COMPOSERS) {
            if (this.formats.contains(composer.getName()) && player.hasPermission(composer.getPermission())) {
                return composer;
            }
        }
        return RainbowComposer.COMPOSERS.get(RainbowComposer.COMPOSERS.size() - 1);
    }

    public @NotNull RainbowComposer getComposer(String name) {
        for (RainbowComposer composer : RainbowComposer.COMPOSERS) {
            if (this.formats.contains(composer.getName()) && Objects.equals(composer.getName(), name)) {
                return composer;
            }
        }
        return RainbowComposer.COMPOSERS.get(RainbowComposer.COMPOSERS.size() - 1);
    }

    @Override
    public void sendMessage(Player player, String message) {
        ChatChannel.super.sendMessage(player, message);
    }
}
