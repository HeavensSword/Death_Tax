package com.heavenssword.deathtax;

public final class DeathTaxConfig
{
    // Private Fields
    private final boolean shouldLoseItemsOnDeath;
    
    private final float percentageOfExpToLose;
    
    // Construction
    public DeathTaxConfig( boolean _shouldLoseItemsOnDeath, float _percentageOfExpToLose )
    {
        shouldLoseItemsOnDeath = _shouldLoseItemsOnDeath;
        
        percentageOfExpToLose = _percentageOfExpToLose;
    }
    
    // Public Methods
    public boolean getShouldLoseItemsOnDeath()
    {
        return shouldLoseItemsOnDeath;
    }
    
    public float getPercentageOfExpToLose()
    {
        return percentageOfExpToLose;
    }
}
