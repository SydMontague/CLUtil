package de.craftlancer.clutil.modules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.Module;
import de.craftlancer.clutil.ModuleType;
import de.craftlancer.skilllevels.Utils;

public class ClassChanger extends Module implements Listener, TabExecutor
{
    private static final Map<String, String> classes = new HashMap<String, String>();
    
    static
    {
        classes.put("Waldläufer", "waldl");
        classes.put("Bogenschütze", "waldl");
        classes.put("waldl", "waldl");
        classes.put("Schurke", "schurke");
        classes.put("Assassine", "schurke");
        classes.put("Krieger", "krieger");
        classes.put("waldläufer", "waldl");
        classes.put("bogenschütze", "waldl");
        classes.put("schurke", "schurke");
        classes.put("assassine", "schurke");
        classes.put("krieger", "krieger");
    }
    
    private final int commandDelay;
    
    public ClassChanger(CLUtil plugin)
    {
        super(plugin);
        commandDelay = getConfig().getInt("commandDelay", 1800 * 20);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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
        
        if (!sign.getLine(1).equals("[Rollenwechsel]") || !classes.containsKey(sign.getLine(2)))
            return;
        
        new ClassChangeTask(e.getPlayer().getUniqueId(), sign.getLine(2)).runTask(getPlugin());
    }
    
    @Override
    public List<String> onTabComplete(CommandSender arg0, Command arg1, String arg2, String[] args)
    {
        switch (args.length)
        {
            case 1:
                return Utils.getMatches(args[0], classes.keySet());
            default:
                return null;
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args)
    {
        if (!(sender instanceof Player))
            sender.sendMessage("Dieser Befehl kann nur von Spielern benutzt werden!");
        else if (args.length < 1 || classes.containsKey(args[0]))
            sender.sendMessage("Du musst eine valide Klasse angeben! (Waldläufer, Krieger, Schurke)");
        
        Player player = (Player) sender;
        
        new ClassChangeTask(player.getUniqueId(), args[0]).runTaskLater(getPlugin(), commandDelay);
        
        sender.sendMessage("Deine Klasse wird in 30 Minuten zu " + args[0] + " gewechselt!");
        return true;
    }
    
    @Override
    public ModuleType getType()
    {
        return ModuleType.CLASSCHANGE;
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
                p.getPlayer().sendMessage("Rolle erfolgreich zu " + group + " gewechselt!");
        }
    }
}
