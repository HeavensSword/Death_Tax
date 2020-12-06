package com.heavenssword.deathtax;

public enum EquipmentTier
{
    // Enumerated Values
    NONE( -1 ),
    
    WOODEN( 0 ),
    STONE( 1 ),
    LEATHER( 2 ),
    CHAINMAIL( 3 ),
    IRON( 4 ),
    GOLDEN( 5 ),
    DIAMOND( 6 ),
    NETHERITE( 7 );
    
    // Private Fields
    private int value = -1;
    
    // Construction
    private EquipmentTier( int _value )
    {
        value = _value;
    }
    
    // Public Methods
    public int getValue()
    {
        return value;
    }
    
    public int compareWith( EquipmentTier other )
    {
        if( value > other.value )
            return 1;
        else if( value < other.value )
            return -1;
        
        return 0;
    }
    
    public static boolean hasValue( String value )
    {
        EquipmentTier foundValue = null;
        
        try
        {
            foundValue = valueOf( value );
        }
        catch( IllegalArgumentException e )
        {
            return false;
        }
        
        return ( foundValue != null );
    }
}
