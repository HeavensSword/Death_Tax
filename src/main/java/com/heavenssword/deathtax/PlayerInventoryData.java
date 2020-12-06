package com.heavenssword.deathtax;

// Java
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

// Mojang
import com.mojang.datafixers.util.Pair;

// Minecraft
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.PlayerInventory;

public final class PlayerInventoryData
{
    // Private Fields
    private Queue<Pair<Integer, ItemStack>> allInventory = new ConcurrentLinkedQueue<Pair<Integer, ItemStack>>();
    
    // Construction
    public PlayerInventoryData() {}
    
    public PlayerInventoryData( PlayerInventory playerInventory )
    {
        for( int i = 0; i < playerInventory.getSizeInventory(); ++i )
            allInventory.add( Pair.of( i, playerInventory.getStackInSlot( i ) ) );
    }
    
    // Public Methods
    public Queue<Pair<Integer, ItemStack>> getInventory()
    {
        return allInventory;
    }
}
