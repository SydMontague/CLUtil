package de.craftlancer.clutil.buildings;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

public class BlockWrapper
{
    private int type;
    private int data;
    private int x;
    private int y;
    private int z;
    
    public BlockWrapper(BaseBlock block, Vector vec)
    {
        this.type = block.getType();
        this.data = block.getData();
        this.x = vec.getBlockX();
        this.y = vec.getBlockY();
        this.z = vec.getBlockZ();
    }
    
    public int getTypeId()
    {
        return type;
    }
    
    public int getData()
    {
        return data;
    }
    
    public int getX()
    {
        return x;
    }
    
    public int getY()
    {
        return y;
    }
    
    public int getZ()
    {
        return z;
    }
}
