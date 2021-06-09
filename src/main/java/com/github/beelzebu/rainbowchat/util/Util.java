package com.github.beelzebu.rainbowchat.util;

import com.github.beelzebu.rainbowchat.RainbowChat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class Util {

    private static final LegacyComponentSerializer COMPONENT_SERIALIZER = LegacyComponentSerializer.builder()
            .hexColors()
            .extractUrls()
            .character(LegacyComponentSerializer.SECTION_CHAR)
            .build();

    public static TextComponent deserialize(String input) {
        return COMPONENT_SERIALIZER.deserialize(input.replace('&', '§'));
    }

    public static String serialize(Component input) {
        return COMPONENT_SERIALIZER.serialize(input);
    }

    public static @NotNull Component replace(@NotNull Component component, final Player source) {
        component = replaceColor(component);
        if (RainbowChat.PLACEHOLDER_API) {
            List<Component> children = new ArrayList<>();
            for (int i = 0; i < component.children().size(); i++) {
                Component child = component.children().get(i);
                children.add(i, replace(child, source));
            }
            component = component.children(children);
            HoverEvent<Component> hoverEvent = (HoverEvent<Component>) component.hoverEvent();
            ClickEvent clickEvent = component.clickEvent();
            component = component.replaceText(builder -> builder.match(".{0,}\\%+.+").replacement(text -> deserialize(PlaceholderAPI.setPlaceholders(source, COMPONENT_SERIALIZER.serialize(text.content(text.content().replace(LegacyComponentSerializer.AMPERSAND_CHAR, LegacyComponentSerializer.SECTION_CHAR)).asComponent())))));
            if (hoverEvent != null) {
                component = component.hoverEvent(hoverEvent.value().replaceText(builder -> builder.match(".{0,}\\%+.+").replacement(text -> deserialize(PlaceholderAPI.setPlaceholders(source, text.content())))));
            }
            if (clickEvent != null) {
                component = component.clickEvent(ClickEvent.clickEvent(clickEvent.action(), PlaceholderAPI.setPlaceholders(source, clickEvent.value())));
            }
        }
        return component;
    }

    public static @NotNull Component replaceColor(@NotNull Component component) {
        List<Component> children = new ArrayList<>();
        for (int i = 0; i < component.children().size(); i++) {
            Component child = component.children().get(i);
            children.add(i, replaceColor(child));
        }
        component = component.children(children);
        component = component
                .replaceText(builder -> builder.match("&[\\da-fA-F]")
                        .replacement((matchResult, textComponent) -> textComponent.content(textComponent.content().replaceAll("&[\\da-fA-F]", "")).mergeStyle(Util.deserialize(matchResult.group()))))
                .replaceText(builder -> builder.match("\\{#([a-fA-F0-9]{6})}")
                        .replacement("&#$1"))
                .replaceText(builder -> builder.match("&#[\\da-fA-F]{6}")
                        .replacement((matchResult, textComponent) -> textComponent.content(textComponent.content().replaceAll("&#[\\da-fA-F]{6}", "")).mergeStyle(Util.deserialize(matchResult.group()))));
        return component;
    }

    public static @NotNull Component replaceItem(@NotNull Component component, Player player) {
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack.getType().isAir()) {
            return component;
        }
        List<Component> children = new ArrayList<>();
        for (int i = 0; i < component.children().size(); i++) {
            Component child = component.children().get(i);
            children.add(i, replaceColor(child));
        }
        Component displayName = Objects.requireNonNullElse(itemStack.getItemMeta().displayName(), itemStack.displayName()).append(Component.text(" (x" + itemStack.getAmount() + ")").color(NamedTextColor.AQUA));
        component = component.children(children);
        component = component.replaceText(builder -> builder.match("(\\[(i|item)\\]|@(item|i))").replacement(Component.empty().append((displayName.hoverEvent(itemStack)))));
        return component;
    }
}
