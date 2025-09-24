package xyz.destiall.addons.items;

import org.bukkit.block.data.BlockData;

public class TempBlock {
    public int x;
    public int y;
    public int z;
    public String world;
    public long expiry;

    public String previous;
    public BlockData previousData;
    public TempBlock() {}
}
