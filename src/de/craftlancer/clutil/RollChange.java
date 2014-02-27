package de.craftlancer.clutil;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import ru.tehkode.permissions.bukkit.PermissionsEx;

public class RollChange implements Listener
{
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent e)
    {
        if (e.getLine(1).equals("[Rollenwechsel]") && !e.getPlayer().hasPermission("cl.util.admin"))
            e.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onSignInteract(PlayerInteractEvent e)
    {
        if (!e.hasBlock())
            return;
        
        if (e.getClickedBlock().getType() != Material.SIGN && e.getClickedBlock().getType() != Material.SIGN_POST && e.getClickedBlock().getType() != Material.WALL_SIGN)
            return;
        
        Sign sign = (Sign) e.getClickedBlock().getState();
        
        if (!sign.getLine(1).equals("[Rollenwechsel]"))
            return;
        
        String extra = e.getPlayer().hasPermission("clgroups.admin") ? "admin" : e.getPlayer().hasPermission("clgroups.mod") ? "mod" : null;
        
        PermissionsEx.getUser(e.getPlayer()).setGroups(new String[] { sign.getLine(2) });
        
        if (extra != null)
            PermissionsEx.getUser(e.getPlayer()).addGroup(extra);
        
        e.getPlayer().sendMessage("Rolle erfolgreich zu " + sign.getLine(2) + " gewechselt!");
    }
}
