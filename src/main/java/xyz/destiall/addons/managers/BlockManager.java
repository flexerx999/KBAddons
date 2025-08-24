package xyz.destiall.addons.managers;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.items.TempBlock;
import xyz.destiall.addons.utils.Pair;
import xyz.destiall.java.gson.Gson;
import xyz.destiall.java.gson.GsonBuilder;
import xyz.destiall.java.gson.JsonArray;
import xyz.destiall.java.gson.JsonObject;
import xyz.destiall.java.gson.JsonParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class BlockManager implements Listener {
    public static final HashMap<Pair<Block, BlockState>, Long> EXPIRIES = new HashMap<>();
    public static final HashMap<Chunk, List<Pair<Block, BlockState>>> UNLOADED = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().create();
    private BlockManager() {}

    public static void disable() {
        JsonObject object = new JsonObject();
        List<TempBlock> blocks = new ArrayList<>();
        for (Map.Entry<Pair<Block, BlockState>, Long> en : EXPIRIES.entrySet()) {
            Block block = en.getKey().getKey();
            BlockState state = en.getKey().getValue();
            TempBlock tblock = new TempBlock();
            tblock.expiry = en.getValue();
            tblock.previous = state.getType().name();
            tblock.previousData = state.getBlockData();
            tblock.x = block.getX();
            tblock.y = block.getY();
            tblock.z = block.getZ();
            tblock.world = block.getWorld().getName();
            blocks.add(tblock);
        }
        for (Map.Entry<Chunk, List<Pair<Block, BlockState>>> en : UNLOADED.entrySet()) {
            List<Pair<Block, BlockState>> list = en.getValue();
            for (Pair<Block, BlockState> pair : list) {
                Block block = pair.getKey();
                BlockState state = pair.getValue();
                TempBlock tblock = new TempBlock();
                tblock.expiry = System.currentTimeMillis();
                tblock.previous = state.getType().name();
                tblock.previousData = state.getBlockData();
                tblock.x = block.getX();
                tblock.y = block.getY();
                tblock.z = block.getZ();
                tblock.world = block.getWorld().getName();
                blocks.add(tblock);
            }
        }
        JsonArray arr = (JsonArray) GSON.toJsonTree(blocks);
        object.add("expiry", arr);
        try {
            if (!arr.isEmpty()) {
                File dataFile = new File(Addons.INSTANCE.getDataFolder(), "blocks.json");
                if (!dataFile.exists()) {
                    dataFile.createNewFile();
                }
                FileWriter writer = new FileWriter(dataFile.getPath());
                writer.write(GSON.toJson(object));
                writer.close();
                Addons.INSTANCE.getLogger().info("Saved all temp blocks!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void init() {
        Bukkit.getPluginManager().registerEvents(new BlockManager(), Addons.INSTANCE);
        File dataFile = new File(Addons.INSTANCE.getDataFolder(), "blocks.json");
        if (dataFile.exists()) {
            try {
                Reader reader = Files.newBufferedReader(dataFile.toPath());
                JsonObject obj = new JsonParser().parse(reader).getAsJsonObject();
                if (obj.has("expiry")) {
                    JsonArray arr = obj.getAsJsonArray("expiry");
                    TempBlock[] blocks = GSON.fromJson(arr, TempBlock[].class);
                    for (TempBlock t : blocks) {
                        World world = Bukkit.getWorld(t.world);
                        Block block = world.getBlockAt(t.x, t.y, t.z);
                        Material material = Material.getMaterial(t.previous);
                        block.setType(material);
                        block.setBlockData(t.previousData);
                    }
                }
                Addons.INSTANCE.getLogger().info("Restored all temp blocks!");
                dataFile.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Bukkit.getScheduler().runTaskTimer(Addons.INSTANCE, () -> {
            final HashSet<Pair<Block, BlockState>> remove = new HashSet<>();
            for (final Map.Entry<Pair<Block, BlockState>, Long> ex : EXPIRIES.entrySet()) {
                if (ex.getValue() <= System.currentTimeMillis() || ex.getKey().getKey().isEmpty()) {
                    Block block = ex.getKey().getKey();
                    BlockState state = ex.getKey().getValue();
                    Chunk chunk = block.getChunk();
                    remove.add(ex.getKey());
                    if (chunk.isLoaded() || chunk.load()) {
                        block.setType(state.getType());
                        block.setBlockData(state.getBlockData());
                        continue;
                    }
                    if (UNLOADED.containsKey(chunk)) {
                        UNLOADED.get(chunk).add(ex.getKey());
                        continue;
                    }
                    List<Pair<Block, BlockState>> list = new ArrayList<>();
                    list.add(ex.getKey());
                    UNLOADED.put(chunk, list);
                }
            }
            for (final Pair<Block, BlockState> block : remove) {
                EXPIRIES.remove(block);
            }
            remove.clear();
        }, 0L, 1L);
    }

    public static void remove(Block block) {
        Pair<Block, BlockState> pair = EXPIRIES.keySet().stream().filter(p -> p.getKey().equals(block)).findFirst().orElse(null);
        if (pair == null)
            return;
        EXPIRIES.remove(pair);
    }

    @EventHandler
    public void onLoadChunk(ChunkLoadEvent e) {
        if (UNLOADED.isEmpty())
            return;

        List<Pair<Block, BlockState>> list = UNLOADED.get(e.getChunk());
        if (list == null)
            return;

        if (!e.getChunk().isLoaded()) {
            System.out.println("Loaded chunk at " + e.getChunk().getX() + ", " + e.getChunk().getZ());
            if (!e.getChunk().load())
                return;
        }

        for (Pair<Block, BlockState> ex : list) {
            Block block = ex.getKey();
            BlockState state = ex.getValue();
            block.setType(state.getType());
            block.setBlockData(state.getBlockData());
        }
        UNLOADED.remove(e.getChunk());
    }
}
