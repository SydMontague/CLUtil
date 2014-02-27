package de.craftlancer.clutil;

import java.util.logging.Logger;

import org.bukkit.Bukkit;

public class Debug
{
    public static Logger log = Bukkit.getLogger();
    public static boolean debug = true;
    
    public static void debug(String string)
    {
        if (debug)
            log.info("[Debug] " + string);
    }
    
    public static void debug(String[] string)
    {
        if (debug)
            for (String s : string)
                debug(s);
    }
    
    public static void error(String string)
    {
        log.severe(string);
    }
    
    public static void error(String[] string)
    {
        for (String s : string)
            error(s);
    }
    
    public static void warning(String string)
    {
        log.warning(string);
    }
    
    public static void warning(String[] string)
    {
        for (String s : string)
            warning(s);
    }
    
    public static void info(String string)
    {
        log.info(string);
    }
    
    public static void info(String[] string)
    {
        for (String s : string)
            info(s);
    }
}
