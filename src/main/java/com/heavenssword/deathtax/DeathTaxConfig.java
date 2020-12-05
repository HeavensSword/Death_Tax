package com.heavenssword.deathtax;

// Java
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Collection;

// Minecraft
import net.minecraft.util.math.MathHelper;

public final class DeathTaxConfig
{
    // Private Fields
    private final boolean shouldLoseItemsOnDeath;
    
    private final float percentageOfExpToLose;
    private final float percentageOfItemStackToLose;
    
    private Queue<String> itemDropWhitelist = new ConcurrentLinkedQueue<String>();
    private final EquipmentTier equipmentTierThreshold;
    
    // Construction
    public DeathTaxConfig( boolean _shouldLoseItemsOnDeath, float _percentageOfExpToLose, float _percentageOfItemStackToLose, Collection<String> _itemDropWhiteList, EquipmentTier _equipmentTierThreshold )
    {
        shouldLoseItemsOnDeath = _shouldLoseItemsOnDeath;
        
        percentageOfExpToLose = MathHelper.clamp( _percentageOfExpToLose, 0.0f, 1.0f );
        percentageOfItemStackToLose = MathHelper.clamp( _percentageOfItemStackToLose, 0.0f, 1.0f );
        
        itemDropWhitelist.addAll( _itemDropWhiteList );
        equipmentTierThreshold = _equipmentTierThreshold;
    }
    
    // Public Methods
    public boolean getShouldLoseItemsOnDeath()
    {
        return shouldLoseItemsOnDeath;
    }
    
    public boolean shouldLoseAllExp()
    {
        return ( percentageOfExpToLose >= 1.0f );
    }
    
    public float getPercentageOfExpToLose()
    {
        return percentageOfExpToLose;
    }
    
    public boolean shouldLoseAllItemsInStack()
    {
        return ( percentageOfItemStackToLose >= 1.0f );
    }
    
    public float getPercentageOfItemStackToLose()
    {
        return percentageOfItemStackToLose;
    }
    
    public boolean hasItemInWhiteList( String itemName )
    {
        return itemDropWhitelist.contains( itemName );
    }
    
    public EquipmentTier getEquipmentTierThreshold()
    {
        return equipmentTierThreshold;
    }
}
