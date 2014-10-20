package de.craftlancer.clutil.old;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.metadata.FixedMetadataValue;

import de.craftlancer.clutil.CLUtil;

public class ResourceAlgoTest
{
    private class BlockWrapper
    {
        private Block block;
        private int depth;
        
        public BlockWrapper(Block block, int depth)
        {
            this.block = block;
            this.depth = depth;
        }
        
        public Block getBlock()
        {
            return block;
        }
        
        public int getDepth()
        {
            return depth;
        }
    }
    
    private int y = 4;
    private int radius = 320;
    private int min = 3;
    private double veinChance = 0.8D;
    private double oreChance = 0.9D;
    private double chancePerSource = 0.01D;
    
    private BlockFace[] face = { BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH };
    
    public ResourceAlgoTest(CLUtil plugin)
    {
        veinChance = plugin.getConfig().getDouble("veinChance", veinChance);
        oreChance = plugin.getConfig().getDouble("oreChance", oreChance);
        chancePerSource = plugin.getConfig().getDouble("chancePerSource", chancePerSource);
        
        for (int i = 0 - radius / 2; i < radius; i++)
            for (int k = 0 - radius / 2; k < radius; k++)
                plugin.getServer().getWorlds().get(0).getBlockAt(i, y, k).setType(Material.AIR);
        
        Bukkit.getLogger().info("veinChance: " + veinChance + " oreChance: " + oreChance);
        
        Random rand = new Random();
        
        int placed = 0;
        
        while (min >= placed || rand.nextDouble() < veinChance)
        {
            double chance = oreChance;
            
            Queue<BlockWrapper> queue = new LinkedList<BlockWrapper>();
            List<Block> levelone = new ArrayList<Block>();
            
            placed++;
            int x = rand.nextInt(radius) - radius / 2;
            int z = rand.nextInt(radius) - radius / 2;
            Bukkit.getLogger().info("start vein: " + placed + " " + x + " " + z);
            
            Block block = plugin.getServer().getWorlds().get(0).getBlockAt(x, y, z);
            if (block.getType() == Material.DIAMOND_BLOCK)
            {
                placed--;
                continue;
            }
            
            block.setType(Material.DIAMOND_BLOCK);
            block.setMetadata("oreDepth", new FixedMetadataValue(plugin, 0));
            
            queue.add(new BlockWrapper(block, 0));
            levelone.add(block);
            
            Bukkit.getLogger().info("start placing ores 1");
            
            while (!queue.isEmpty())
            {
                BlockWrapper bw = queue.poll();
                Block b = bw.getBlock();
                
                switch (bw.getDepth())
                {
                    case 0:
                    {
                        for (BlockFace f : face)
                        {
                            Block bb = b.getRelative(f);
                            if (rand.nextDouble() < chance && (!bb.hasMetadata("oreDepth") || bb.getMetadata("oreDepth").get(0).asInt() != 0))
                            {
                                bb.setType(Material.DIAMOND_BLOCK);
                                bb.setMetadata("oreDepth", new FixedMetadataValue(plugin, bw.getDepth()));
                                queue.add(new BlockWrapper(bb, 0));
                                chance -= chancePerSource;
                            }
                            else if (!bb.hasMetadata("oreDepth") || bb.getMetadata("oreDepth").get(0).asInt() > 1)
                            {
                                
                                bb.setType(Material.GOLD_BLOCK);
                                bb.setMetadata("oreDepth", new FixedMetadataValue(plugin, 1));
                                queue.add(new BlockWrapper(bb, 1));
                            }
                        }
                        break;
                    }
                    case 1:
                    {
                        for (BlockFace f : face)
                        {
                            Block bb = b.getRelative(f);
                            if (!bb.hasMetadata("oreDepth") || bb.getMetadata("oreDepth").get(0).asInt() > 2)
                            {
                                bb.setType(Material.GOLD_BLOCK);
                                bb.setMetadata("oreDepth", new FixedMetadataValue(plugin, 2));
                                queue.add(new BlockWrapper(bb, 2));
                            }
                        }
                        break;
                    }
                    case 2:
                    {
                        for (BlockFace f : face)
                        {
                            Block bb = b.getRelative(f);
                            if (!bb.hasMetadata("oreDepth") || bb.getMetadata("oreDepth").get(0).asInt() > 3)
                            {
                                bb.setType(Material.IRON_BLOCK);
                                bb.setMetadata("oreDepth", new FixedMetadataValue(plugin, 3));
                                queue.add(new BlockWrapper(bb, 3));
                            }
                        }
                        break;
                    }
                    case 3:
                    {
                        for (BlockFace f : face)
                        {
                            Block bb = b.getRelative(f);
                            if (!bb.hasMetadata("oreDepth") || bb.getMetadata("oreDepth").get(0).asInt() > 4)
                            {
                                bb.setType(Material.IRON_BLOCK);
                                bb.setMetadata("oreDepth", new FixedMetadataValue(plugin, 4));
                                queue.add(new BlockWrapper(bb, 4));
                            }
                        }
                        break;
                    }
                    case 4:
                    {
                        for (BlockFace f : face)
                        {
                            Block bb = b.getRelative(f);
                            if (!bb.hasMetadata("oreDepth") || bb.getMetadata("oreDepth").get(0).asInt() > 5)
                            {
                                bb.setType(Material.IRON_BLOCK);
                                bb.setMetadata("oreDepth", new FixedMetadataValue(plugin, 5));
                                //queue.add(new BlockWrapper(bb, 2));
                            }
                        }
                        break;
                    }
                    
                }
                /*
                if (b.getType() != Material.DIAMOND_BLOCK)
                    continue;
                
                Block bb = b.getRelative(1, 0, 0);
                if (rand.nextDouble() < chance && bb.getType() != Material.DIAMOND_BLOCK)
                {
                    bb.setType(Material.DIAMOND_BLOCK);
                    queue.add(bb);
                    levelone.add(bb);
                    chance -= chancePerSource;
                }
                bb = b.getRelative(0, 0, 1);
                if (rand.nextDouble() < chance && bb.getType() != Material.DIAMOND_BLOCK)
                {
                    bb.setType(Material.DIAMOND_BLOCK);
                    queue.add(bb);
                    levelone.add(bb);
                    chance -= chancePerSource;
                }
                bb = b.getRelative(-1, 0, 0);
                if (rand.nextDouble() < chance && bb.getType() != Material.DIAMOND_BLOCK)
                {
                    bb.setType(Material.DIAMOND_BLOCK);
                    queue.add(bb);
                    levelone.add(bb);
                    chance -= chancePerSource;
                }
                bb = b.getRelative(0, 0, -1);
                if (rand.nextDouble() < chance && bb.getType() != Material.DIAMOND_BLOCK)
                {
                    bb.setType(Material.DIAMOND_BLOCK);
                    queue.add(bb);
                    levelone.add(bb);
                    chance -= chancePerSource;
                }
                
                Bukkit.getLogger().info("Queue Size: " + queue.size() + "");
            }
            
            queue.addAll(levelone);
            
            Bukkit.getLogger().info("start placing ores 2");
            while (!queue.isEmpty())
            {
                Block b = queue.poll();
                
                switch (b.getType())
                {
                    case DIAMOND_BLOCK:
                    {
                        Block bb = b.getRelative(0, 0, -1);
                        if (bb.getType() != Material.DIAMOND_BLOCK && bb.getType() != Material.GOLD_BLOCK)
                        {
                            bb.setType(Material.GOLD_BLOCK);
                            queue.add(bb);
                        }
                        bb = b.getRelative(0, 0, 1);
                        if (bb.getType() != Material.DIAMOND_BLOCK && bb.getType() != Material.GOLD_BLOCK)
                        {
                            bb.setType(Material.GOLD_BLOCK);
                            queue.add(bb);
                        }
                        bb = b.getRelative(-1, 0, 0);
                        if (bb.getType() != Material.DIAMOND_BLOCK && bb.getType() != Material.GOLD_BLOCK)
                        {
                            bb.setType(Material.GOLD_BLOCK);
                            queue.add(bb);
                        }
                        bb = b.getRelative(1, 0, 0);
                        if (bb.getType() != Material.DIAMOND_BLOCK && bb.getType() != Material.GOLD_BLOCK)
                        {
                            bb.setType(Material.GOLD_BLOCK);
                            queue.add(bb);
                        }
                        break;
                    }
                    case GOLD_BLOCK:
                    {
                        Block bb = b.getRelative(0, 0, -1);
                        if (bb.getType() != Material.DIAMOND_BLOCK && bb.getType() != Material.GOLD_BLOCK && bb.getType() != Material.IRON_BLOCK)
                        {
                            bb.setType(Material.IRON_BLOCK);
                        }
                        bb = b.getRelative(0, 0, 1);
                        if (bb.getType() != Material.DIAMOND_BLOCK && bb.getType() != Material.GOLD_BLOCK && bb.getType() != Material.IRON_BLOCK)
                        {
                            bb.setType(Material.IRON_BLOCK);
                        }
                        bb = b.getRelative(1, 0, 0);
                        if (bb.getType() != Material.DIAMOND_BLOCK && bb.getType() != Material.GOLD_BLOCK && bb.getType() != Material.IRON_BLOCK)
                        {
                            bb.setType(Material.IRON_BLOCK);
                        }
                        bb = b.getRelative(-1, 0, 0);
                        if (bb.getType() != Material.DIAMOND_BLOCK && bb.getType() != Material.GOLD_BLOCK && bb.getType() != Material.IRON_BLOCK)
                        {
                            bb.setType(Material.IRON_BLOCK);
                        }
                        break;
                    }
                    default:
                        continue;
                }*/
            }
        }
    }
}
