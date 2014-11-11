package de.craftlancer.clutil.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;
import de.craftlancer.skilllevels.Utils;

public class ClassChanger extends Module implements Listener, TabExecutor
{
    private final Map<String, List<String>> aliases = new HashMap<>();
    private final Map<UUID, BukkitTask> changes = new HashMap<>();
    private final int commandDelay;
    
    public ClassChanger(CLUtil plugin)
    {
        super(plugin);
        for (String key : getConfig().getConfigurationSection("aliases").getKeys(false))
            aliases.put(key, getConfig().getStringList("aliases." + key));
        commandDelay = getConfig().getInt("commandDelay", 1800 * 20);
        getPlugin().getCommand("class").setExecutor(this);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent e)
    {
        if (e.getLine(1).equals("[Class change]") && !e.getPlayer().hasPermission("cl.util.admin"))
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
        
        if (!sign.getLine(1).equals("[Class change]") || !isClassAlias(sign.getLine(2)))
            return;
        
        new ClassChangeTask(e.getPlayer().getUniqueId(), getClassByAlias(sign.getLine(2))).runTask(getPlugin());
    }
    
    @Override
    public List<String> onTabComplete(CommandSender arg0, Command arg1, String arg2, String[] args)
    {
        switch (args.length)
        {
            case 1:
                List<String> classes = new ArrayList<>();
                for (List<String> list : aliases.values())
                    classes.addAll(list);
                
                return Utils.getMatches(args[0], classes);
            default:
                return null;
        }
    }
    
    private boolean isClassAlias(String str)
    {
        for (List<String> entry : aliases.values())
            if (entry.contains(str))
                return true;
        
        return false;
    }
    
    private String getClassByAlias(String str)
    {
        for (Entry<String, List<String>> entry : aliases.entrySet())
            if (entry.getValue().contains(str))
                return entry.getKey();
        
        return null;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args)
    {
        if (!(sender instanceof Player))
            sender.sendMessage("This command can only be used by players!");
        else if (args.length < 1 || !isClassAlias(args[0]))
            sender.sendMessage("You must enter a valid class! (Ranger, Warrior, Rogue)");
        
        Player player = (Player) sender;
        
        if(changes.containsKey(player.getUniqueId()))
        {
            BukkitTask task = changes.get(player.getUniqueId());
            task.cancel();
        }
        
        changes.put(player.getUniqueId(), new ClassChangeTask(player.getUniqueId(), getClassByAlias(args[0])).runTaskLater(getPlugin(), commandDelay));
        
        sender.sendMessage("In 30 minutes your class will be changed to " + getClassByAlias(args[0]) + "!");
        return true;
    }
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.CLASSCHANGE;
    }
    
    protected void removeEntry(UUID player)
    {
        changes.remove(player);
    }
    
    class ClassChangeTask extends BukkitRunnable
    {
        private UUID player;
        private String group;
        
        public ClassChangeTask(UUID player, String group)
        {
            this.player = player;
            this.group = group;
        }
        
        @SuppressWarnings("deprecation")
        @Override
        public void run()
        {
            OfflinePlayer p = Bukkit.getOfflinePlayer(player);
            PermissionUser user = PermissionsEx.getUser(p.getName());
            String extra = user.has("cl.util.admin") ? "admin" : user.has("cl.util.mod") ? "mod" : null;
            
            user.setGroups(new String[] { group });
            
            if (extra != null)
                user.addGroup(extra);
            
            if (p.isOnline())
                p.getPlayer().sendMessage("Class successfully changed to " + group + " !");
            
            removeEntry(player);
        }
    }
}
