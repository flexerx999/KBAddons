package xyz.destiall.addons.utils;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/// Utility class for wrapping the Bukkit scheduler to add support for Folia
public final class Scheduler {
    private final Plugin plugin;
    private boolean folia = false;
    private Set<Task> foliaTasks;
    private boolean isCancellingAll = false;

    public Scheduler(Plugin plugin) {
        this.plugin = plugin;
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
            foliaTasks = ConcurrentHashMap.newKeySet();
            folia = true;
            plugin.getLogger().warning("Using Folia scheduler!");
        } catch (ClassNotFoundException ignored) {
            plugin.getLogger().warning("Using Spigot scheduler!");
        }
    }

    /**
     * Get the owned plugin of this Scheduler
     * @return The owned plugin
     */
    public Plugin getOwningPlugin() {
        return plugin;
    }

    /**
     * Cancel all tasks owned by this scheduler's owning plugin.
     */
    public void cancelTasks() {
        if (isFolia()) {
            isCancellingAll = true;
            List<Task> copy = new ArrayList<>(foliaTasks);
            for (Task task : copy) {
                try {
                    task._internalCancel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            foliaTasks.clear();
            isCancellingAll = false;
            return;
        }

        isCancellingAll = true;
        try {
            plugin.getServer().getScheduler().cancelTasks(plugin);
        } catch (Exception e) {
            e.printStackTrace();
        }
        isCancellingAll = false;
    }

    /**
     * Checks if this scheduler is currently accepting any tasks
     * @return The state of this scheduler
     */
    public boolean isNotAccepting() {
        try {
            return isCancellingAll || plugin.getServer().isStopping() || !plugin.isEnabled();
        } catch (NoSuchMethodError e) {
            return isCancellingAll || !plugin.isEnabled();
        }
    }

    /**
     * Schedules a task to be executed on the global region scheduler on the next tick.
     * @param runnable The task to execute
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTask(Runnable runnable) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getGlobalRegionScheduler().run(plugin, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }));
        }

        return new Task(this, plugin.getServer().getScheduler().runTask(plugin, runnable));
    }

    /**
     * Schedules an async task to be executed on the global region scheduler on the next tick.
     * @param runnable The task to execute
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskAsync(Runnable runnable) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getGlobalRegionScheduler().run(plugin, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    /**
     * Schedules a task to be executed on the region which owns the location on the next tick.
     * @param runnable The task to execute
     * @param location The region to execute the task in
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTask(Runnable runnable, Location location) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getRegionScheduler().run(plugin, location, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }));
        }

        return new Task(this, plugin.getServer().getScheduler().runTask(plugin, runnable));
    }

    /**
     * Schedules an async task to be executed on the region which owns the location on the next tick.
     * @param runnable The task to execute
     * @param location The region to execute the task in
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskAsync(Runnable runnable, Location location) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getRegionScheduler().run(plugin, location, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    /**
     * Schedules a task to be executed on the region which owns the location on the next tick.
     * @param runnable The task to execute
     * @param chunk The chunk to execute the task in
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTask(Runnable runnable, Chunk chunk) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getRegionScheduler().run(plugin, chunk.getWorld(), chunk.getX(), chunk.getZ(),task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }));
        }

        return new Task(this, plugin.getServer().getScheduler().runTask(plugin, runnable));
    }

    /**
     * Schedules an async task to be executed on the region which owns the location on the next tick.
     * @param runnable The task to execute
     * @param chunk The chunk to execute the task in
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskAsync(Runnable runnable, Chunk chunk) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getRegionScheduler().run(plugin, chunk.getWorld(), chunk.getX(), chunk.getZ(),task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    /**
     * Schedules a task to be executed on the region which owns the entity on the next tick.
     * @param runnable The task to execute
     * @param entity The entity to execute the task from
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTask(Runnable runnable, Entity entity) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, entity.getScheduler().run(plugin, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }, null));
        }

        return new Task(this, plugin.getServer().getScheduler().runTask(plugin, runnable));
    }

    /**
     * Schedules an async task to be executed on the region which owns the entity on the next tick.
     * @param runnable The task to execute
     * @param entity The entity to execute the task from
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskAsync(Runnable runnable, Entity entity) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, entity.getScheduler().run(plugin, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }, null));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    /**
     * Schedules a repeated task to be executed on the global region scheduler.
     * @param runnable The task to execute
     * @param delay The delay in ticks from task initialization
     * @param period The period in ticks to execute the task
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskTimer(Runnable runnable, long delay, long period) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> runnable.run(), delay <= 0L ? 1L : delay, period));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period));
    }

    /**
     * Schedules an async repeated task to be executed on the global region scheduler.
     * @param runnable The task to execute
     * @param delay The delay in ticks from task initialization
     * @param period The period in ticks to execute the task
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskTimerAsync(Runnable runnable, long delay, long period) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> runnable.run(), delay <= 0L ? 1L : delay, period));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period));
    }

    /**
     * Schedules a repeated task to be executed on the region which owns the location.
     * @param runnable The task to execute
     * @param location The region to execute the task in
     * @param delay The delay in ticks from task initialization
     * @param period The period in ticks to execute the task
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskTimer(Runnable runnable, Location location, long delay, long period) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getRegionScheduler().runAtFixedRate(plugin, location, task -> runnable.run(), delay <= 0L ? 1L : delay, period));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period));
    }

    /**
     * Schedules an async repeated task to be executed on the region which owns the location.
     * @param runnable The task to execute
     * @param location The region to execute the task in
     * @param delay The delay in ticks from task initialization
     * @param period The period in ticks to execute the task
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskTimerAsync(Runnable runnable, Location location, long delay, long period) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getRegionScheduler().runAtFixedRate(plugin, location, task -> runnable.run(), delay <= 0L ? 1L : delay, period));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period));
    }

    /**
     * Schedules a repeated task to be executed on the region which owns the location.
     * @param runnable The task to execute
     * @param chunk The chunk to execute the task in
     * @param delay The delay in ticks from task initialization
     * @param period The period in ticks to execute the task
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskTimer(Runnable runnable, Chunk chunk, long delay, long period) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getRegionScheduler().runAtFixedRate(plugin, chunk.getWorld(), chunk.getX(), chunk.getZ(), task -> runnable.run(), delay <= 0L ? 1L : delay, period));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period));
    }

    /**
     * Schedules an async repeated task to be executed on the region which owns the location.
     * @param runnable The task to execute
     * @param chunk The chunk to execute the task in
     * @param delay The delay in ticks from task initialization
     * @param period The period in ticks to execute the task
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskTimerAsync(Runnable runnable, Chunk chunk, long delay, long period) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getRegionScheduler().runAtFixedRate(plugin, chunk.getWorld(), chunk.getX(), chunk.getZ(), task -> runnable.run(), delay <= 0L ? 1L : delay, period));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period));
    }

    /**
     * Schedules a repeated task to be executed on the region which owns the entity.
     * @param runnable The task to execute
     * @param entity The entity to execute the task from
     * @param delay The delay in ticks from task initialization
     * @param period The period in ticks to execute the task
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskTimer(Runnable runnable, Entity entity, long delay, long period) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, entity.getScheduler().runAtFixedRate(plugin, task -> runnable.run(), null, delay <= 0L ? 1L : delay, period));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period));
    }

    /**
     * Schedules an async repeated task to be executed on the region which owns the entity.
     * @param runnable The task to execute
     * @param entity The entity to execute the task from
     * @param delay The delay in ticks from task initialization
     * @param period The period in ticks to execute the task
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskTimerAsync(Runnable runnable, Entity entity, long delay, long period) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, entity.getScheduler().runAtFixedRate(plugin, task -> runnable.run(), null, delay <= 0L ? 1L : delay, period));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period));
    }

    /**
     * Schedules a repeated task to be executed on the global region scheduler.
     * @param runnable The task to execute
     * @param delay The delay in ticks from task initialization
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskLater(Runnable runnable, long delay) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }, delay <= 0L ? 1L : delay));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay));
    }

    /**
     * Schedules an async repeated task to be executed on the global region scheduler.
     * @param runnable The task to execute
     * @param delay The delay in ticks from task initialization
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskLaterAsync(Runnable runnable, long delay) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }, delay <= 0L ? 1L : delay));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay));
    }

    /**
     * Schedules a repeated task to be executed on the region which owns the location.
     * @param runnable The task to execute
     * @param location The region to execute the task in
     * @param delay The delay in ticks from task initialization
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskLater(Runnable runnable, Location location, long delay) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getRegionScheduler().runDelayed(plugin, location, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }, delay <= 0L ? 1L : delay));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay));
    }

    /**
     * Schedules an async repeated task to be executed on the region which owns the location.
     * @param runnable The task to execute
     * @param location The region to execute the task in
     * @param delay The delay in ticks from task initialization
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskLaterAsync(Runnable runnable, Location location, long delay) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getRegionScheduler().runDelayed(plugin, location, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }, delay <= 0L ? 1L : delay));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay));
    }

    /**
     * Schedules a repeated task to be executed on the region which owns the location.
     * @param runnable The task to execute
     * @param chunk The chunk to execute the task in
     * @param delay The delay in ticks from task initialization
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskLater(Runnable runnable, Chunk chunk, long delay) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getRegionScheduler().runDelayed(plugin, chunk.getWorld(), chunk.getX(), chunk.getZ(), task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }, delay <= 0L ? 1L : delay));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay));
    }

    /**
     * Schedules a repeated task to be executed on the region which owns the location.
     * @param runnable The task to execute
     * @param chunk The chunk to execute the task in
     * @param delay The delay in ticks from task initialization
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskLaterAsync(Runnable runnable, Chunk chunk, long delay) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, plugin.getServer().getRegionScheduler().runDelayed(plugin, chunk.getWorld(), chunk.getX(), chunk.getZ(), task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }, delay <= 0L ? 1L : delay));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay));
    }

    /**
     * Schedules a delayed task to be executed on the region which owns the entity.
     * @param runnable The task to execute
     * @param entity The entity to execute the task from
     * @param delay The delay in ticks from task initialization
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskLater(Runnable runnable, Entity entity, long delay) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, entity.getScheduler().runDelayed(plugin, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }, null, delay <= 0L ? 1L : delay));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay));
    }

    /**
     * Schedules an async delayed task to be executed on the region which owns the entity.
     * @param runnable The task to execute
     * @param entity The entity to execute the task from
     * @param delay The delay in ticks from task initialization
     * @return The scheduled task wrapper for Bukkit or Folia
     */
    public Task runTaskLaterAsync(Runnable runnable, Entity entity, long delay) {
        if (isFolia()) {
            if (isNotAccepting())
                throw new RuntimeException("Unable to schedule a task while cancelTasks() is being called!");

            return new Task(this, entity.getScheduler().runDelayed(plugin, task -> {
                runnable.run();
                this.foliaTasks.removeIf(t -> t.foliaTask == task);
            }, null, delay <= 0L ? 1L : delay));
        }

        return new Task(this, plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay));
    }

    /**
     * If the current server implementation is running Folia
     * @return true if Folia is underneath
     */
    public boolean isFolia() {
        return folia;
    }

    public static final class Task {
        private static final AtomicInteger IDS = new AtomicInteger(0);
        private final int externalId = IDS.incrementAndGet();
        private final Scheduler owningScheduler;
        private BukkitTask bukkitTask = null;
        private ScheduledTask foliaTask = null;

        private Task(Scheduler owningScheduler, BukkitTask bukkitTask) {
            this.owningScheduler = owningScheduler;
            this.bukkitTask = bukkitTask;
        }

        private Task(Scheduler owningScheduler, ScheduledTask foliaTask) {
            this.owningScheduler = owningScheduler;
            this.foliaTask = foliaTask;

            owningScheduler.foliaTasks.add(this);
        }

        /**
         * Get the Scheduler that scheduled this task
         * @return The scheduler that scheduled this task
         */
        public Scheduler getScheduler() {
            return owningScheduler;
        }

        /**
         * If this task is being executed as Folia.
         * @return true if Folia is underneath
         */
        public boolean isFolia() {
            return bukkitTask == null;
        }

        /**
         * Get the external ID to identify tasks
         * @return the ID
         */
        public int getExternalId() {
            return externalId;
        }

        /**
         * If this task is being run asynchronously.
         * @return true by default if on Folia.
         */
        public boolean isAsync() {
            if (isFolia()) {
                return true;
            }

            return !bukkitTask.isSync();
        }

        /**
         * Get the owned plugin that executed this task
         * @return The owned plugin
         */
        public Plugin getOwnedPlugin() {
            if (isFolia()) {
                return foliaTask.getOwningPlugin();
            }

            return bukkitTask.getOwner();
        }

        /**
         * Cancel this task from further execution. If this task is a timer or delayed task, it will halt.
         */
        public void cancel() {
            if (isFolia()) {
                this.owningScheduler.foliaTasks.remove(this);
            }

            _internalCancel();
        }

        private void _internalCancel() {
            if (isFolia()) {
                foliaTask.cancel();
                return;
            }

            bukkitTask.cancel();
        }

        /**
         * If this task has been cancelled
         * @return Cancellation state of the task
         */
        public boolean isCancelled() {
            if (isFolia()) {
                return foliaTask.isCancelled();
            }

            return bukkitTask.isCancelled();
        }
    }

    /// Wrapper for the BukkitRunnable
    public static abstract class TaskRunnable implements Runnable {
        private Task task;
        public synchronized void cancel() throws IllegalStateException {
            if (task == null)
                throw new IllegalStateException("This runnable has not yet been scheduled!");

            task.cancel();
        }

        public final Task runTask(Scheduler ownedScheduler, Entity entity) {
            return task = ownedScheduler.runTask(this, entity);
        }

        public final Task runTask(Scheduler ownedScheduler, Location location) {
            return task = ownedScheduler.runTask(this, location);
        }

        public final Task runTask(Scheduler ownedScheduler, Chunk chunk) {
            return task = ownedScheduler.runTask(this, chunk);
        }

        public final Task runTask(Scheduler ownedScheduler) {
            return task = ownedScheduler.runTask(this);
        }

        public final Task runTaskTimer(Scheduler ownedScheduler, Entity entity, long delay, long period) {
            return task = ownedScheduler.runTaskTimer(this, entity, delay, period);
        }

        public final Task runTaskTimer(Scheduler ownedScheduler, Location location, long delay, long period) {
            return task = ownedScheduler.runTaskTimer(this, location, delay, period);
        }

        public final Task runTaskTimer(Scheduler ownedScheduler, Chunk chunk, long delay, long period) {
            return task = ownedScheduler.runTaskTimer(this, chunk, delay, period);
        }

        public final Task runTaskTimer(Scheduler ownedScheduler, long delay, long period) {
            return task = ownedScheduler.runTaskTimer(this, delay, period);
        }

        public final Task runTaskLater(Scheduler ownedScheduler, Entity entity, long delay) {
            return task = ownedScheduler.runTaskLater(this, entity, delay);
        }

        public final Task runTaskLater(Scheduler ownedScheduler, Location location, long delay) {
            return task = ownedScheduler.runTaskLater(this, location, delay);
        }

        public final Task runTaskLater(Scheduler ownedScheduler, Chunk chunk, long delay) {
            return task = ownedScheduler.runTaskLater(this, chunk, delay);
        }

        public final Task runTaskLater(Scheduler ownedScheduler, long delay) {
            return task = ownedScheduler.runTaskLater(this, delay);
        }

        public final int getExternalId() {
            if (task == null)
                throw new IllegalStateException("This runnable has not yet been scheduled!");

            return task.getExternalId();
        }
    }
}