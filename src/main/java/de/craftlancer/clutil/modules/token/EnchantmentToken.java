package de.craftlancer.clutil.modules.token;

import org.bukkit.enchantments.Enchantment;

public class EnchantmentToken extends Token
{
    private Enchantment enchantment;
    
    public EnchantmentToken(Enchantment enchantment)
    {
        this.enchantment = enchantment;
    }
    
    public Enchantment getEnchantment()
    {
        return enchantment;
    }

    @Override
    public TokenType getType()
    {
        return TokenType.ENCHANTMENT;
    }
}
