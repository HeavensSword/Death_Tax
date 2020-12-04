package com.heavenssword.deathtax;

public final class ConfigLoader
{
    // Public Methods
    public static DeathTaxConfig loadConfigFromFile( String configPath )
    {
        return new DeathTaxConfig( true, 0.5f );
    }
}
