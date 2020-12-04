package com.heavenssword.deathtax;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectionUtilities
{
    @SuppressWarnings( "rawtypes" )
    public static Field getField( Class clazz, String fieldName )
    {
        try 
        {
            return clazz.getDeclaredField( fieldName );
        }
        catch( NoSuchFieldException e ) 
        {
            Class superClass = clazz.getSuperclass();
            if( superClass == null ) 
                return null;
            else
                return getField( superClass, fieldName );
        }
    }
    
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public static Method getMethod( Class clazz, String methodName )
    {
        try
        {
            return clazz.getDeclaredMethod( methodName );
        }
        catch( NoSuchMethodException e )
        {
            Class superClass = clazz.getSuperclass();
            if( superClass == null )
                return null;
            else
                return getMethod( superClass, methodName );
        }
    }
    
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public static Method getMethod( Class clazz, String methodName, Class... methodArgs )
    {
        try
        {
            return clazz.getDeclaredMethod( methodName, methodArgs );
        }
        catch( NoSuchMethodException e )
        {
            Class superClass = clazz.getSuperclass();
            if( superClass == null )
                return null;
            else
                return getMethod( superClass, methodName, methodArgs );
        }
    }
}
