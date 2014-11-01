package de.craftlancer.clutil.modules;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.minecraft.server.v1_7_R4.EntityFallingBlock;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.TileEntityChest;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftFallingSand;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;
import de.craftlancer.clutil.modules.token.TokenTracker;
import de.craftlancer.clutil.modules.token.TokenType;
import de.craftlancer.clutil.speed.CaptureSpeedModifier;
import de.craftlancer.core.Utils;
import de.craftlancer.core.ValueMap;
import de.craftlancer.currencyhandler.CurrencyHandler;
import de.craftlancer.speedapi.SpeedAPI;

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
    public final static ItemStack TOKENITEM = new ItemStack(Material.SPONGE);
    static
    {
        ItemMeta meta = TOKENITEM.getItemMeta();
        meta.setDisplayName(TokenType.CAPTURE_EVENT.getName());
        TOKENITEM.setItemMeta(meta);
    }
    
    private Random random = new Random();    
    private long lastRun = 0;
    
    /* Configuration variables start */
    private final long tickTime;
    private final Location center;
    private final int startTime;
    private final int messageTime;
    private final int minPlayers;
    private final double chancePerTick;
    private final long timeBetween;
    private final int radius;
    private final int protectionTime;
    private final int minDistanceToTown;
    private final int moneyReward;
    private final ValueMap rewardMap;
    private final int maxValue;
    /* Configuration variables end */
    
    /* Running state variables start */
    private CaptureState state = CaptureState.NONE;
    private Location location;
    private Location approxLocation;
    private int ticksToStart;
    private List<ItemStack> reward;    
    private TokenTracker tokenTracker;
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
        protectionTime = getConfig().getInt("protectionTime", 10);
        
        moneyReward = getConfig().getInt("moneyReward", 200);
        maxValue = getConfig().getInt("maxValue", 200);
        rewardMap = new ValueMap(getConfig().getConfigurationSection("rewardItems"));
        
        SpeedAPI.addModifier("captureevent", new CaptureSpeedModifier(3));
        
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
        reward = rewardMap.getRandomItems(maxValue, 27);
        
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
            tokenTracker = new TokenTracker(getPlugin(), location);
            spawnChest(location, TOKENITEM);
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
        if (ticksToStart % messageTime == 0)
            Bukkit.broadcastMessage(String.format("The Token is currently at %1$s", tokenTracker.getLocationString()));
        
        Entity entity = tokenTracker.getEntity();
        
        if (isAtOwnHomeblock(entity))
        {
            HumanEntity player = ((HumanEntity) entity);
            Bukkit.broadcastMessage(String.format("%1$s hat den Token zu seiner Stadt gebracht!", player.getName()));
            
            player.getInventory().remove(TOKENITEM);
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, protectionTime * 20, 5));
            
            CurrencyHandler.getHandler("money").giveCurrency(player, moneyReward);
            spawnChest(entity.getLocation(), reward);
            
            lastRun = System.currentTimeMillis();
            tokenTracker.end();
            tokenTracker = null;
            state = CaptureState.NONE;
            return;
        }
        
        ticksToStart++;
    }
    
    private boolean isAtOwnHomeblock(Entity entity)
    {
        if (entity.getType() != EntityType.PLAYER)
            return false;
        
        HumanEntity player = (HumanEntity) entity;
        try
        {
            Resident resi = TownyUniverse.getDataSource().getResident(player.getName());
            
            if (!resi.hasTown())
                return false;
            
            Town town = resi.getTown();
            
            return town.getHomeBlock().getX() == entity.getLocation().getChunk().getX() && town.getHomeBlock().getZ() == entity.getLocation().getChunk().getZ();
        }
        catch (TownyException e)
        {
            e.printStackTrace();
            return false;
        }
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
        spawnChest(target, Arrays.asList(items));
    }
    
    private void spawnChest(Location target, List<ItemStack> items)
    {
        @SuppressWarnings("deprecation")
        FallingBlock entity = target.getWorld().spawnFallingBlock(target, Material.CHEST, (byte) 0);
        
        EntityFallingBlock nmsEntity = ((CraftFallingSand) entity).getHandle();
        
        TileEntityChest tileChest = new TileEntityChest();
        net.minecraft.server.v1_7_R4.ItemStack[] content = tileChest.getContents();
        
        for (int i = 0; i < items.size() && i < 27; i++)
            content[i] = CraftItemStack.asNMSCopy(items.get(i));
        
        NBTTagCompound nbt = new NBTTagCompound();
        tileChest.b(nbt);
        nmsEntity.tileEntityData = nbt;
    }
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.CAPTURETHETOKEN;
    }
    
    private enum CaptureState
    {
        NONE,
        ANNOUNCED,
        RUNNING,
    }
}
