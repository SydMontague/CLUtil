package de.craftlancer.clutil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ru.tehkode.permissions.bukkit.PermissionsEx;

import de.craftlancer.skilllevels.event.SkillLevelUpEvent;

public class AutoLevelUp implements Listener
{
    private Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();
    private CLUtil plugin;
    private Map<Integer, String> levelUpString = new HashMap<Integer, String>();
    public AutoLevelUp(CLUtil plugin)
    {
        this.plugin = plugin;
        
        map.put(10, new ArrayList<String>()
        {
            private static final long serialVersionUID = 1L;
            {
                add("-mcmmo.skills.swords");
                add("-mcmmo.skills.archery");
                add("-mcmmo.skills.acrobatics");
                add("-cl.util.wald.dmgmod");
                add("-cl.util.shear");
            }
        });
        levelUpString.put(10, "Neue Skills! \n Krieger: Schwertkampf \n Waldläufer: Bogenschießen, extra Bogenschaden \n Schurke: Akrobatik, erhöhter Scherenschaden");
        map.put(15, new ArrayList<String>()
        {
            private static final long serialVersionUID = 1L;
            {
                add("-mcmmo.ability.swords.bleed");
                add("-mcmmo.ability.archery.trackarrows");
                add("-mcmmo.ability.acrobatics.roll");
                add("-shadow.advbackstab");
            }
        });
        levelUpString.put(15, "Neue Skills! \n Krieger: Blutungschance \n Waldläufer: Pfeilrückgewinnung \n Schurke: Rolle, verbesserter Backstab");
        map.put(20, new ArrayList<String>()
        {
            private static final long serialVersionUID = 1L;
            {
                add("-cl.util.armor.iron");
                add("-cl.util.wald.speed");
                add("-mcmmo.ability.acrobatics.dodge");
            }
        });
        levelUpString.put(20, "Neue Skills! \n Krieger: Volle Geschwindigkeit mit Eisenrüstung \n Waldläufer: 15% schneller laufen \n Schurke: Ausweichchance");
        map.put(25, new ArrayList<String>()
        {
            private static final long serialVersionUID = 1L;
            {
                add("-mcmmo.ability.swords.counterattack");
                add("-shadow.airassassination");
                add("-mcmmo.ability.archery.bonusdamage");
            }
        });
        levelUpString.put(25, "Neue Skills! \n Krieger: Konterchance \n Waldläufer: Bonusschaden durch mcMMO Level \n Schurke: Luftattentat");
        map.put(30, new ArrayList<String>()
        {
            private static final long serialVersionUID = 1L;
            {
                add("-cl.util.sneak");
                add("-shadow.pickpocket");
            }
        });
        levelUpString.put(30, "Neue Skills! \n Du kannst nun mit /sneak schleichen, solange du keine Eisen/Diarüstung trägst.");
        map.put(35, new ArrayList<String>()
        {
            private static final long serialVersionUID = 1L;
            {
                add("-cl.util.berserk.speed");
                add("-shadow.lockpick");
                add("-mcmmo.ability.archery.daze");
                
            }
        });
        levelUpString.put(35, "Neue Skills! \n Krieger: Berserker Geschwindigkeitsbuff \n Waldläufer: Verwirrende Pfeile \n Schurke: Schlossknacken");
        map.put(40, new ArrayList<String>()
        {
            private static final long serialVersionUID = 1L;
            {
                add("-cl.util.armor.dia");
                add("-shadow.hook");
                add("-cl.util.arrow");
            }
        });
        levelUpString.put(40, "Neue Skills! \n Krieger: Volle Geschwindigkeit mit Diarüstung \n Waldläufer: Giftpfeile \n Schurke: Enterhaken");
        map.put(50, new ArrayList<String>()
        {
            private static final long serialVersionUID = 1L;
            {
                add("-cl.util.berserk.damage");
                add("-mcmmo.ability.acrobatics.gracefulroll");
                add("-cl.util.find");
            }
        });
        levelUpString.put(50, "Neue Skills! \n Krieger: Berserker Schadensbuff \n Waldläufer: /find Befehl (richtet Kompass auf Spieler aus) \n Schurke: verbessertes abrollen");
    }
    
    @EventHandler
    public void onLevelUp(SkillLevelUpEvent e)
    {
        if (!map.containsKey(e.getNewLevel()))
            return;
        
        if (plugin.getServer().getPlayerExact(e.getUser()) == null)
            return;
        
        for (String s : map.get(e.getNewLevel()))
            PermissionsEx.getPermissionManager().getUser(e.getUser()).removePermission(s);
        
        if(levelUpString.containsKey(e.getNewLevel()))
            plugin.getServer().getPlayerExact(e.getUser()).sendMessage(levelUpString.get(e.getNewLevel()));
    }
}
