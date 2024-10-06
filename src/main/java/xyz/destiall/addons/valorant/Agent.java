package xyz.destiall.addons.valorant;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class Agent implements Listener {
    protected final Player player;

    public Agent(Player player) {
        this.player = player;
    }
}
