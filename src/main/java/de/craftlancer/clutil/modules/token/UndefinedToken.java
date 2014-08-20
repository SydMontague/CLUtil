package de.craftlancer.clutil.modules.token;

public class UndefinedToken extends Token
{
    @Override
    public TokenType getType()
    {
        return TokenType.UNDEFINED;
    }
}
