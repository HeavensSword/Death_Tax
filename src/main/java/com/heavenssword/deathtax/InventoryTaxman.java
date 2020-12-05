package com.heavenssword.deathtax;

// Java
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

// Minecraft
import net.minecraft.item.ItemStack;

public final class InventoryTaxman
{
    // Private Fields
    private static final Map<String, EquipmentTier> equipmentTierItemLookupMap = new ConcurrentHashMap<String, EquipmentTier>()
                                                                                 { private static final long serialVersionUID = -7620257081530091034L;
                                                                                   {
                                                                                     put( "Fishing Rod", EquipmentTier.WOODEN );
                                                                                     put( "Compass", EquipmentTier.IRON );
                                                                                     put( "Shears", EquipmentTier.IRON );
                                                                                     put( "Clock", EquipmentTier.GOLDEN );

                                                                                     put( "Wooden Hoe", EquipmentTier.WOODEN );
                                                                                     put( "Stone Hoe", EquipmentTier.STONE );
                                                                                     put( "Iron Hoe", EquipmentTier.IRON );
                                                                                     put( "Golden Hoe", EquipmentTier.GOLDEN );
                                                                                     put( "Diamond Hoe", EquipmentTier.DIAMOND );

                                                                                     put( "Wooden Axe", EquipmentTier.WOODEN );
                                                                                     put( "Stone Axe", EquipmentTier.STONE );
                                                                                     put( "Iron Axe", EquipmentTier.IRON );
                                                                                     put( "Golden Axe", EquipmentTier.GOLDEN );
                                                                                     put( "Diamond Axe", EquipmentTier.DIAMOND );

                                                                                     put( "Wooden Pickaxe", EquipmentTier.WOODEN );
                                                                                     put( "Stone Pickaxe", EquipmentTier.STONE );
                                                                                     put( "Iron Pickaxe", EquipmentTier.IRON );
                                                                                     put( "Golden Pickaxe", EquipmentTier.GOLDEN );
                                                                                     put( "Diamond Pickaxe", EquipmentTier.DIAMOND );

                                                                                     put( "Wooden Shovel", EquipmentTier.WOODEN );
                                                                                     put( "Stone Shovel", EquipmentTier.STONE );
                                                                                     put( "Iron Shovel", EquipmentTier.IRON );
                                                                                     put( "Golden Shovel", EquipmentTier.GOLDEN );
                                                                                     put( "Diamond Shovel", EquipmentTier.DIAMOND );

                                                                                     put( "Wooden Shovel", EquipmentTier.WOODEN );
                                                                                     put( "Stone Shovel", EquipmentTier.STONE );
                                                                                     put( "Iron Shovel", EquipmentTier.IRON );
                                                                                     put( "Golden Shovel", EquipmentTier.GOLDEN );
                                                                                     put( "Diamond Shovel", EquipmentTier.DIAMOND );

                                                                                     put( "Shield", EquipmentTier.IRON );

                                                                                     put( "Wooden Sword", EquipmentTier.WOODEN );
                                                                                     put( "Stone Sword", EquipmentTier.STONE );
                                                                                     put( "Iron Sword", EquipmentTier.IRON );
                                                                                     put( "Golden Sword", EquipmentTier.GOLDEN );
                                                                                     put( "Diamond Sword", EquipmentTier.DIAMOND );
                                                                                     put( "Netherite Sword", EquipmentTier.NETHERITE );

                                                                                     put( "Leather Cap", EquipmentTier.LEATHER );
                                                                                     put( "Leather Tunic", EquipmentTier.LEATHER );
                                                                                     put( "Leather Pants", EquipmentTier.LEATHER );
                                                                                     put( "Leather Boots", EquipmentTier.LEATHER );

                                                                                     put( "Chainmail Helmet", EquipmentTier.CHAINMAIL );
                                                                                     put( "Chainmail Chestplate", EquipmentTier.CHAINMAIL );
                                                                                     put( "Chainmail Leggings", EquipmentTier.CHAINMAIL );
                                                                                     put( "Chainmail Boots", EquipmentTier.CHAINMAIL );

                                                                                     put( "Iron Helmet", EquipmentTier.IRON );
                                                                                     put( "Iron Chestplate", EquipmentTier.IRON );
                                                                                     put( "Iron Leggings", EquipmentTier.IRON );
                                                                                     put( "Iron Boots", EquipmentTier.IRON );

                                                                                     put( "Golden Helmet", EquipmentTier.GOLDEN );
                                                                                     put( "Golden Chestplate", EquipmentTier.GOLDEN );
                                                                                     put( "Golden Leggings", EquipmentTier.GOLDEN );
                                                                                     put( "Golden Boots", EquipmentTier.GOLDEN );

                                                                                     put( "Diamond Helmet", EquipmentTier.DIAMOND );
                                                                                     put( "Diamond Chestplate", EquipmentTier.DIAMOND );
                                                                                     put( "Diamond Leggings", EquipmentTier.DIAMOND );
                                                                                     put( "Diamond Boots", EquipmentTier.DIAMOND );

                                                                                     put( "Netherite Helmet", EquipmentTier.NETHERITE );
                                                                                     put( "Netherite Chestplate", EquipmentTier.NETHERITE );
                                                                                     put( "Netherite Leggings", EquipmentTier.NETHERITE );
                                                                                     put( "Netherite Boots", EquipmentTier.NETHERITE );
                                                                                 } };

    // Public Methods
    public static Queue<ItemStack> taxInventory( DeathTaxConfig deathTaxConfig, Queue<ItemStack> playerInventory )
    {
        if( deathTaxConfig == null || !deathTaxConfig.getShouldLoseItemsOnDeath() )
            return playerInventory;
        
        Queue<ItemStack> taxedInventory = new ConcurrentLinkedQueue<ItemStack>();
        
        for( ItemStack itemStack : playerInventory )
        {
            // Keep whitelisted items.
            if( deathTaxConfig.hasItemInWhiteList( itemStack.getDisplayName().getString() ) )
                taxedInventory.add( itemStack );
            
            // Keep items that are equal to above the chosen EquipmentTier Threshold.
            if( deathTaxConfig.getEquipmentTierThreshold().compareWith( getEquipmentTierForItem( itemStack ) ) <= 0 )
                taxedInventory.add( itemStack );
            
            // Remove items from stacks based on selected loss percentage.
            if( itemStack.isStackable() )
            {
                int stackCount = itemStack.getCount();
                int amountToLose = Math.round( stackCount * deathTaxConfig.getPercentageOfItemStackToLose() );
                
                ItemStack reducedStack = itemStack.copy();
                reducedStack.setCount( stackCount - amountToLose );
                
                itemStack.setCount( amountToLose );
                
                taxedInventory.add( reducedStack );
            }
        }
        
        // What remains in the incoming inventory is what will be discarded.
        for( ItemStack itemStack : taxedInventory )
            playerInventory.remove( itemStack );
        
        return taxedInventory;
    }
    
    public static EquipmentTier getEquipmentTierForItem( ItemStack itemStack )
    {
        String itemName = itemStack.getDisplayName().getString().replace( "Enchanted", "" ).replace( "Damaged", "" );
        
        if( equipmentTierItemLookupMap.containsKey( itemName ) )
            return equipmentTierItemLookupMap.get( itemName );
        
        return EquipmentTier.NONE;
    }
}
