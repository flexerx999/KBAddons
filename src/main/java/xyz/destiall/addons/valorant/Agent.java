package xyz.destiall.addons.valorant;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import xyz.destiall.addons.utils.Scheduler;

import java.util.ArrayList;
import java.util.List;

public abstract class Agent implements Listener {
    protected final Player self;
    protected final List<Scheduler.Task> tasks;

    public Agent(Player player) {
        this.self = player;
        tasks = new ArrayList<>();
    }

    public List<Scheduler.Task> getTasks() {
        return tasks;
    }

    public void unset() {}
}
