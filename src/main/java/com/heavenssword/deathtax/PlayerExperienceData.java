package com.heavenssword.deathtax;

public final class PlayerExperienceData
{
    // Private Fields
    public final int experienceLevel;
    public final int experienceTotal;
    public final float experience;
    
    // Construction
    public PlayerExperienceData( int _experienceLevel, int _experienceTotal, float _experience )
    {
        experienceLevel = _experienceLevel;
        experienceTotal = _experienceTotal;
        experience = _experience;
    }
    
    // Public Methods
    public int getExperienceLevel()
    {
        return experienceLevel;
    }
    
    public int getExperienceTotal()
    {
        return experienceTotal;
    }
    
    public float getExperience()
    {
        return experience;
    }
    
    public void modifyExperienceByPercentage( float expMod )
    {
        
    }
}
