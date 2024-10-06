package xyz.destiall.addons.managers;

import com.gmail.filoghost.holographicdisplays.api.line.HologramLine;
import com.gmail.filoghost.holographicdisplays.api.line.ItemLine;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.gmail.filoghost.holographicdisplays.object.NamedHologram;
import com.gmail.filoghost.holographicdisplays.object.NamedHologramManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import xyz.destiall.addons.Addons;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HologramManager {
    private static HashMap<UUID, NamedHologram> selectedHolograms;
    private static List<NamedHologram> holograms;
    private static HashMap<NamedHologram, HologramAddon> hologramMap;
    private static YamlConfiguration holoConfig;
    private static List<Map<Object, Object>> holoConfigList;
    private static File file;

    public static void init() {
        Bukkit.getScheduler().runTaskLater(Addons.INSTANCE, () -> {
            holograms = NamedHologramManager.getHolograms();
            hologramMap = new HashMap<>();
            selectedHolograms = new HashMap<>();
            file = new File(Addons.INSTANCE.getDataFolder(), "holograms.yml");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            holoConfig = YamlConfiguration.loadConfiguration(file);
            holoConfigList = (List<Map<Object, Object>>) holoConfig.getList("holograms", new ArrayList<>());
            refreshAll();
        }, 20L * 2);
    }

    public static NamedHologram getSelectedHologram(Player player) {
        return selectedHolograms.get(player.getUniqueId());
    }

    public static void setSelectedHologram(Player player, NamedHologram hologram) {
        selectedHolograms.put(player.getUniqueId(), hologram);
    }

    public static void refreshAll() {
        for (Object o : holoConfigList) {
            Map<Object, Object> map = (Map<Object, Object>) o;
            String name = (String) map.get("name");
            HologramData data = new HologramData(map);

            for (NamedHologram h : holograms) {
                HologramAddon hologramAddon = hologramMap.get(h);
                if (h.getName().equals(name)) {
                    if (hologramAddon == null) {
                        hologramAddon = new HologramAddon(h, data);
                        hologramMap.put(h, hologramAddon);
                    } else {
                        hologramAddon.data = data;
                        hologramAddon.hologram = h;
                    }
                    boolean setPickup = false;
                    boolean setTouch = false;
                    for (int i = h.size() - 1; i != 0; i--) {
                        HologramLine l = h.getLine(i);
                        if (l instanceof ItemLine) {
                            if (!setPickup) {
                                ItemLine itemLine = (ItemLine) l;
                                itemLine.setPickupHandler(player -> {
                                    if (data.items != null) {
                                        for (ItemStack item : data.items) {
                                            player.getInventory().addItem(item.clone());
                                        }
                                    }
                                    if (data.cmds != null) {
                                        for (String cmd : data.cmds) {
                                            try {
                                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", player.getName()));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    if (data.effects != null) {
                                        for (PotionEffect effect : data.effects) {
                                            player.addPotionEffect(effect);
                                        }
                                    }
                                    if (data.remove) Bukkit.getScheduler().runTask(Addons.INSTANCE, () -> delete(h));
                                });
                                setPickup = true;
                            }
                        } else if (l instanceof TextLine) {
                            if (!setTouch) {
                                TextLine textLine = (TextLine) l;
                                textLine.setTouchHandler((player -> {
                                    if (data.items != null) {
                                        for (ItemStack item : data.items) {
                                            player.getInventory().addItem(item.clone());
                                        }
                                    }
                                    if (data.cmds != null) {
                                        for (String cmd : data.cmds) {
                                            try {
                                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", player.getName()));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    if (data.effects != null) {
                                        for (PotionEffect effect : data.effects) {
                                            player.addPotionEffect(effect);
                                        }
                                    }
                                    if (data.remove) Bukkit.getScheduler().runTask(Addons.INSTANCE, () -> delete(h));
                                }));
                                setTouch = true;
                            }
                        }
                    }
                }
            }
        }
    }

    public static void addItem(NamedHologram hologram, ItemStack item) {
        boolean add = false;
        for (Map<Object, Object> map : holoConfigList) {
            if (map.get("name").equals(hologram.getName())) {
                map.putIfAbsent("items", new ArrayList<>());
                List<ItemStack> items = (List<ItemStack>) map.get("items");
                items.add(item);
                add = true;
            }
        }
        if (!add) {
            Map<Object, Object> map = new HashMap<>();
            map.put("name", hologram.getName());
            List<ItemStack> items = new ArrayList<>();
            items.add(item);
            map.put("items", items);
            holoConfigList.add(map);
        }
        save();
    }

    public static void addCommand(NamedHologram hologram, String cmd) {
        boolean add = false;
        for (Map<Object, Object> map : holoConfigList) {
            if (map.get("name").equals(hologram.getName())) {
                map.putIfAbsent("cmds", new ArrayList<>());
                List<String> cmds = (List<String>) map.get("cmds");
                cmds.add(cmd);
                add = true;
            }
        }
        if (!add) {
            Map<Object, Object> map = new HashMap<>();
            map.put("name", hologram.getName());
            List<String> cmds = new ArrayList<>();
            cmds.add(cmd);
            map.put("cmds", cmds);
            holoConfigList.add(map);
        }
        save();
    }

    public static void addEffect(NamedHologram hologram, PotionEffectType type, int duration, int amplifier) {
        boolean add = false;
        for (Map<Object, Object> map : holoConfigList) {
            if (map.get("name").equals(hologram.getName())) {
                map.putIfAbsent("effects", new ArrayList<>());
                List<PotionEffect> effects = (List<PotionEffect>) map.get("effects");
                effects.add(type.createEffect(duration * 20, amplifier));
                add = true;
            }
        }
        if (!add) {
            Map<Object, Object> map = new HashMap<>();
            List<PotionEffect> effects = new ArrayList<>();
            effects.add(type.createEffect(duration * 20, amplifier));
            map.put("effects", effects);
            holoConfigList.add(map);
        }
        save();
    }

    public static void setRemoveOnInteract(NamedHologram hologram, boolean remove) {
        boolean add = false;
        for (Map<Object, Object> map : holoConfigList) {
            if (map.get("name").equals(hologram.getName())) {
                map.put("remove", remove);
                add = true;
            }
        }
        if (!add) {
            Map<Object, Object> map = new HashMap<>();
            map.put("name", hologram.getName());
            map.put("remove", remove);
            holoConfigList.add(map);
        }
        save();
    }

    public static void save() {
        holoConfig.set("holograms", holoConfigList);
        try {
            holoConfig.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        refreshAll();
    }

    public static void delete(NamedHologram hologram) {
        hologram.delete();
        hologramMap.remove(hologram);

    }

    public static class HologramAddon {
        public NamedHologram hologram;
        public HologramData data;

        public HologramAddon(NamedHologram h, HologramData d) {
            hologram = h;
            data = d;
        }
    }

    public static class HologramData {
        public List<PotionEffect> effects;
        public List<String> cmds;
        public List<ItemStack> items;
        public boolean remove;

        public HologramData(Map<Object, Object> map) {
            if (map.containsKey("cmds")) cmds = (List<String>) map.get("cmds");
            if (map.containsKey("items")) items = (List<ItemStack>) map.get("items");
            if (map.containsKey("effects")) effects = (List<PotionEffect>) map.get("effects");
            if (map.containsKey("remove")) remove = (boolean) map.get("remove");
        }
    }
}
