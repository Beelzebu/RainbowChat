package com.github.beelzebu.rainbowchat.channel;

import java.util.List;
import java.util.function.Function;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public final class DynamicAudienceChatChannel extends BaseChatChannel implements ChatChannel {

    private @NotNull Function<Player, Audience> audienceFunction;

    public DynamicAudienceChatChannel(@NotNull String name, @NotNull String commandName, @NotNull String[] aliases, @NotNull Component displayName, @NotNull List<String> formats, @NotNull Function<Player, Audience> audienceFunction, boolean chatHologram) {
        super(name, commandName, aliases, displayName, formats, chatHologram);
        this.audienceFunction = audienceFunction;
    }

    @Override
    public @Nullable String getPermission() {
        return null;
    }

    @Override
    public @NotNull Audience getAudience(Player player) {
        return Audience.audience(Bukkit.getConsoleSender(), audienceFunction.apply(player));
    }

    public void setAudienceFunction(@NotNull Function<Player, Audience> audienceFunction) {
        this.audienceFunction = audienceFunction;
    }
}
