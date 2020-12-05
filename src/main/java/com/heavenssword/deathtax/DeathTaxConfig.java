package com.heavenssword.deathtax;

import net.minecraft.util.math.MathHelper;

public final class DeathTaxConfig
{
    // Private Fields
    private final boolean shouldLoseItemsOnDeath;
    
    private final float percentageOfExpToLose;
    
    // Construction
    public DeathTaxConfig( boolean _shouldLoseItemsOnDeath, float _percentageOfExpToLose )
    {
        shouldLoseItemsOnDeath = _shouldLoseItemsOnDeath;
        
        percentageOfExpToLose = MathHelper.clamp( _percentageOfExpToLose, 0.0f, 1.0f );
    }
    
    // Public Methods
    public boolean getShouldLoseItemsOnDeath()
    {
        return shouldLoseItemsOnDeath;
    }
    
    public boolean getShouldLoseAllExp()
    {
        return ( percentageOfExpToLose >= 1.0f );
    }
    
    public float getPercentageOfExpToLose()
    {
        return percentageOfExpToLose;
    }
}
