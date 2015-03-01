package de.craftlancer.clutil.modules;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import ru.tehkode.permissions.bukkit.PermissionsEx;
import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;
import de.craftlancer.core.Utils;
import de.craftlancer.wayofshadows.event.ShadowLeapEvent;

public class UtilModule extends Module implements Listener
{
    private final List<String> startPerms;
    
    public UtilModule(CLUtil plugin)
    {
        super(plugin);
        startPerms = getConfig().getStringList("startPerms");
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
        
        getPlugin().getCommand("sneak").setExecutor(new CommandExecutor()
        {
            
            @Override
            public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args)
            {
                
                if (!(sender instanceof Player))
                    return false;
                
                Player player = (Player) sender;
                if (player.hasMetadata("cl.util.sneak"))
                {
                    player.removeMetadata("cl.util.sneak", getPlugin());
                    player.sendMessage("Sneak aktiviert");
                }
                else
                {
                    player.setMetadata("cl.util.sneak", new FixedMetadataValue(getPlugin(), null));
                    player.sendMessage("Sneak deaktiviert");
                }
                
                return true;
            }
        });
        
        getPlugin().getCommand("leap").setExecutor(new CommandExecutor()
        {
            
            @Override
            public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3)
            {
                if (!(sender instanceof Player))
                    return false;
                
                Player player = (Player) sender;
                
                if (player.hasMetadata("cl.util.leap"))
                {
                    player.removeMetadata("cl.util.leap", getPlugin());
                    player.sendMessage("Leap aktiviert");
                    getConfig().set("leap." + player.getUniqueId(), false);
                }
                else
                {
                    player.setMetadata("cl.util.leap", new FixedMetadataValue(getPlugin(), null));
                    player.sendMessage("Leap deaktiviert");
                    getConfig().set("leap." + player.getUniqueId(), true);
                }
                
                return true;
            }
            
        });
        
        getPlugin().getCommand("health").setExecutor(new CommandExecutor()
        {
            
            @Override
            public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3)
            {
                if (!(sender instanceof Player))
                    return false;
                
                Player player = (Player) sender;
                
                if (player.getHealthScale() == 20)
                    player.setHealthScale(50);
                else
                    player.setHealthScale(20);
                
                return true;
            }
            
        });
        
        for (String perm : startPerms)
            getPlugin().getServer().getPluginManager().addPermission(new Permission(perm, PermissionDefault.TRUE));
    }
    
    @Override
    public void onDisable()
    {
        saveConfig();
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDragonDeath(EntityDeathEvent e)
    {
        if (e.getEntity().hasMetadata("SkillLevels.ignore"))
            e.setDroppedExp(0);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVillagerInteract(PlayerInteractEntityEvent e)
    {
        if (e.getRightClicked().getType() == EntityType.VILLAGER)
            e.setCancelled(true);
    }
    
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        if (getConfig().getBoolean("leap" + e.getPlayer().getUniqueId(), false) && !e.getPlayer().hasMetadata("cl.util.leap"))
            e.getPlayer().setMetadata("cl.util.leap", new FixedMetadataValue(getPlugin(), null));
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFirstJoin(PlayerJoinEvent e)
    {
        if (!e.getPlayer().hasPlayedBefore())
            for (String s : startPerms)
                PermissionsEx.getPermissionManager().getUser(e.getPlayer()).addPermission(s);
    }
    
    @EventHandler
    public void onLeap(ShadowLeapEvent event)
    {
        if (event.getPlayer().hasMetadata("cl.util.leap"))
            event.setCancelled(true);
    }
    
    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent e)
    {
        if (!e.getItem().getType().isEdible())
            return;
        
        float extra = 0;
        
        switch (e.getItem().getType())
        {
            case BREAD:
                extra = 2.4f;
                break;
            case CARROT_ITEM:
                extra = 4.8f;
                break;
            case BAKED_POTATO:
                extra = 7.2f;
                break;
            case POTATO_ITEM:
                extra = 0.6f;
                break;
            case POISONOUS_POTATO:
                extra = 1.2f;
                break;
            case GOLDEN_CARROT:
                extra = 14.4f;
                break;
            case PUMPKIN_PIE:
                extra = 4.8f;
                break;
            case COOKIE:
                extra = 0.4f;
                break;
            case MELON:
                extra = 1.2f;
                break;
            case MUSHROOM_SOUP:
                extra = 7.2f;
                break;
            case RAW_CHICKEN:
                extra = 1.2f;
                break;
            case COOKED_CHICKEN:
                extra = 7.2f;
                break;
            case RAW_BEEF:
                extra = 1.8f;
                break;
            case COOKED_BEEF:
                extra = 12.8f;
                break;
            case RAW_FISH:
                extra = 0.4f;
                break;
            case COOKED_FISH:
                extra = 9.6f;
                break;
            case PORK:
                extra = 1.8f;
                break;
            case GRILLED_PORK:
                extra = 12.8f;
                break;
            case APPLE:
                extra = 2.4f;
                break;
            case GOLDEN_APPLE:
                extra = 9.6f;
                break;
            case ROTTEN_FLESH:
                extra = 0.8f;
                break;
            case SPIDER_EYE:
                extra = 3.2f;
                break;
            default:
        }
        
        e.getPlayer().setSaturation(e.getPlayer().getSaturation() + extra);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        event.setDroppedExp((int) (Utils.getExp(event.getEntity().getLevel()) * 0.65D));
    }
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.UTILITY;
    }
    
}
