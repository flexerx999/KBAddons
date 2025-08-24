package xyz.destiall.addons.valorant;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public abstract class Agent implements Listener {
    protected final Player self;
    protected final List<BukkitTask> tasks;

    public Agent(Player player) {
        this.self = player;
        tasks = new ArrayList<>();
    }

    public List<BukkitTask> getTasks() {
        return tasks;
    }

    public void unset() {}
}
