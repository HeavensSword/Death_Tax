package com.heavenssword.deathtax;

// Java
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

// Log4j
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Minecraft
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SCombatPacket;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.GameRules;

// MinecraftForge
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;

// The value here should match an entry in the META-INF/mods.toml file
@Mod( "deathtax" )
public class DeathTax
{
    // Private static fields
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    
    // Private fields
    private DeathTaxConfig deathTaxConfig = null;
    
    private Map<Integer, PlayerExperienceData> deadPlayerExpDataMap = new ConcurrentHashMap<Integer, PlayerExperienceData>();
    private Map<Integer, Queue<ItemStack>> deadPlayerInventoryMap = new ConcurrentHashMap<Integer, Queue<ItemStack>>();

    // Construction
    public DeathTax()
    {   
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register( this );
    }

    // Public event handlers
    @SubscribeEvent
    public void initialize( final FMLServerAboutToStartEvent event )
    {        
        deathTaxConfig = ConfigLoader.loadConfigFromFile( "" );
    }
    
    @SubscribeEvent
    public void onLivingDeath( final LivingDeathEvent event )
    {
        if( event.getEntityLiving() instanceof ServerPlayerEntity )
        {
            ServerPlayerEntity deadPlayer = (ServerPlayerEntity)event.getEntityLiving();
            String playerName = deadPlayer.getDisplayName().getString();

            LOGGER.debug( "The player \"" + playerName + "\" has died." );

            LOGGER.debug( "Item's in \"" + playerName + "'s\" main inventory:" );
            for( ItemStack itemStack : deadPlayer.inventory.mainInventory )
                LOGGER.debug( "\t" + itemStack.getDisplayName().getString() + " [" + itemStack.getCount() + "]" );
            
            if( event.isCancelable() && !event.isCanceled() )
                event.setCanceled( true );
            
            handlePlayerDeathPenalties( deadPlayer, event.getSource() );
        }
    }
    
    @SubscribeEvent
    public void onPlayerRespawn( final PlayerEvent.PlayerRespawnEvent event )
    {
        if( event.getEntityLiving() instanceof ServerPlayerEntity && deathTaxConfig != null )
        {
            ServerPlayerEntity respawnedPlayer = (ServerPlayerEntity)event.getEntityLiving();
            
            if( respawnedPlayer.isSpectator() )
                return;
            
            //LOGGER.debug( "Respawning player with UUID: " + respawnedPlayer.getUniqueID().toString() );
            //LOGGER.debug( "UUID hash: " + respawnedPlayer.getUniqueID().hashCode() );
            PlayerExperienceData playerExpData = null;
            int respawnedPlayerHash = respawnedPlayer.getUniqueID().hashCode();
            if( deadPlayerExpDataMap.containsKey( respawnedPlayerHash ) )
            {
                playerExpData = deadPlayerExpDataMap.get( respawnedPlayerHash );
                deadPlayerExpDataMap.remove( respawnedPlayerHash );
            }
            
            LOGGER.debug( "Respawned player exp vals: expLevel = " + respawnedPlayer.experienceLevel + " expTotal = " + respawnedPlayer.experienceTotal + " exp = " + respawnedPlayer.experience );
                    
            if( playerExpData != null && !deathTaxConfig.shouldLoseAllExp() )
            {
                LOGGER.debug( "Reclaimed player exp vals: expLevel = " + playerExpData.getExperienceLevel() + " expTotal = " + playerExpData.getExperienceTotal() + " exp = " + playerExpData.getExperience() );
                respawnedPlayer.giveExperiencePoints( Math.round( playerExpData.getExperienceTotal() * ( 1.0f - deathTaxConfig.getPercentageOfExpToLose() ) ) );                
                LOGGER.debug( "Respawned player exp vals after gift: expLevel = " + respawnedPlayer.experienceLevel + " expTotal = " + respawnedPlayer.experienceTotal + " exp = " + respawnedPlayer.experience );
            }
            
            if( !deathTaxConfig.getShouldLoseItemsOnDeath() )
            {
                if( deadPlayerInventoryMap.containsKey( respawnedPlayerHash ) )
                {
                    Queue<ItemStack> inventoryQueue = deadPlayerInventoryMap.get( respawnedPlayerHash );
                    for( ItemStack itemStack : inventoryQueue )
                    {
                        if( !itemStack.isEmpty() )
                            respawnedPlayer.inventory.addItemStackToInventory( itemStack );
                    }
                    
                    deadPlayerInventoryMap.remove( respawnedPlayerHash );
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerDroppedExperience( final LivingExperienceDropEvent event )
    {
        if( event.getEntityLiving() instanceof ServerPlayerEntity && deathTaxConfig != null )
        {
            ServerPlayerEntity player = (ServerPlayerEntity)event.getEntityLiving();
            
            if( !player.isSpectator() && !player.world.getGameRules().getBoolean( GameRules.KEEP_INVENTORY ) && deathTaxConfig.getPercentageOfExpToLose() > 0.0f )
            {
                int playerHash = player.getUniqueID().hashCode();
                
                if( deadPlayerExpDataMap.containsKey( playerHash ) )
                {
                    PlayerExperienceData playerExpData = deadPlayerExpDataMap.get( playerHash );
                    int expToDrop = MathHelper.clamp( playerExpData.getExperienceLevel() * 7, 0, 100 );
                    
                    // Make sure that the player actually lost more xp than they should drop.
                    int expLost = Math.round( playerExpData.getExperienceTotal() * ( 1.0f - deathTaxConfig.getPercentageOfExpToLose() ) );
                    
                    event.setDroppedExperience( ( expToDrop < expLost ? expToDrop : Math.round( expLost * 0.75f ) ) );
                }
            }
        }
    }

    // Private Methods    
    private void handlePlayerDeathPenalties( ServerPlayerEntity player, DamageSource damageSource )
    {
        int playerHash = player.getUniqueID().hashCode();
        
        //LOGGER.debug( "Adding player with UUID: " + player.getUniqueID().toString() );
        //LOGGER.debug( "UUID hash: " + player.getUniqueID().hashCode() );
        // Copy dead player's experience data.
        deadPlayerExpDataMap.put( playerHash, new PlayerExperienceData( player.experienceLevel, player.experienceTotal, player.experience ) );
        
        // Copy dead player's inventory.
        deadPlayerInventoryMap.put( playerHash, new ConcurrentLinkedQueue<ItemStack>( player.inventory.mainInventory ) );
        
        boolean shouldShowDeathMessages = player.world.getGameRules().getBoolean( GameRules.SHOW_DEATH_MESSAGES );
        if( shouldShowDeathMessages )
        {
            ITextComponent deathMessageComponent = player.getCombatTracker().getDeathMessage();
            player.connection.sendPacket( new SCombatPacket( player.getCombatTracker(), SCombatPacket.Event.ENTITY_DIED, deathMessageComponent ),
                                         ( p_212356_2_ ) ->
                                         {
                                             if( !p_212356_2_.isSuccess() )
                                             {
                                                 String s = deathMessageComponent.getStringTruncated( 256 );
                                                 ITextComponent msgTooLongMessageComponent = new TranslationTextComponent( "death.attack.message_too_long", ( new StringTextComponent( s ) ).mergeStyle( TextFormatting.YELLOW ) );
                                                 ITextComponent evenMoreMagicMessageComponent = ( new TranslationTextComponent( "death.attack.even_more_magic",
                                                                                                  player.getDisplayName() ) ).modifyStyle( ( p_212357_1_ ) ->
                                                                                                  {
                                                                                                      return p_212357_1_.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT,
                                                                                                                                                        msgTooLongMessageComponent ) );
                                                                                                  } );
                                                 player.connection.sendPacket( new SCombatPacket( player.getCombatTracker(), SCombatPacket.Event.ENTITY_DIED, evenMoreMagicMessageComponent ) );
                                             }
                                         } );
            
            Team team = player.getTeam();
            if( team != null && team.getDeathMessageVisibility() != Team.Visible.ALWAYS )
            {
                if( team.getDeathMessageVisibility() == Team.Visible.HIDE_FOR_OTHER_TEAMS )
                    player.server.getPlayerList().sendMessageToAllTeamMembers( player, deathMessageComponent );
                else if( team.getDeathMessageVisibility() == Team.Visible.HIDE_FOR_OWN_TEAM )
                    player.server.getPlayerList().sendMessageToTeamOrAllPlayers( player, deathMessageComponent );
            }
            else
                player.server.getPlayerList().func_232641_a_( deathMessageComponent, ChatType.SYSTEM, Util.DUMMY_UUID );
        }
        else
            player.connection.sendPacket( new SCombatPacket( player.getCombatTracker(), SCombatPacket.Event.ENTITY_DIED ) );

        // Handles dropping equipped armor?
        Method spawnShoulderEntitiesMethod = ReflectionUtilities.getMethod( player.getClass(), "spawnShoulderEntities" );
        if( spawnShoulderEntitiesMethod != null )
        {
            try 
            { 
                spawnShoulderEntitiesMethod.setAccessible( true );
                spawnShoulderEntitiesMethod.invoke( player );
                spawnShoulderEntitiesMethod.setAccessible( false );
            }
            catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) { e.printStackTrace(); }
        }
        
        // Should mobs hold a grudge against the player or forgive them when the player dies?
        if( player.world.getGameRules().getBoolean( GameRules.FORGIVE_DEAD_PLAYERS ) )
        {
            Method forgiveDeadPlayerMethod = ReflectionUtilities.getMethod( player.getClass(), "func_241157_eT_" );
            if( forgiveDeadPlayerMethod != null )
            {
                try 
                {
                    forgiveDeadPlayerMethod.setAccessible( true );
                    forgiveDeadPlayerMethod.invoke( player ); 
                    forgiveDeadPlayerMethod.setAccessible( false );
                }
                catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) { e.printStackTrace(); }
            }
        }

        // This drops inventory and experience as well as spawns applicable loot.
        if( !player.isSpectator() )
            spawnDrops( player, damageSource );

        // Award the attacking entity with an increase in kill count and score.
        player.getWorldScoreboard().forAllObjectives( ScoreCriteria.DEATH_COUNT, player.getScoreboardName(), Score::incrementScore );
        LivingEntity attackingEntity = player.getAttackingEntity();
        if( attackingEntity != null )
        {
            player.addStat( Stats.ENTITY_KILLED_BY.get( attackingEntity.getType() ) );
            
            Field scoreValue = ReflectionUtilities.getField( player.getClass(), "scoreValue" );
            if( scoreValue != null )
            {
                int playerScoreValue;
                try
                {
                    scoreValue.setAccessible( true );
                    playerScoreValue = scoreValue.getInt( player );
                    scoreValue.setAccessible( false );
                    
                    attackingEntity.awardKillScore( player, playerScoreValue, damageSource );
                }
                catch( IllegalArgumentException | IllegalAccessException e ) { e.printStackTrace(); }
                
                Method createWitherRoseMethod = ReflectionUtilities.getMethod( player.getClass(), "createWitherRose", LivingEntity.class );
                if( createWitherRoseMethod != null )
                {
                    try 
                    {
                        createWitherRoseMethod.setAccessible( true );
                        createWitherRoseMethod.invoke( player, attackingEntity );
                        createWitherRoseMethod.setAccessible( false );
                    }
                    catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) { e.printStackTrace(); }
                }
            }
        }
        
        // Update the dying player's stats.
        player.world.setEntityState( player, (byte)3 );
        player.addStat( Stats.DEATHS );
        player.takeStat( Stats.CUSTOM.get( Stats.TIME_SINCE_DEATH ) );
        player.takeStat( Stats.CUSTOM.get( Stats.TIME_SINCE_REST ) );
        player.extinguish();
        
        // Set burning to false
        Method setFlagMethod = ReflectionUtilities.getMethod( player.getClass(), "setFlag", int.class, boolean.class );
        if( setFlagMethod != null )
        {
            try 
            {
                setFlagMethod.setAccessible( true );
                setFlagMethod.invoke( player, 0, false );
                setFlagMethod.setAccessible( false );
            }
            catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) { e.printStackTrace(); }
        }
        
        player.getCombatTracker().reset();
    }
    
    private void spawnDrops( ServerPlayerEntity player, DamageSource damageSourceIn ) 
    {
        Entity entity = damageSourceIn.getTrueSource();

        int lootingLevel = net.minecraftforge.common.ForgeHooks.getLootingLevel( player, entity, damageSourceIn );
        player.captureDrops( new java.util.ArrayList<>() );

        Field recentlyHitField = ReflectionUtilities.getField( player.getClass(), "recentlyHit" );
        
        if( recentlyHitField != null )
        {
            try
            {
                recentlyHitField.setAccessible( true );
                int recentlyHit = recentlyHitField.getInt( player );
                recentlyHitField.setAccessible( false );
                
                boolean wasRecentlyHit = recentlyHit > 0;
                
                Method isAdultMethod = ReflectionUtilities.getMethod( player.getClass(), "func_230282_cS_" );
                boolean isAdult = false;
                if( isAdultMethod != null )
                {
                    isAdultMethod.setAccessible( true );
                    isAdult = (boolean)isAdultMethod.invoke( player );
                    isAdultMethod.setAccessible( false );
                
                    if( isAdult && player.world.getGameRules().getBoolean( GameRules.DO_MOB_LOOT ) ) 
                    {
                        Method dropLootMethod = ReflectionUtilities.getMethod( player.getClass(), "dropLoot", DamageSource.class, boolean.class );
                        if( dropLootMethod != null )
                        {
                            dropLootMethod.setAccessible( true );
                            dropLootMethod.invoke( player, damageSourceIn, wasRecentlyHit );
                            dropLootMethod.setAccessible( false );
                        }
                        
                        Method dropSpecialItemsMethod = ReflectionUtilities.getMethod( player.getClass(), "dropSpecialItems", DamageSource.class, int.class, boolean.class );
                        if( dropSpecialItemsMethod != null )
                        {
                            dropSpecialItemsMethod.setAccessible( true );
                            dropSpecialItemsMethod.invoke( player, damageSourceIn, lootingLevel, wasRecentlyHit );
                            dropSpecialItemsMethod.setAccessible( false );
                        }
                    }
                }
            }
            catch( IllegalArgumentException | IllegalAccessException | InvocationTargetException e ) { e.printStackTrace(); }
        }

        if( deathTaxConfig.getShouldLoseItemsOnDeath() )
        {
            Method dropInventoryMethod = ReflectionUtilities.getMethod( player.getClass(), "dropInventory" );
            if( dropInventoryMethod != null )
            {
                try
                {
                    dropInventoryMethod.setAccessible( true );
                    dropInventoryMethod.invoke( player );
                    dropInventoryMethod.setAccessible( false );
                }
                catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) { e.printStackTrace(); }
            }
        }

        Method dropExperienceMethod = ReflectionUtilities.getMethod( player.getClass(), "dropExperience" );
        if( dropExperienceMethod != null )
        {
            try
            {
                dropExperienceMethod.setAccessible( true );
                dropExperienceMethod.invoke( player );
                dropExperienceMethod.setAccessible( false );
            }
            catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) { e.printStackTrace(); }
        }

        if( recentlyHitField != null )
        {
            try
            {
                recentlyHitField.setAccessible( true );
                int recentlyHit = recentlyHitField.getInt( player );
                recentlyHitField.setAccessible( false );
        
                Collection<ItemEntity> drops = player.captureDrops( null );
                if( !net.minecraftforge.common.ForgeHooks.onLivingDrops( player, damageSourceIn, drops, lootingLevel, recentlyHit > 0 ) )
                   drops.forEach( e -> player.world.addEntity( e ) );
            }
            catch( IllegalArgumentException | IllegalAccessException e ) { e.printStackTrace(); }
        }
    }
}
