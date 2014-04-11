package de.craftlancer.clutil.physics;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class WeightEffectListener implements Listener
{
    @EventHandler
    public void onFallDamage(EntityDamageEvent event)
    {
        if (event.getCause() != DamageCause.FALL)
            return;
        
        if (event.getEntityType() != EntityType.PLAYER)
            return;
        
        int weight = WeightManager.getInstance().getWeight((Player) event.getEntity());
        float height = event.getEntity().getFallDistance();
        
        float damage = calculateFallDamage(weight, height);
        
        event.setDamage(damage);
    }
    
    private static float calculateFallDamage(int weight, float height)
    {
        // schaden = m/2 * v² * faktor
        // v = Fallhöhe
        // faktor = magic value
        return ((weight / 2) * height * height) / 100;
    }
}
