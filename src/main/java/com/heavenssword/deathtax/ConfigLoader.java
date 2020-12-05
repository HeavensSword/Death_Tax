package com.heavenssword.deathtax;

public final class ConfigLoader
{
    // Public Methods
    public static DeathTaxConfig loadConfigFromFile( String configPath )
    {
        return new DeathTaxConfig( false, 0.5f );
    }
}
