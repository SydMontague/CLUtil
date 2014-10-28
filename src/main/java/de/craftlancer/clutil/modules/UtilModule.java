package de.craftlancer.clutil.modules;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import ru.tehkode.permissions.bukkit.PermissionsEx;
import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;

public class UtilModule extends Module implements Listener
{
    private static final List<String> startPerms = new ArrayList<String>();
    static {
        //TODO init permsList
    }
    
    
    public UtilModule(CLUtil plugin)
    {
        super(plugin);
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
        
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
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFirstJoin(PlayerJoinEvent e)
    {
        if (!e.getPlayer().hasPlayedBefore())
            for (String s : startPerms)
                PermissionsEx.getPermissionManager().getUser(e.getPlayer()).addPermission(s);
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
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.UTILITY;
    }
    
}
