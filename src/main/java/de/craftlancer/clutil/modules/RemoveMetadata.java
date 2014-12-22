package de.craftlancer.clutil.modules;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;

public class RemoveMetadata extends Module
{
    static int MIN_X = -5000;
    static int MAX_X = 5000;
    static int MIN_Y = 0;
    static int MAX_Y = 255;
    static int MIN_Z = -5000;
    static int MAX_Z = 5000;
    
    int x;
    int y;
    int z;
    int perTick;
    boolean finished = false;
    
    public RemoveMetadata(CLUtil plugin)
    {
        super(plugin);
        
        this.perTick = getConfig().getInt("perTick", 10000);
        
        x = getConfig().getInt("x", MIN_X);
        y = getConfig().getInt("y", MIN_Y);
        z = getConfig().getInt("z", MIN_Z);
        finished = getConfig().getBoolean("finished", false);
        
        getPlugin().getCommand("replacer").setExecutor(new CommandExecutor()
        {
            @Override
            public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args)
            {
                if (!sender.getName().equalsIgnoreCase("SydMontague") && !(sender instanceof ConsoleCommandSender))
                    return true;
                
                try
                {
                    int i = Integer.parseInt(args[0]);
                    if (i <= 0)
                        return true;
                    
                    perTick = i;
                    return true;
                }
                catch (NumberFormatException | ArrayIndexOutOfBoundsException e)
                {
                    sender.sendMessage("Currently at " + x + " " + y + " " + z);
                    return true;
                }
            }
        });
        
        new BukkitRunnable()
        {
            //TODO handle per chunk, not per block
            //this way a chunk has only be loaded once, not 16*256 times
            private World world = Bukkit.getWorld("world");
            
            @SuppressWarnings("deprecation")
            @Override
            public void run()
            {
                long time = 0;
                
                for (int i = 0; i < perTick; i++)
                {
                    x++;
                    if (x > MAX_X)
                    {
                        x = MIN_X;
                        z++;
                    }
                    if (z > MAX_Z)
                    {
                        z = MIN_Z;
                        y++;
                    }
                    if (y > MAX_Y)
                        finished = true;
                    
                    if (finished)
                        cancel();
                    
                    long time2 = System.nanoTime();
                    Block b = world.getBlockAt(x, y, z);
                    time += System.nanoTime() - time2;
                    
                    
                    if (b.getType() != Material.STONE)
                        continue;
                    
                    if (b.getData() == 2 || b.getData() == 3 || b.getData() == 5)
                        b.setData((byte) 0);
                }
                
                System.out.println(System.nanoTime() - time);
            }
        }.runTaskTimer(getPlugin(), 1, 1);
    }
    
    @Override
    public void onDisable()
    {
        getConfig().set("x", x);
        getConfig().set("y", y);
        getConfig().set("z", z);
        getConfig().set("finished", finished);
        saveConfig();
    }
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.REMOVEMETA;
    }
    
}
