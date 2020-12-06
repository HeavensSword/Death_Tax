package com.heavenssword.deathtax;

//Java
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;

public final class ConfigLoader
{
    // Public Static Methods
    public static DeathTaxConfig loadConfigFromFile( File rootDirectorypath )
    {
        File config = new File( rootDirectorypath, "death_tax.properties" );
        if( !config.exists() )
            initConfig( config );

        boolean isEnabled = false;
        boolean shouldLoseItemsOnDeath = true;
        float percentageOfExpToLose = 1.0f;
        float percentageOfItemStackToLose = 1.0f;
        Collection<String> itemWhitelist = new ArrayList<String>();
        EquipmentTier equipmentTierThreshold = EquipmentTier.NONE;
        
        Properties props = new Properties();
        try
        {
            props.load( new FileReader( config ) );
            isEnabled = props.getProperty( "enabled" ).equals( "true" );            
            
            if( isEnabled )
            {
                Set<Object> keySets = props.keySet();
                for( Object obj : keySets )
                {
                    String curLine = (String)obj;

                    String[] tokens = curLine.split( "\\." );
                    if( tokens.length < 2 )
                        continue;

                    String key = tokens[0];
                    if( key.equals( "exp" ) )
                    {
                        String expKey = tokens[1];
                        
                        if( "percentageOfExpToLose".equals( expKey ) )
                            percentageOfExpToLose = Float.parseFloat( props.getProperty( curLine ) );
                    }
                    else if( key.equals( "inventory" ) )
                    {
                        String inventoryKey = tokens[1];
                        
                        switch( inventoryKey )
                        {
                            case "shouldLoseItemsOnDeath":
                                shouldLoseItemsOnDeath = Boolean.parseBoolean( props.getProperty( curLine ) );
                            break;
                            case "percentageOfItemStackToLose":
                                percentageOfItemStackToLose = Float.parseFloat( props.getProperty( curLine ) );
                            break;
                            case "itemDropWhitelist":
                            {
                                itemWhitelist.addAll( Arrays.asList( props.getProperty( curLine ).toLowerCase().replace( "_", " " ).split( "," ) ) );
                            }
                            break;
                            case "equipmentTierThreshold":
                            {
                                String propertyValueStr = props.getProperty( curLine ).toUpperCase();
                                
                                if( EquipmentTier.hasValue( propertyValueStr ) )
                                    equipmentTierThreshold = EquipmentTier.valueOf( propertyValueStr );
                            }
                            break;
                        }
                    }
                }
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        
        return new DeathTaxConfig( isEnabled, shouldLoseItemsOnDeath, percentageOfExpToLose, percentageOfItemStackToLose, itemWhitelist, equipmentTierThreshold );
    }

    // Private Static Methods
    private static void initConfig( File configFile )
    {
        try
        {
            configFile.createNewFile();
            
            try( BufferedWriter writer = new BufferedWriter( new FileWriter( configFile ) ) )
            {
                writer.write( "# Death Tax Config\n\n" +

                              "enabled=true\n\n" +
                                           
                              "# === Percentage of Exp to Lose\n" +
                              "# This should be a value between 0.0 and 1.0. (e.g. 0.5 = 50%)\n" +
                              "# How much exp will be lost on death?\n\n" +

                              "exp.percentageOfExpToLose=0.25\n\n" +

                              "# === Should Lost Items On Death\n" +
                              "# Values : [true/false]\n" +
                              "# Simply specifies if items should be lost upon death.\n\n" +

                              "inventory.shouldLoseItemsOnDeath=true\n\n" +

                              "# === Percentage of Item Stack to Lose\n" +
                              "# This should be a value between 0.0 and 1.0. (e.g. 0.5 = 50%)\n" +
                              "# If any item is stackable, what percentage of your current stack will be lost on death?\n\n" +

                              "inventory.percentageOfItemStackToLose=0.2\n\n" +

                              "# === Item Drop Whitelist\n" +
                              "# Values : Comma separated item names (REPLACE SPACES WITH UNDERSCORE e.g. \"Diamond Sword\" should be \"diamond_sword\")\n" +
                              "# This is a list of items that you don't want to drop.\n\n" +

                              "inventory.itemDropWhitelist=apple,diamond_sword\n\n" +

                              "# === Equipment Tier Threshold\n" +
                              "# Values : [NONE, WOODEN, STONE, LEATHER, CHAINMAIL, IRON, GOLDEN, DIAMOND, or NETHERITE]\n" +
                              "# You can specify the minimum SAFE material tier. That tier and above will be safe from being dropped, anything below the chosen tier can be dropped.\n" +
                              "# e.g. If you choose \"IRON\", any equipment made from iron, gold, diamond, or netherite would be safe.\n\n" +

                              "inventory.equipmentTierThreshold=CHAINMAIL\n" );
            }
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }
}
