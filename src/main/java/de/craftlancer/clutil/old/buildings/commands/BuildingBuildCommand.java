package de.craftlancer.clutil.old.buildings.commands;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.old.buildings.Building;
import de.craftlancer.clutil.old.buildings.BuildingManager;
import de.craftlancer.clutil.old.buildings.MassChestInventory;
import de.craftlancer.core.command.SubCommand;
import de.craftlancer.groups.GroupPlayer;
import de.craftlancer.groups.Town;
import de.craftlancer.groups.managers.PlayerManager;

public class BuildingBuildCommand extends SubCommand
{
    
    public BuildingBuildCommand(String permission, CLUtil plugin)
    {
        super(permission, plugin, false);
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!checkSender(sender))
            return "You don't have the permission for this command!"; // TODO externalise
            
        if (args.length < 2 || !BuildingManager.getInstance().hasBuilding(args[1]))
            return "You need to specify a valid building!"; // TODO externalise
            
        GroupPlayer gp = PlayerManager.getGroupPlayer(sender.getName());
        Town t = gp.getTown();
        
        if (t == null)
            return "You are in no Town"; // TODO externalise
            
        if (!t.hasPermission(sender.getName(), "town.build")) // TODO externalise
            return "You don't have the permission to place buildings."; // TODO externalise
            
        Building build = BuildingManager.getInstance().getBuilding(args[1]);
        
        if (!build.isInTown((Player) sender, t))
            return "This whole building needs to be on claimed area!"; // TODO externalise
            
        Player player = (Player) sender;
        
        int facing = Math.abs((Math.round((player.getLocation().getYaw()) / 90)) % 4);
        
        int xFacing = 0;
        int zFacing = 0;
        BlockFace signFacing = null;
        switch (facing)
        {
            case 0: // SOUTH
                xFacing = -1;
                zFacing = 0;
                signFacing = BlockFace.NORTH;
                break;
            case 1: // WEST
                xFacing = 0;
                zFacing = -1;
                signFacing = BlockFace.EAST;
                break;
            case 2: // NORTH
                xFacing = 1;
                zFacing = 0;
                signFacing = BlockFace.SOUTH;
                break;
            case 3: // EAST
                xFacing = 0;
                zFacing = 1;
                signFacing = BlockFace.WEST;
                break;
        }
        
        MassChestInventory inventory;
        
        if (true/* town.getBuildingInventory() == null */)
        {
            Block block = player.getLocation().getBlock().getRelative(xFacing, 0, zFacing);
            block.setType(Material.CHEST);
            Block block2 = player.getLocation().getBlock().getRelative(xFacing * 2, 0, zFacing * 2);
            block2.setType(Material.CHEST);
            
            inventory = new MassChestInventory(build.getName(), build.getName(), ((Chest) block.getState()).getInventory(), ((Chest) block2.getState()).getInventory());
        }
        
        Block sign = player.getLocation().getBlock().getRelative(-xFacing, 0, -zFacing);
        sign.setType(Material.SIGN_POST);
        
        Sign s = (Sign) sign.getState();
        MaterialData data = s.getData();
        ((org.bukkit.material.Sign) data).setFacingDirection(signFacing);
        s.setData(data);
        s.update();
        
        BuildingManager.getInstance().startBuilding(player, build.getName(), inventory);
        return "Bau gestartet!";
    }
    
    @Override
    public void help(CommandSender arg0)
    {
        // TODO Auto-generated method stub
        
    }
    
}
