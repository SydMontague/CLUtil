package de.craftlancer.clutil.old.buildings.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.SignBlock;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.clutil.old.buildings.BlockWrapper;
import de.craftlancer.clutil.old.buildings.Building;
import de.craftlancer.clutil.old.buildings.BuildingManager;
import de.craftlancer.clutil.old.buildings.FeatureBuilding;
import de.craftlancer.clutil.old.buildings.FeatureFactory;
import de.craftlancer.clutil.old.buildings.FeatureType;
import de.craftlancer.core.command.SubCommand;

public class BuildingCreateCommand extends SubCommand
{
    
    public BuildingCreateCommand(String permission, CLUtil plugin)
    {
        super(permission, plugin, false);
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!checkSender(sender))
            return "You don't have the permission for this command!";
        
        if (args.length < 2)
            return "You need to specify a name and optional a feature for this building!";
        
        if (BuildingManager.getInstance().hasBuilding(args[1]))
            return "Es gibt bereits ein Gebäude mit diesem Namen!";
        
        FeatureType type = args.length >= 3 ? FeatureType.getFeatureType(args[2]) : null;
        
        if (args.length >= 3 && type == null)
            return "The given feature type is invalid!";
        
        CuboidClipboard clip = null;
        
        try
        {
            clip = WorldEdit.getInstance().getSession(sender.getName()).getClipboard();
        }
        catch (EmptyClipboardException e)
        {
            return "Your clipboard is empty!";
        }
        
        Map<String, BlockWrapper> map = new HashMap<String, BlockWrapper>();
        
        if (type != null)
        {
            for (int x = 0; x < clip.getWidth(); x++)
                for (int y = 0; y < clip.getHeight(); y++)
                    for (int z = 0; z < clip.getLength(); z++)
                    {
                        Vector vec = new Vector(x,y,z);
                        BaseBlock block = clip.getBlock(vec);
                        if (!(block instanceof SignBlock))
                            continue;
                        
                        String[] text = ((SignBlock) block).getText();
                        
                        if (text[0].equalsIgnoreCase("[Feature]"))
                            map.put(text[1], new BlockWrapper(block, vec));
                        
                        clip.setBlock(vec, new BaseBlock(Material.AIR.getId()));
                    }
        }
        
        BlockFace face = null;
        switch (Math.abs((Math.round((((Player) sender).getLocation().getYaw()) / 90)) % 4))
        {
            case 0: 
                face = BlockFace.SOUTH;
                break;
            case 1:
                face = BlockFace.WEST;
                break;
            case 2: 
                face = BlockFace.NORTH;
                break;
            case 3: 
                face = BlockFace.EAST;
                break;
            default:
                throw new IllegalArgumentException("Should not happen!");
        }
        
        try
        {
            SchematicFormat.MCEDIT.save(clip, new File(((Plugin) getPlugin()).getDataFolder(), "schematics" + File.separator + args[1]));
        }
        catch (IOException | DataException e)
        {
            e.printStackTrace();
            return "Error while saving the schematic!";
        }
        FeatureBuilding feature = FeatureFactory.createFeature(type, map, face);
        
        BuildingManager.getInstance().addBuilding(args[1], new Building(CLUtil.getInstance(), args[1], args[2], 0, face, new ArrayList<ItemStack>(), feature, new ArrayList<String>(), null));
        BuildingManager.getInstance().save(false);
        // building create <name> [feature]
        // check if player with permission
        // check if clipboard is not empty
        // check if clipboard contains feature signs
        // create feature building for feature
        // remove feature signs from schematic
        // store schematic, building and feature
        
        return "Building successfully created!";
    }
    
    @Override
    public void help(CommandSender arg0)
    {
        // TODO Auto-generated method stub
        
    }
    
}
