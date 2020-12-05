package com.heavenssword.deathtax;

// Java
import java.util.ArrayList;
import java.util.Arrays;

public final class ConfigLoader
{
    // Public Methods
    public static DeathTaxConfig loadConfigFromFile( String configPath )
    {
        return new DeathTaxConfig( false, 0.5f, 0.2f, new ArrayList<String>( Arrays.asList( "Diamond Sword", "Apple" ) ), EquipmentTier.STONE );
    }
}
