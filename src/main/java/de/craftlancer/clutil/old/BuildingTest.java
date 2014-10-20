package de.craftlancer.clutil.old;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class BuildingTest implements Listener
{
    /*
    private BuildingManager manager;
    
    public BuildingTest()
    {
        manager = BuildingManager.getInstance();
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent e)
    {
        if (!e.hasItem())
            return;
        
        if (e.getItem().getType() == Material.GOLD_SWORD)
        {
            manager.undoLastBuilding();
            return;
        }
        else if (e.getItem().getType() == Material.DIAMOND_SWORD)
        {
            manager.previewBuilding(e.getPlayer(), "gasthaus");
            return;
        }
        else if (e.getItem().getType() != Material.IRON_SWORD)
            return;
        
        //manager.startBuilding(e.getPlayer(), "gasthaus", e.getPlayer().getInventory());
        /*
         * try
         * {
         * build = SchematicFormat.MCEDIT.load(new File(plugin.getDataFolder(),
         * "gasthaus.schematic"));
         * }
         * catch (IOException ee)
         * {
         * ee.printStackTrace();
         * }
         * catch (DataException ee)
         * {
         * ee.printStackTrace();
         * }
         * if (e.getItem() != null && e.getItem().getType() ==
         * Material.WOOD_AXE)
         * return;
         * int facing =
         * Math.abs((Math.round((e.getPlayer().getLocation().getYaw()) / 90)) %
         * 4);
         * // add base facing
         * facing += 1;
         * build.rotate2D(facing * 90);
         * loc =
         * e.getPlayer().getLocation().getBlock().getRelative(build.getOffset
         * ().getBlockX(), 0, build.getOffset().getBlockZ());
         * inventory = e.getPlayer().getInventory();
         * xmax = build.getWidth() - 1;
         * ymax = build.getHeight() - 1;
         * zmax = build.getLength() - 1;
         * x = 0;
         * y = 0;
         * z = 0;
         * started = true;
         */
    //}
    /*
     * @EventHandler
     * public void onBlockBreak(BlockBreakEvent e)
     * {
     * if (isProtected(e.getBlock()))
     * e.setCancelled(true);
     * }
     * @EventHandler
     * public void onBlockPlace(BlockPlaceEvent e)
     * {
     * if (isProtected(e.getBlock()))
     * e.setCancelled(true);
     * }
     * private boolean isProtected(Block b)
     * {
     * if (!started)
     * return false;
     * if (b.getX() >= loc.getX() && b.getX() <= loc.getX() + xmax)
     * if (b.getY() >= loc.getY() && b.getY() <= loc.getY() + ymax)
     * if (b.getZ() >= loc.getZ() && b.getZ() <= loc.getZ() + zmax)
     * return true;
     * return false;
     * }
     * @SuppressWarnings("deprecation")
     * @Override
     * public void run()
     * {
     * if (!started)
     * return;
     * for (int i = 0; i < BLOCKS_PER_RUN; i++)
     * {
     * if (x == xmax && y == ymax && z == zmax)
     * {
     * started = false;
     * return;
     * }
     * BaseBlock b = build.getBlock(new Vector(x, y, z));
     * LocalWorld world = null;
     * for (LocalWorld w : WorldEdit.getInstance().getServer().getWorlds())
     * if (w.getName().equals(loc.getWorld().getName()))
     * {
     * world = w;
     * break;
     * }
     * if (world == null)
     * throw new NullPointerException("This world should never be null!");
     * // world.set
     * // Block bb = loc.getRelative(x, y, z);
     * if (b.getType() == 0 ||
     * CraftItYourself.removeItemFromInventory(inventory, new
     * ItemStack(b.getType(), 1)))
     * {
     * undoList.add(loc.getWorld().getBlockAt(loc.getX() + x, loc.getY() + y,
     * loc.getZ() + z).getState());
     * world.setBlock(new Vector(loc.getX() + x, loc.getY() + y, loc.getZ() +
     * z), b, false);// bb.setTypeIdAndData(b.getType(),
     * // (byte)
     * // b.getData(),
     * // false);
     * }
     * else
     * {
     * Bukkit.getLogger().info("" + Material.getMaterial(b.getType()));
     * return;
     * }
     * if (x == xmax)
     * {
     * x = 0;
     * if (z == zmax)
     * {
     * y++;
     * z = 0;
     * }
     * else
     * z++;
     * }
     * else
     * x++;
     * }
     * }
     */
}
