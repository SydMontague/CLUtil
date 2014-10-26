package de.craftlancer.clutil.modules;

import java.util.Random;

import net.minecraft.server.v1_7_R4.EntityFallingBlock;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.TileEntityChest;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftFallingSand;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;
import de.craftlancer.clutil.modules.token.TokenType;
import de.craftlancer.core.Utils;

/*
 * Create
 *  create location
 *  create reward
 * Announce event
 * Announce event type
 * Announce event location area
 * Announce event location
 * Spawn chest at location
 * Track Token
 * Announce Token position
 * When token reaches a players Home Plot, spawn reward chest
 * 
 * 
 * 
 */
public class CaptureTheToken extends Module implements Listener
{
    private final static ItemStack TOKENITEM = new ItemStack(Material.SPONGE);
    static
    {
        ItemMeta meta = TOKENITEM.getItemMeta();
        meta.setDisplayName(TokenType.CAPTURE_EVENT.getName());
        TOKENITEM.setItemMeta(meta);
    }
    
    private Random random = new Random();
    
    /* Configuration variables start */
    private final long tickTime;
    private final Location center;
    private final int startTime;
    private final int messageTime;
    private final int minPlayers;
    private final double chancePerTick;
    private final long timeBetween;
    private final int radius;
    private final int minDistanceToTown;
    /* Configuration variables end */
    
    private long lastRun = 0;
    
    /* Running state variables start */
    private CaptureState state;
    private Location location;
    private Location approxLocation;
    private int ticksToStart;
    
    // private Object rewards;
    /* Running state variables end */
    
    public CaptureTheToken(CLUtil plugin)
    {
        super(plugin);
        tickTime = getConfig().getLong("tickTime", 20L);
        center = Utils.parseLocation(getConfig().getString("center"));
        startTime = getConfig().getInt("startTime", 15 * 60);
        messageTime = getConfig().getInt("messageTime", 4 * 60);
        timeBetween = getConfig().getLong("timeBetween", 2 * 60 * 60 * 1000); // 2 hours
        chancePerTick = getConfig().getDouble("chancePerTick", 0.00025);
        minPlayers = getConfig().getInt("minPlayers", 10);
        radius = getConfig().getInt("radius", 10);
        minDistanceToTown = getConfig().getInt("minDistanceToTown", 10);
        
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                switch (getState())
                {
                    case NONE:
                        handleNoneState();
                        break;
                    case ANNOUNCED:
                        handleAnnouncedState();
                        break;
                    case RUNNING:
                        handleRunningState();
                        break;
                }
            }
        }.runTaskTimer(getPlugin(), tickTime, tickTime);
    }
    
    /*
     * create a new instance on certain conditions
     * Conditions:
     * min Players of different groups
     * random chance
     * between certain times?
     */
    protected void handleNoneState()
    {
        if (random.nextDouble() > chancePerTick)
            return;
        
        if (Bukkit.getOnlinePlayers().size() < minPlayers)
            return;
        
        if (System.currentTimeMillis() - lastRun < timeBetween)
            return;
        
        if (TownyUniverse.getDataSource().getTowns().isEmpty())
            return;
        
        location = generateLocation();
        int dx = (50 + random.nextInt(150)) * (random.nextBoolean() ? -1 : 1);
        int dz = 50 + random.nextInt(150) * (random.nextBoolean() ? -1 : 1);
        approxLocation = new Location(location.getWorld(), location.getBlockX() + dx, 0, location.getBlockZ() + dz);
        ticksToStart = startTime;
        
        // TODO create rewards
        state = CaptureState.ANNOUNCED;
        
    }
    
    /*
     * Send messages acording to the time left until start
     * Messages:
     * event starts in time
     * event type starts in time
     * event type starts in time near loc
     * event type starts in time at loc
     * event type starts new at loc
     */
    protected void handleAnnouncedState()
    {
        if (ticksToStart == 0)
        {
            spawnChest(location, TOKENITEM);
            
            /*
             * spawn token
             */
            
            // start
            state = CaptureState.RUNNING;
            return;
        }
        
        if (ticksToStart == startTime)
            Bukkit.broadcastMessage(String.format("Event is starting in %1$s!", getNextTimerString()));
        else if (ticksToStart == startTime - messageTime)
            Bukkit.broadcastMessage(String.format("Event %1$s is starting in %2$s!", getEventName(), getNextTimerString()));
        else if (ticksToStart == startTime - messageTime * 2)
            Bukkit.broadcastMessage(String.format("Event %1$s is starting in %2$s near %3$s!", getEventName(), getNextTimerString(), getApproxLocationString()));
        else if (ticksToStart == startTime - messageTime * 3)
            Bukkit.broadcastMessage(String.format("Event %1$s is starting in %2$s at %3$s!", getEventName(), getNextTimerString(), getLocationString()));
        else if (ticksToStart == startTime - messageTime * 4)
            Bukkit.broadcastMessage(String.format("Event %1$s started at %2$s!", getEventName(), getLocationString()));
        
        ticksToStart--;
    }
    
    /*
     * Handle the running event
     * Handles:
     * track the Token
     * prevent the bearer to fast travel
     * check win conditions
     */
    protected void handleRunningState()
    {
        // TODO Auto-generated method stub
        ticksToStart++;
    }
    
    public CaptureState getState()
    {
        return state;
    }
    
    private Location generateLocation()
    {
        int x = random.nextInt(radius * 2) - radius;
        int d = (int) Math.sqrt(radius * radius - x * x);
        int z = random.nextInt(d * 2) - d;
        
        Location loc = new Location(center.getWorld(), center.getX() + x, 128, center.getY() + z);
        int distance = 999;
        try
        {
            distance = TownyUniverse.getDataSource().getWorld(center.getWorld().getName()).getMinDistanceFromOtherTownsPlots(new Coord(loc.getChunk().getX(), loc.getChunk().getZ()));
        }
        catch (NotRegisteredException e)
        {
            e.printStackTrace();
        }
        
        return distance >= minDistanceToTown ? loc : generateLocation();
    }
    
    private String getApproxLocationString()
    {
        return "X " + approxLocation.getBlockX() + " Z " + approxLocation.getBlockZ();
    }
    
    private String getNextTimerString()
    {
        return Utils.getTimeString((1000 / tickTime) * (startTime));
    }
    
    private String getLocationString()
    {
        return "X " + location.getBlockX() + " Z " + location.getBlockZ();
    }
    
    private String getEventName()
    {
        return "Capture the Token";
    }
    
    private void spawnChest(Location target, ItemStack... items)
    {
        @SuppressWarnings("deprecation")
        FallingBlock entity = target.getWorld().spawnFallingBlock(target, Material.CHEST, (byte) 0);
        
        EntityFallingBlock nmsEntity = ((CraftFallingSand) entity).getHandle();
        
        TileEntityChest tileChest = new TileEntityChest();
        net.minecraft.server.v1_7_R4.ItemStack[] content = tileChest.getContents();
        
        for (int i = 0; i < items.length && i < 27; i++)
            content[i] = CraftItemStack.asNMSCopy(items[i]);
        
        NBTTagCompound nbt = new NBTTagCompound();
        tileChest.b(nbt);
        nmsEntity.tileEntityData = nbt;
    }
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.CAPTURETHECORE;
    }
    
    enum CaptureState
    {
        NONE,
        ANNOUNCED,
        RUNNING,
    }
}
