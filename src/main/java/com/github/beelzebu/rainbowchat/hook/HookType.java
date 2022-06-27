package com.github.beelzebu.rainbowchat.hook;

public enum HookType {

    TOWNY("Towny", true);

    private final String name;
    private final boolean plugin;

    HookType(String name, boolean plugin) {
        this.name = name;
        this.plugin = plugin;
    }

    public String getName() {
        return name;
    }

    public boolean isPlugin() {
        return plugin;
    }
}
